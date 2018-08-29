package com.fasthink.shalemonitor.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasthink.shalemonitor.cache.api.CacheItem;

/**
 * Jedis缓存服务接口。
 *
 */
public abstract interface IJedisService
{
	/**
	 * 重载
	 */
	void reload(String addresses);
	
	/**
	 * 从缓存中获取数据。
	 * 
	 * @param key 缓存键名
	 * @return 缓存数据
	 */
	byte[] get(String key);
	
	String getStrValue(String key);
	
	/**
	 * 批量获取缓存数据
	 * @param keys
	 * @return
	 */
	List<byte[]> getByList(List<String> keys);
	
	boolean exists(String key);
	
	int delete(String key);
	
	void delEx(String pattern);
	
	/**
	 *  INCR命令 
	 */
	Long incr(String key);
	
	/**
	 * 
	 * @param key
	 * @param timeout
	 * @param value
	 */
	void set(String key, int timeout, byte[] value);
	
	void setStr(String key, int seconds, String value);
	
	void setStr(String key, String value);
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	void set(String key, byte[] value);
	
	/**
	 * 
	 * 
	 * @param cacheItems
	 */
	<T> void setByList(List<CacheItem<T>> cacheItems);
	
	/**
	 * 批量保存缓存数据，带失效时间
	 * @param cacheItems
	 */
	<T> void setByListWithExpire(List<CacheItem<T>> cacheItems);
	
	/**
	 * http://redis.io/commands/zadd
	 * 
	 * @param key
	 * @param score
	 * @param member
	 */
	void zadd(String key, double score, byte[] member);
	
	/**
	 * zadd的批量模式
	 * @param key
	 * @param members
	 */
	void zaddBatch(String key, Map<byte[], Double> members);
	
	/**
	 * 移除有序集key中的一个或多个成员，不存在的成员将被忽略。
	 */
	void zrem(String key, String... members);
	
	/**
	 * 获取zadd添加的set数据
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	Set<String> zrange(String key, long start, long end);
	
	/**
	 * 获取zadd列表中的set数据条数
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	long zcount(String key, long min, long max);
	
	/**
	 * 返回有序集key中，score值介于max和min之间(默认包括等于max或min)的所有的成员。有序集成员按score值递减(从大到小)的次序排列。
	 */
	Set<String> zrevrangeByScore(String key, long max, long min);
	
	/**
	 * 返回有序集key中，score值介于max和min之间(默认包括等于max或min)的所有的成员。
	 */
	Set<String> zrangeByScore(String key, long max, long min);
	
	Set<String> zrangeByScore(String key, String max, String min);
	
	/**
	 * Removes all elements in the sorted set stored at key with a score between min and max (inclusive).
	 * http://redis.io/commands/zremrangebyscore
	 * 
	 * @param key
	 * @param start
	 * @param end
	 */
	long zremrangeByScore(String key, long start, long end);
	
	/**
	 * redis zrank 命令
	 */
	Long zrank(String key, String member);
	
	/**
	 * redis keys 命令
	 * @param pattern
	 * @return
	 */
	Set<String> keys(String pattern);
	
	/**
	 * 根据key和member，获取member的score值
	 * @author shaoz 2015-08-02
	 * @param key
	 * @param member
	 * @return
	 */
	Double zscore(String key ,String member);
	
	
	String hget(String key, String field);
	
	Map<String, String> hgetAll(String key);
	
	long hset(String key, String field, String value);
	
	long hdel(String key, String... fields);
	
	Set<String> hkeys(String key);
	
	List<String> hvals(String key);
	
	boolean hexists(String key, String field);
	
	Long hincrBy(String key, String field, long value);
	
	long sadd(String key, String... members);
	
	long srem(String key, String... members);
	
	Set<String> smembers(String key);
}
