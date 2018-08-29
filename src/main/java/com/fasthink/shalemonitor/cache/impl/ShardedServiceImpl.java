package com.fasthink.shalemonitor.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.MDC;
import org.slf4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisSentinelPool;

import com.fasthink.shalemonitor.cache.api.CacheItem;
import com.fasthink.shalemonitor.cache.IJedisService;

/**
 * Redis缓存访问支持。
 * 
 */
public class ShardedServiceImpl implements IJedisService
{

	private static Logger log = org.slf4j.LoggerFactory.getLogger(ShardedServiceImpl.class);
	
	private static int REDIS_MAXWAIT = 200;
	
	private static final String CHARSET = "UTF-8";
	
	/**
	 * Redis 分片模式连接池客户端
	 */
	private ShardedJedisSentinelPool shardedJedisPool;
	
	public void setShardedJedisPool(ShardedJedisSentinelPool shardedJedisPool)
	{
		this.shardedJedisPool = shardedJedisPool;
	}
	
	@Override
	public void reload(String addresses)
	{
		shardedJedisPool.reload(addresses);
	}
	
	@Override
    public byte[] get(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.get(key.getBytes(CHARSET));
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when get from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public String getStrValue(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.get(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when getStrValue from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public boolean exists(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			
			return jedis.exists(key);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when get from cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
		
		return false;
    }
	
	@Override
    public int delete(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			
			return jedis.del(key.getBytes(CHARSET)).intValue();
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when get from cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
		
		return 0;
    }
	
	@Override
	public void delEx(String pattern)
	{
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			
			//ShardedJedis不支持keys方法,需要重置为jedis实现。 参考 http://www.zyiqibook.com/article151.html
			Collection<Jedis> jedisList = jedis.getAllShards();
			
			for (Jedis temp : jedisList)
			{
				try
				{
					Set<String> keys = temp.keys(pattern);

					for (String key : keys)
					{
						if (temp.del(key) > 0)
						{
							log.debug("deleted key={} from redis={}", key, temp.getClient().getHost());
						}
					}
				} catch (Throwable e)
				{
					log.error("[{}] delEx failed, ", temp.getClient().getHost(), e.getMessage());
				}
			}
		}
		catch (Throwable e)
		{
			log.error("[cache]error when execute keys in cache. pattern=" + pattern, e);
		}
		finally
		{
			returnJedis(jedis);
		}
	}

	@Override
    public void set(String key, int seconds, byte[] value)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.setex(key.getBytes(CHARSET), seconds, value);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when get from cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public void setStr(String key, int seconds, String value)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.setex(key, seconds, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get from cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public void setStr(String key, String value)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.set(key, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when setStr to cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public void set(String key, byte[] value)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.set(key.getBytes(CHARSET), value);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when set from cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public Long incr(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.incr(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when incr from cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
		
		return null;
    }
	
	/**
	 * 采用管线模式，批量获取
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<byte[]> getByList(List<String> keyList)
    {
    	int size = keyList.size();
    	List objectList = new ArrayList(size);
    	ShardedJedis jedis = null;
    	List<Object> responseList = new ArrayList<Object>(size);
    	
    	try
    	{
    		jedis = shardedJedisPool.getResource();
    		ShardedJedisPipeline shardedJedisPipeline = jedis.pipelined();
    		
    		for (String key : keyList)
    		{
    			responseList.add(shardedJedisPipeline.get(key.getBytes(CHARSET)));
    		}
    		shardedJedisPipeline.sync();
    		for (int i = 0; i < size; i++)
    		{
    			objectList.add(((Response)responseList.get(i)).get());
    		}
    		
    		return objectList; 
    	}
    	catch (Throwable e)
    	{
    		log.warn("error when set cache." + e);
    		return objectList;
    	}
    	finally
    	{
    		returnJedis(jedis);
    	}
		
    }
    
    @Override
    public void zadd(String key, double score, byte[] member)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.zadd(key.getBytes(CHARSET), score, member);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zadd to cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public void zaddBatch(String key, Map<byte[], Double> members)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.zadd(key.getBytes(CHARSET), members);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zadd to cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public void zrem(String key, String... members)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			jedis.zrem(key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zrem to cache. key=" + key + ", members=" + members, e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public Set<String> zrange(String key, long start, long end)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zrange(key, start, end);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zrange from cache. key=" + key + ", start=" + start + ", end=" + end, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public long zcount(String key, long min, long max)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zcount(key, String.valueOf(min), String.valueOf(max));
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zcount from cache. key=" + key + ", min=" + min + ", max=" + max, e);
			return -1;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public Set<String> zrevrangeByScore(String key, long max, long min)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zrevrangeByScore(key, max, min);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zrevrangeByScore from cache. key=" + key + ", max=" + max + ", min=" + min, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public Set<String> zrangeByScore(String key, long max, long min)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zrangeByScore(key, max, min);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zrangeByScore from cache. key=" + key + ", max=" + max + ", min=" + min, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public Set<String> zrangeByScore(String key, String max, String min)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zrangeByScore(key, max, min);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zrangeByScore from cache. key=" + key + ", max=" + max + ", min=" + min, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
    
    @Override
    public long zremrangeByScore(String key, long start, long end)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zremrangeByScore(key, String.valueOf(start), String.valueOf(end));
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zremrangeByScore to cache. key=" + key, e);
		}
		finally
		{
			returnJedis(jedis);
		}
		return 0;
    }
    
    @Override
    public Long zrank(String key, String member)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zrank(key, member);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zrank. key={}, member={}.", key, member, e);
		}
		finally
		{
			returnJedis(jedis);
		}
		return null;
    }
    
    @Override
    public Set<String> keys(String pattern)
    {
    	ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			
			// FIXME ShardedJedis不支持keys方法，下面这种实现可能会有性能问题
			// 参考 http://www.zyiqibook.com/article151.html
			return jedis.getShard("*").keys(pattern);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when execute keys in cache. pattern=" + pattern, e);
		}
		finally
		{
			returnJedis(jedis);
		}
		return null;
    }

//    public void setAutoInvalidation(String key, byte[] value)
//    {
//		
//		long shardedStartTime = System.currentTimeMillis();
//		long shardedEndTime = 0;
//		long shardedCostTime = 0;
//		
//		ShardedJedis jedis = null;
//		try
//		{
//			jedis = shardedJedisPool.getResource();
//			jedis.set(key.getBytes(CHARSET), value);
//		}
//		catch (Throwable e)
//		{
//			// 缓存异常不应影响主体业务逻辑，故只打日志
//			log.error("[cache]error when set cache autoinvalidation. key=" + key, e);
//		}
//		finally
//		{
//			shardedEndTime = System.currentTimeMillis();
//			shardedCostTime = shardedEndTime - shardedStartTime;
//			
//			if (shardedCostTime > REDIS_MAXWAIT)
//			{
//				MDC.put("moduleName", "trans");
//				log.info(new StringBuilder().append("DESC=redis set data execte timeout.").append("costtime=")
//				        .append(shardedCostTime).append("ms").toString());
//			}
//			
//			returnJedis(jedis);
//		}
//    }

//    @Override
//    public Map<String, Pool<?>> getJedisPool()
//    {
//	    return poolMap;
//    }

    
	@Override
    public <T> void setByList(List<CacheItem<T>> cacheItems)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
    		ShardedJedisPipeline shardedJedisPipeline = jedis.pipelined();
    		
    		for (CacheItem<T> cacheItem : cacheItems)
    		{
    			shardedJedisPipeline.set(cacheItem.getKey().getBytes(CHARSET), cacheItem.getData());
    		}
    		shardedJedisPipeline.sync();
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when set cache by list.", e);
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public <T> void setByListWithExpire(List<CacheItem<T>> cacheItems)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
    		ShardedJedisPipeline shardedJedisPipeline = jedis.pipelined();
    		
    		for (CacheItem<T> cacheItem : cacheItems)
    		{
    			shardedJedisPipeline.setex(cacheItem.getKey().getBytes(CHARSET), cacheItem.getSeconds(), cacheItem.getData());
    		}
    		shardedJedisPipeline.sync();
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when set cache by list.", e);
		}
		finally
		{
			returnJedis(jedis);
		}
	    
    }

	/**
	 * 向缓存池返回缓存连接
	 * 
	 * @param jedis jedis
	 * 
	 */
	private void returnJedis(ShardedJedis jedis)
    {
		if (null == jedis)
		{
			return;
		}
		long shardedStartTime = System.currentTimeMillis();
		long shardedEndTime = 0;
		long shardedCostTime = 0;
		try
		{
			// 由于调用returnResource() 也可能抛出异常，而缓存里即使异常也不应影响主体业务逻辑，所以捕获所有异常
			shardedJedisPool.returnResource(jedis);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when return cache connection.");
			shardedJedisPool.returnBrokenResource(jedis);
		}
		finally
		{
			shardedEndTime = System.currentTimeMillis();
			shardedCostTime = shardedEndTime - shardedStartTime;
			
			if (shardedCostTime > REDIS_MAXWAIT)
			{
				MDC.put("moduleName", "trans");
				log.info(new StringBuilder().append("DESC=return connection execte timeout.").append("costtime=")
				        .append(shardedCostTime).append("ms").toString());
			}
		}
    }

	@Override
    public Double zscore(String key, String member)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.zscore(key, member);
		}
		catch (Throwable e)
		{
			// 缓存异常不应影响主体业务逻辑，故只打日志
			log.error("[cache]error when zscore from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	
	@Override
    public String hget(String key, String field)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hget(key, field);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hset from cache. key=" + key + ", field="+ field, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public Map<String, String> hgetAll(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hgetAll(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hgetAll from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public long hset(String key, String field, String value)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hset(key, field, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hset to cache. key=" + key + ", field="+ field + ", value=" + value, e);
			return -1;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public long hdel(String key, String... fields)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hdel(key, fields);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hdel from cache. key=" + key + ", fields="+ fields, e);
			return -1;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public Set<String> hkeys(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hkeys(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hkeys from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public List<String> hvals(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hvals(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hvals from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public boolean hexists(String key, String field)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hexists(key, field);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hexists from cache. key=" + key + ", field="+ field, e);
			return false;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
    public Long hincrBy(String key, String field, long value)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.hincrBy(key, field, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hincrBy. key={}, field={}, value={} ", key, field, value, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
	public long sadd(String key, String... members)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.sadd(key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when sadd from cache. key=" + key + ", members="+ members, e);
			return -1;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
	public Set<String> smembers(String key)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.smembers(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when sadd from cache. key=" + key, e);
			return null;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	
	@Override
	public long srem(String key, String... members)
    {
		ShardedJedis jedis = null;
		try
		{
			jedis = shardedJedisPool.getResource();
			return jedis.srem(key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when srem from cache. key=" + key + ", members="+ members, e);
			return -1;
		}
		finally
		{
			returnJedis(jedis);
		}
    }
	 
}
