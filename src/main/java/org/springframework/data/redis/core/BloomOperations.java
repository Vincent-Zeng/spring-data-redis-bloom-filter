package org.springframework.data.redis.core;

/**
 * @Auther: Jackie
 * @Date: 2019-07-26 10:44
 * @Description:
 */
public interface BloomOperations<K, V> {

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
    void reserve(K key, double errorRate, long capacity);

    /**
     * Add an element to filter
     *
     * @param: key
     * @param: value
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:35
     */
    Boolean add(K key, V value);

    /**
     * Batch add elements to filter
     *
     * @param: key
     * @param: values
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:36
     */
    Boolean[] madd(K key, V... values);

    /**
     * reserve + madd
     * @param key
     * @param capacity
     * @param errorRate
     * @param values
     * @return
     */
    Boolean[] insert(K key, long capacity, double errorRate,  V... values);

    /**
     * reserve + add with default config
     * capacity: 1000
     * errorRate: 0.01
     * @param key
     * @param values
     * @return
     */
    Boolean[] insert(K key, V... values);

    /**
     * Check an element is exists
     *
     * @param: key
     * @param: value
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:38
     */
    Boolean exists(K key, V value);

    /**
     * Batch check elements is exists
     *
     * @param: key
     * @param: values
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:39
     */
    Boolean[] mexists(K key, V... values);

    /**
     * Delete a Bloom Filter
     *
     * @param: key
     * @return:
     * @auther: Jackie
     * @date: 2019-07-26 13:42
     */
    Boolean delete(K key);
}
