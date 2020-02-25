package org.springframework.data.redis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Auther: Jackie
 * @Date: 2019-07-26 10:52
 * @Description:
 */
public class DefaultBloomOperations<K, V> extends AbstractOperations<K, V> implements BloomOperations<K, V> {

    public DefaultBloomOperations(RedisTemplate<K, V> template) {
        super(template);
    }

    public void reserve(K key, double errorRate, long capacity) {
        byte[] rawKey = rawKey(key);
        byte[] rawErrorRate = rawString(String.valueOf(errorRate));
        byte[] rawInitCapacity = rawString(String.valueOf(capacity));
        execute(connection -> {
            connection.execute(BloomCommand.RESERVE.getCommand(), rawKey, rawErrorRate, rawInitCapacity);
            return null;
        }, true);
    }

    public Boolean add(K key, V value) {
        byte[] rawKey = rawKey(key);
        byte[] rawValue = rawValue(value);
        return execute(connection -> {
            Long l = (Long) connection.execute(BloomCommand.ADD.getCommand(), rawKey, rawValue);
            return Objects.equals(l, 1L);
        }, true);
    }

    public Boolean[] madd(K key, V... values) {
        byte[][] rawArgs = rawArgs(key, values);
        return execute(connection -> {
            List<Long> ls = (List<Long>) connection.execute(BloomCommand.MADD.getCommand(), rawArgs);
            return ls.stream().map(l -> Objects.equals(l, 1L)).toArray(Boolean[]::new);
        }, true);
    }

    @Override
    public Boolean[] insert(K key, long capacity, double errorRate, V... values) {
        String capacityStr = String.valueOf(capacity);
        String errorRateStr = String.valueOf(errorRate);
        final String CAPACITY = "CAPACITY";
        final String ERROR_RATE = "ERROR";
        final String ITEMS = "ITEMS";

        List<Object> arg_part_list = new ArrayList<>(Arrays.asList(
                CAPACITY, capacityStr, ERROR_RATE,
                errorRateStr, ITEMS
        ));
        arg_part_list.addAll(Arrays.asList(values));

        Object[] arg_part_arr = arg_part_list.toArray(new Object[arg_part_list.size()]);
        byte[][] rawArgs = rawArgs(key, arg_part_arr);

        return execute(connection -> {
            List<Long> ls = (List<Long>) connection.execute(BloomCommand.INSERT.getCommand(), rawArgs);
            return ls.stream().map(l -> Objects.equals(l, 1L)).toArray(Boolean[]::new);
        }, true);
    }

    @SafeVarargs
    @Override
    public final Boolean[] insert(K key, V... values) {
        return this.insert(key, 1000, 0.01, values);
    }

    public Boolean exists(K key, V value) {
        byte[] rawKey = rawKey(key);
        byte[] rawValue = rawValue(value);
        return execute(connection -> {
            Long l = (Long) connection.execute(BloomCommand.EXISTS.getCommand(), rawKey, rawValue);
            return Objects.equals(l, 1L);
        }, true);
    }

    public Boolean[] mexists(K key, V... values) {
        byte[][] rawArgs = rawArgs(key, values);
        return execute(connection -> {
            List<Long> ls = (List<Long>) connection.execute(BloomCommand.MEXISTS.getCommand(), rawArgs);
            return ls.stream().map(l -> Objects.equals(l, 1L)).toArray(Boolean[]::new);
        }, true);
    }

    public Boolean delete(K key) {
        return template.delete(key);
    }

    private byte[][] rawArgs(Object key, Object... values) {
        byte[][] rawArgs = new byte[1 + values.length][];

        int i = 0;
        rawArgs[i++] = rawKey(key);

        for (Object value : values) {
            rawArgs[i++] = rawValue(value);
        }

        return rawArgs;
    }
}
