package org.springframework.data.redis.core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Jackie
 * @Date: 2019-07-26 10:52
 * @Description:
 */
public class DefaultBloomOperations extends AbstractOperations<String, String> implements BloomOperations {
    private static final String ADD_COUNT_KEY_TPL = "cnt_${bloomKey}";

    public DefaultBloomOperations(RedisTemplate<String, String> template) {
        super(template);
    }

    public void reserve(String key, double errorRate, long capacity) {
        byte[] rawKey = rawKey(key);
        byte[] rawErrorRate = rawString(String.valueOf(errorRate));
        byte[] rawInitCapacity = rawString(String.valueOf(capacity));
        execute(connection -> {
            connection.execute(BloomCommand.RESERVE.getCommand(), rawKey, rawErrorRate, rawInitCapacity);
            return null;
        }, true);
    }

    public Boolean add(String key, String value) {
        byte[] rawKey = rawKey(key);
        byte[] rawValue = rawValue(value);
        return execute(connection -> {
            Long l = (Long) connection.execute(BloomCommand.ADD.getCommand(), rawKey, rawValue);
            return Objects.equals(l, 1L);
        }, true);
    }

    public Boolean[] madd(String key, List<String> values) {
        byte[][] rawArgs = rawArgs(key, values);
        return execute(connection -> {
            List<Long> ls = (List<Long>) connection.execute(BloomCommand.MADD.getCommand(), rawArgs);
            return ls.stream().map(l -> Objects.equals(l, 1L)).toArray(Boolean[]::new);
        }, true);
    }

    @Override
    public Boolean[] insert(String key, long capacity, double errorRate, List<String> values) {
        String capacityStr = String.valueOf(capacity);
        String errorRateStr = String.valueOf(errorRate);
        final String CAPACITY = "CAPACITY";
        final String ERROR_RATE = "ERROR";
        final String ITEMS = "ITEMS";

        List<Object> arg_part_list = Arrays.asList(
                CAPACITY, capacityStr, ERROR_RATE,
                errorRateStr, ITEMS
        );

        byte[][] rawArgs = rawArgs(key, arg_part_list, values);

        return execute(connection -> {
            List<Long> ls = (List<Long>) connection.execute(BloomCommand.INSERT.getCommand(), rawArgs);
            return ls.stream().map(l -> Objects.equals(l, 1L)).toArray(Boolean[]::new);
        }, true);
    }

    @Override
    public Boolean[] insert(String key, long capacity, double errorRate, int resetExpansion, long expire, TimeUnit unit, List<String> values) {
        // key里有多少个item
        String countKey = ADD_COUNT_KEY_TPL.replace("${bloomKey}", key);
        ValueOperations<String, String> opsForValue = template.opsForValue();
        String countStr = Optional.ofNullable(opsForValue.get(countKey)).orElse("0");
        long count = Long.parseLong(countStr);

        // key的expansion数目是否已经超过
        int multiple = seriesDoubleSum(resetExpansion);
        if (count > capacity * multiple) {
            // 删除旧key
            template.delete(key);
            template.delete(countKey);

            // 新key的容量要更大
            capacity *= multiple;
        }

        // add
        Boolean[] result = this.insert(key, capacity, errorRate, values);
        opsForValue.increment(countKey, values.size());

        // expire
        template.expire(key, expire, unit);
        template.expire(countKey, expire, unit);

        return result;
    }

    @Override
    public Boolean[] insert(String key, long capacity, double errorRate, long expire, TimeUnit unit, List<String> values) {
        return this.insert(key, capacity, errorRate, 3, expire, unit, values);
    }

    private static int seriesDoubleSum(int n) {
        int sum = 0;

        for (int i = 0; i < n; i++) {
            sum = sum + (1 << i);
        }

        return sum;
    }


    public Boolean exists(String key, String value) {
        byte[] rawKey = rawKey(key);
        byte[] rawValue = rawValue(value);
        return execute(connection -> {
            Long l = (Long) connection.execute(BloomCommand.EXISTS.getCommand(), rawKey, rawValue);
            return Objects.equals(l, 1L);
        }, true);
    }

    public Boolean[] mexists(String key, List<String> values) {
        byte[][] rawArgs = rawArgs(key, values);
        return execute(connection -> {
            List<Long> ls = (List<Long>) connection.execute(BloomCommand.MEXISTS.getCommand(), rawArgs);
            return ls.stream().map(l -> Objects.equals(l, 1L)).toArray(Boolean[]::new);
        }, true);
    }

    public Boolean delete(String key) {
        return template.delete(key);
    }

    private byte[][] rawArgs(Object key, List<String> values) {
        byte[][] rawArgs = new byte[1 + values.size()][];

        int i = 0;
        rawArgs[i++] = rawKey(key);

        for (Object value : values) {
            rawArgs[i++] = rawValue(value);
        }

        return rawArgs;
    }

    private byte[][] rawArgs(Object key, List<Object> params, List<String> values) {
        byte[][] rawArgs = new byte[1 + params.size() + values.size()][];

        int i = 0;
        rawArgs[i++] = rawKey(key);

        for (Object param : params) {
            rawArgs[i++] = rawValue(param);
        }

        for (Object value : values) {
            rawArgs[i++] = rawValue(value);
        }

        return rawArgs;
    }
}
