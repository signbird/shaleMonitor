package com.fasthink.shalemonitor.cache.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存管理接口，供业务测调用。
 *
 */
public interface CacheManager
{
	public static String ZRANGEBYSCORE_START = "-inf";
	public static String ZRANGEBYSCORE_END = "+inf";
	
	/**
	 * 保存缓存数据
	 */
	void set(String key, Object value);
	
	/**
	 * 保存缓存数据
	 */
	void set(String key, byte[] value);
	
	/**
	 * 保存缓存数据,带过期时间
	 * 
	 * @param expireSeconds 永不过期，传-1
	 */
	void set(String key, Object value, int expireSeconds);
	
	/**
	 * 保存缓存数据,带过期时间
	 * 
	 * @param expireSeconds 永不过期，传-1
	 */
	void setStr(String key, String value, int expireSeconds);
	
	/**
	 * 保存有序的缓存数据
	 */
	void zadd(String key, double score, String member);
	
	/**
	 * 批量保存有序的缓存数据
	 */
	void zaddBatch(String key, Map<String, Double> members);
	
	/**
	 * 移除有序集key中的一个或多个成员，不存在的成员将被忽略。
	 */
	void zrem(String key, String... members);
	
	/**
	 * 获取zadd添加的set数据
	 * 
	 * 查询所有时，start传0，end传-1
	 */
	Set<String> zrange(String key, long start, long end);
	
	/**
	 * 获取zadd列表中的set数据条数
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
	 * 清理过期缓存
	 * Removes all elements in the sorted set stored at key with a score between min and max (inclusive).
	 * http://redis.io/commands/zremrangebyscore
	 */
	long zremrangeByScore(String key, long start, long end);
	
	/**
	 * redis zrank 命令
	 * 获取某个member在zset序列中的位置
	 */
	Long zrank(String key, String member);
	
	/**
	 * redis keys 命令
	 * @param pattern
	 * @return
	 */
	Set<String> keys(String pattern);
	
	/**
	 * 缓存是否存在
	 * @param key
	 * @return
	 */
	boolean exists(String key);
	
	/**
	 * 删除缓存
	 * @param key
	 */
	int delete(String key);
	
	/**
	 * 模糊删除
	 */
	void delEx(String pattern);
	
	/**
	 * redis 的INCR命令
	 * @param key
	 * @return 自增后的结果
	 */
	Long incr(String key);
	
	/**
	 * 从缓存中获取数据。
	 * 
	 * @param key 缓存键名
	 * @return 缓存数据
	 */
	<T> T get(String key);
	
	/**
	 * 返回string
	 * @param key
	 * @return
	 */
	String getStrValue(String key);
	
	/**
	 * 批量获取缓存数据
	 * @param keys
	 * @return
	 */
	List<byte[]> getByList(List<String> keys);
	
	/**
	 * 批量获取缓存数据
	 * @param keys
	 * @return
	 */
	<T> List<T> getObjectByList(List<String> keys);
	
	/**
	 * 批量保存缓存数据
	 * 
	 * @param cacheItems
	 */
	<T> void setByList(List<CacheItem<T>> cacheItems);
	
	/**
	 * 批量保存缓存数据，带失效时间
	 * 
	 * @param cacheItems
	 */
	<T> void setByListWithExpire(List<CacheItem<T>> cacheItems);
	
	/**
	 * 重新初始化缓存
	 * @param addresses 缓存地址列表 
	 */
	void reload(String addresses);
	/**
	 * 根据key和member，获取member的score值
	 * @author shaoz 2015-08-02
	 * @param key
	 * @param member
	 * @return
	 */
	Double zscore(String key ,String member);
	
	/**
	 * redis hget命令
	 */
	String hget(String key, String field);
	
	Map<String, String> hgetAll(String key);
	
	/**
	 * redis hset命令
	 */
	long hset(String key, String field, String value);
	
	long hdel(String key, String... fields);
	
	Set<String> hkeys(String key);
	
	List<String> hvals(String key);
	
	/**
	 * redis hexists命令
	 */
	boolean hexists(String key, String field);
	
	Long hincrBy(String key, String field, long value);
	
	long sadd(String key, String... members);
	
	long srem(String key, String... members);
	
	Set<String> smembers(String key);
}
