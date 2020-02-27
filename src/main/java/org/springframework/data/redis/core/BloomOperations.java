package org.springframework.data.redis.core;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Jackie
 * @Date: 2019-07-26 10:44
 * @Description:
 */
public interface BloomOperations {

    /**
     * Create a Bloom Filter
     *
     * @param: key
     * @param: errorRate
     * @param: capacity
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:34
     */
    void reserve(String key, double errorRate, long capacity);

    /**
     * Add an element to filter
     *
     * @param: key
     * @param: value
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:35
     */
    Boolean add(String key, String value);

    /**
     * Batch add elements to filter
     *
     * @param: key
     * @param: values
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:36
     */
    Boolean[] madd(String key, List<String> values);

    /**
     * reserve + madd
     * @param key
     * @param capacity
     * @param errorRate
     * @param values
     * @return
     */
    Boolean[] insert(String key, long capacity, double errorRate,  List<String> values);

    /**
     * reserve + madd
     *
     * @param key
     * @param capacity
     * @param errorRate
     * @param resetExpansion expansion几次之后重置该key
     * @param expire 过期时间
     * @param unit 过期时间单位
     * @param values
     * @return
     */
    Boolean[] insert(String key, long capacity, double errorRate, int resetExpansion, long expire, TimeUnit unit, List<String> values);

    /**
     * reserve + madd
     * resetExpansion 默认为3
     * @param key
     * @param capacity
     * @param errorRate
     * @param expire 过期时间
     * @param unit 过期时间单位
     * @param values
     * @return
     */
    Boolean[] insert(String key, long capacity, double errorRate, long expire, TimeUnit unit, List<String> values);

    /**
     * Check an element is exists
     *
     * @param: key
     * @param: value
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:38
     */
    Boolean exists(String key, String value);

    /**
     * Batch check elements is exists
     *
     * @param: key
     * @param: values
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:39
     */
    Boolean[] mexists(String key, List<String> values);

    /**
     * Delete a Bloom Filter
     *
     * @param: key
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:42
     */
    Boolean delete(String key);
}
