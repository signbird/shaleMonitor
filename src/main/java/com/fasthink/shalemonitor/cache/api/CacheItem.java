package com.fasthink.shalemonitor.cache.api;

/**
 * 缓存容器
 *
 * @param <T>
 */
public class CacheItem<T>
{
	/**
	 * 缓存key
	 */
	private String key;
	
	/**
	 * 缓存过期时间
	 */
	private int seconds;
	
	/**
	 * 缓存对象
	 */
	private T object;
	
	/**
	 * 序列化后的缓存数据
	 */
	private byte[] data;

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public int getSeconds()
	{
		return seconds;
	}

	public void setSeconds(int seconds)
	{
		this.seconds = seconds;
	}

	public T getObject()
	{
		return object;
	}

	public void setObject(T object)
	{
		this.object = object;
	}

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}
	
}
