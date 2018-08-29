package com.fasthink.shalemonitor.cache.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasthink.shalemonitor.cache.CacheCoder;
import com.fasthink.shalemonitor.cache.IJedisService;
import com.fasthink.shalemonitor.cache.api.CacheItem;
import com.fasthink.shalemonitor.cache.api.CacheManager;
import com.fasthink.shalemonitor.cache.coder.FastJsonCoder;

public class CacheManagerImpl implements CacheManager
{
	private static Logger log = org.slf4j.LoggerFactory.getLogger("CacheLog");

	private static final String CHARSET = "UTF-8";
	
	/**
	 * 序列化工具
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    private CacheCoder<Object> valueSerializer = new FastJsonCoder();
	
	private IJedisService jedisService;
	
	/**
	 * 请求超时时间
	 */
    private String timeout = "100";
    
    /**
     * 缓存是否开启
     */
    private boolean enable = true;
    
//    /**
//     * 默认缓存失效时间
//     */
//    @SuppressWarnings("unused")
//    private int defaultInvalidTime = -1;
	
	@Override
	public void reload(String addresses)
	{
		if (StringUtils.isEmpty(addresses))
		{
			log.error("Cache reload error, for addresses is empty.");
		}
		
		jedisService.reload(addresses);
	}
	
	@SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			byte[] bValue = jedisService.get(key);
			if (null != bValue)
			{
				return (T)decodeValue(bValue);
			}
			
			log.debug("[cache]not found from cache, key={}", key);
			return null;
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get, key={}", key, e);
		}
		return null;
    }

	@Override
    public String getStrValue(String key)
	{
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.getStrValue(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when getStrValue, key={}", key, e);
		}
		return null;
	}
	
	@Override
	public boolean exists(String key)
	{
		if (!enable || StringUtils.isEmpty(key))
		{
			return false;
		}
		
		return jedisService.exists(key);
	}
	
	@Override
    public int delete(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return 0;
		}
		
		log.debug("[cache]delete, key={}", key);
		
		try
		{
			return jedisService.delete(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when delete, key={}", key, e);
		}
		
		return 0;
    }
	
	@Override
	public void delEx(String pattern)
	{
		if (!enable || StringUtils.isEmpty(pattern))
		{
			return;
		}
		
		log.warn("[cache]delEx, pattern={}", pattern);
		try
		{
			jedisService.delEx(pattern);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when delEx, pattern={}", pattern, e);
		}
	}
	
	@Override
    public Long incr(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		log.debug("[cache]incr, key={}", key);
		try
		{
			return jedisService.incr(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when incr, key={}", key, e);
		}
		
		return null;
    }
	
	@Override
    public void set(String key, Object value)
    {
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || value == null)
		{
			return;
		}
		
		try
		{
			jedisService.set(key, rawValue(value));
			log.debug("[cache]set, key={}, value={}", key, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when set key={}, value={}", key, value, e);
		}
    }

	@Override
    public void set(String key, Object value, int timeout)
    {
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || value == null)
		{
			return;
		}
		
		try
		{
			if (timeout > 0)
			{
				jedisService.set(key, timeout, rawValue(value));
				log.debug("[cache]set, key={}, value={}, timeout={}", key, value, timeout);
			}
			else
			{
				jedisService.set(key, rawValue(value));
			}
		}
		catch (Throwable e)
		{
			log.error("[cache]error when set, key={}, value={}, timeout={}", key, value, timeout, e);
		}
    }

	@Override
    public void setStr(String key, String value, int timeout)
    {
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || value == null)
		{
			return;
		}
		
		try
		{
			if (timeout > 0)
			{
				jedisService.setStr(key, timeout, value);
				log.debug("[cache]setStr, key={}, timeout={}, value={}", key, timeout, value);
			}
			else
			{
				jedisService.setStr(key, value);
				log.debug("[cache]setStr, key={}, value={}", key, value);
			}
		}
		catch (Throwable e)
		{
			log.error("[cache]error when setStr, key={}, value={}", key, value, e);
		}
    }
	
	
	@Override
    public void zadd(String key, double score, String member)
    {
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || member == null)
		{
			return;
		}
		
		try
		{
			jedisService.zadd(key, score, member.getBytes(CHARSET));
//			log.debug("[cache]zadd, key={}, score={}, member={}", key, score, member);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zadd, key={}, score={}, member={}", key, score, member, e);
		}
    }
	
	@Override
    public void zaddBatch(String key, Map<String, Double> members)
    {
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(members))
		{
			return;
		}
		
		Map<byte[], Double> rawMap = new HashMap<byte[], Double>();	
		for (Map.Entry<String, Double> entry : members.entrySet())
		{
			try
            {
	            rawMap.put(entry.getKey().getBytes(CHARSET), entry.getValue());
            } 
			catch (UnsupportedEncodingException e)
            {
				log.error("[cache]error when zaddBatch, key={}", e);
            }
		}
		
		try
		{
			jedisService.zaddBatch(key, rawMap);
			log.debug("[cache]zaddBatch, key={}, size={}", key, rawMap.size());
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zaddBatch, key={}, size={}", key, rawMap.size(), e);
		}
    }
	
	@Override
    public void zrem(String key, String... members)
	{
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(members))
		{
			return;
		}
		
		try
		{
			jedisService.zrem(key, members);
			log.debug("[cache]zrem, key={}, members={}", key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zrem, key={}, members={}", key, members, e);
		}
	}
	
    @Override
    public Set<String> zrange(String key, long start, long end)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.zrange(key, start, end);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zrange from cache.", e);
		}
		return null;
    }
	
    @Override
    public long zcount(String key, long min, long max)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return -1;
		}
		
		try
		{
			return jedisService.zcount(key, min, max);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zcount from cache.", e);
		}
		return -1;
    }
    
    
    @Override
    public Set<String> zrevrangeByScore(String key, long max, long min)
    {
    	if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.zrevrangeByScore(key, max, min);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get from cache.", e);
		}
		return null;
    }
    
    @Override
    public Set<String> zrangeByScore(String key, long max, long min)
    {
    	if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.zrangeByScore(key, max, min);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get from cache.", e);
		}
		return null;
    }
    
    @Override
    public Set<String> zrangeByScore(String key, String max, String min)
    {
    	if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.zrangeByScore(key, max, min);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get from cache.", e);
		}
		return null;
    }
    
	@Override
    public long zremrangeByScore(String key, long start, long end)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return 0;
		}
		
		try
		{
			return jedisService.zremrangeByScore(key, start, end);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zremrangeByScore from cache, key=" + key, e);
		}
		return 0;
    }
	
	@Override
    public Long zrank(String key, String member)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.zrank(key, member);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when zrank. key={}, member={}.", key, member, e);
		}
		return null;
    }
	
	
	@Override
	public Set<String> keys(String pattern)
	{
		if (!enable || StringUtils.isEmpty(pattern))
		{
			return null;
		}
		
		try
		{
			return jedisService.keys(pattern);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when execute keys in cache, pattern=" + pattern, e);
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<byte[]> getByList(List<String> keys)
    {
	    if (!enable)
		{
			return null;
		}
		
		try
		{
			List templateList = new ArrayList(keys.size());
			List<byte[]> byteList = jedisService.getByList(keys);
			
			int keySize = keys.size();
			int valueSize = byteList.size();
			if (keySize != valueSize)
			{
				log.error("[cache]found in cache but keyNum=" + keySize + ",valueNum=" + valueSize);
				return null;
			}
			
			for(byte[] bs : byteList)
			{
				if (bs != null)
				{
					templateList.add(decodeValue(bs));
				}
				else
				{
					templateList.add(null);
				}
			}
			
			return templateList;
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get from cache:getByList.", e);
		}
		
		return null;
    }
    

	@SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getObjectByList(List<String> keys)
    {
		if (!enable)
		{
			return null;
		}
		
		try
		{
			List<T> templateList = new ArrayList<T>(keys.size());
			List<byte[]> byteList = jedisService.getByList(keys);
			
			int keySize = keys.size();
			int valueSize = byteList.size();
			if (keySize != valueSize)
			{
				log.error("[cache]found in cache but keyNum=" + keySize + ",valueNum=" + valueSize);
				return null;
			}
			
			for(byte[] bs : byteList)
			{
				if (bs != null)
				{
					templateList.add((T)decodeValue(bs));
				}
			}
			
			return templateList;
		}
		catch (Throwable e)
		{
			log.error("[cache]error when get from cache:getByList.", e);
		}
		
		return null;
    }
    
	@Override
    public void set(String key, byte[] value)
    {
		if (!enable)
		{
			return;
		}
		
		if (StringUtils.isEmpty(key) || value == null)
		{
			return;
		}
		
		log.debug("[cache]set, key={}, value={}", key, value);
		
		try
		{
			jedisService.set(key, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when set, key={}, value={}", key, value, e);
		}
    }


	public void setTimeout(String timeout)
	{
		try
		{
			this.timeout = String.valueOf(Integer.parseInt(timeout));
		}
		catch (Exception e)
		{
			log.error("[cache]cache.requestTimeOut is not correct, value={}", timeout, e);
		}
		finally
		{
			log.info("request timeOut is set=" + this.timeout);
		}
	}

	@Override
    public <T> void setByList(List<CacheItem<T>> cacheItems)
    {
		if (!enable)
		{
			return;
		}
		
		for (CacheItem<T> cacheItem : cacheItems)
		{
			cacheItem.setData(rawValue(cacheItem.getObject()));
		}
		
		try
		{
			jedisService.setByList(cacheItems);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when setByList", e);
		}
    }

	@Override
    public <T> void setByListWithExpire(List<CacheItem<T>> cacheItems)
    {
		if (!enable)
		{
			return;
		}
		
		for (CacheItem<T> cacheItem : cacheItems)
		{
			cacheItem.setData(rawValue(cacheItem.getObject()));
		}
		
		try
		{
			jedisService.setByListWithExpire(cacheItems);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when setByListWithExpire", e);
		}
	    
    }

	public void setJedisService(IJedisService jedisService)
	{
		this.jedisService = jedisService;
	}

	@Override
    public Double zscore(String key, String member)
    {
		Double result = 0.0;
		if (!enable || StringUtils.isEmpty(key))
		{
			return 0.0;
		}
		
		try
		{
			result =  jedisService.zscore(key,member);
		} 
		catch (Throwable e)
		{
			log.error("[cache]error when zscore, key={}, member={}", key, member, e);
		}
		return result;
    }

	@Override
    public String hget(String key, String field)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(field))
		{
			return null;
		}
		
		try
		{
			return jedisService.hget(key, field);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hget from cache.", e);
		}
		return null;
    }
	
	@Override
    public Map<String, String> hgetAll(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.hgetAll(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hgetAll, key={}", key, e);
		}
		return null;
    }
	
	@Override
    public long hset(String key, String field, String value)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(field))
		{
			return -1;
		}
		
		log.debug("[cache]hset, key={}, field={}, value={}", key, field, value);
		
		try
		{
			return jedisService.hset(key, field, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hset, key={}, field={}, value={}", key, field, value, e);
		}
		return -1;
    }
	
	@Override
    public long hdel(String key, String... members)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(members))
		{
			return -1;
		}
		
		log.debug("[cache]hdel, key={}, memebers={}", key, members);
		try
		{
			return jedisService.hdel(key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hdel, key={}, memebers={}", key, members, e);
			return -1;
		}
    }
	
	@Override
    public Set<String> hkeys(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.hkeys(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hkeys, key={}", key, e);
		}
		return null;
    }
	
	@Override
    public List<String> hvals(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.hvals(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hvals, key={}", key, e);
		}
		return null;
    }
	

	@Override
    public boolean hexists(String key, String field)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(field))
		{
			return false;
		}
		
		try
		{
			return jedisService.hexists(key, field);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hexists, key={}, field={}", key, field, e);
		}
		return false;
    }

	@Override
    public Long hincrBy(String key, String field, long value)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(field))
		{
			return null;
		}
		
		try
		{
			return jedisService.hincrBy(key, field, value);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when hincrBy, key={}, field={}, value={} ", key, field, value, e);
		}
		return null;
    }
	
	
	@Override
    public long sadd(String key, String... members)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(members))
		{
			return -1;
		}
		
		log.debug("[cache]sadd, key={}, members={}", key, members);
		
		try
		{
			return jedisService.sadd(key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when sadd, key={}, members={}", key, members, e);
			return -1;
		}
    }
	
	@Override
    public long srem(String key, String... members)
    {
		if (!enable || StringUtils.isEmpty(key) || StringUtils.isEmpty(members))
		{
			return -1;
		}
		
		log.debug("[cache]srem, key={}, memebers={}", key, members);
		try
		{
			return jedisService.srem(key, members);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when srem, key={}, memebers={}", key, members, e);
			return -1;
		}
    }

	@Override
    public Set<String> smembers(String key)
    {
		if (!enable || StringUtils.isEmpty(key))
		{
			return null;
		}
		
		try
		{
			return jedisService.smembers(key);
		}
		catch (Throwable e)
		{
			log.error("[cache]error when smembers, key={}", key, e);
		}
	    return null;
    }
	
	protected byte[] rawValue(Object value)
	{
		return valueSerializer.encode(value);
	}

	protected Object decodeValue(byte[] rawValue)
	{
		return valueSerializer.decode(rawValue);
	}

	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}

//	public void setDefaultInvalidTime(int defaultInvalidTime)
//	{
//		this.defaultInvalidTime = defaultInvalidTime;
//	}

	public IJedisService getJedisService()
	{
		return jedisService;
	}
}
