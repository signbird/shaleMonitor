package com.fasthink.shalemonitor.cache.coder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.fasthink.shalemonitor.cache.CacheCoder;
import com.fasthink.shalemonitor.cache.api.CacheItem;

/**
 * Protostuff 缓存编解码器实现
 *
 */
public class ProtostuffCacheCoder<T> implements CacheCoder<T>
{

	static final Logger log = LoggerFactory.getLogger(ProtostuffCacheCoder.class);
	
    @SuppressWarnings("rawtypes")
    static final Schema<CacheItem> containerSchema = RuntimeSchema.getSchema(CacheItem.class);
	
	/**
	 * Preferably the size of your largest
	 */
	static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
	
	static final ThreadLocal<LinkedBuffer> threadSafeBuffer = new ThreadLocal<LinkedBuffer>()
	{
		@Override
		protected LinkedBuffer initialValue()
		{
			return LinkedBuffer.allocate(DEFAULT_BUFFER_SIZE);
		}
	};
	
	@Override
    public byte[] encode(Object obj)
    {
		// 对象不存在时，保存为空数组
		if (obj == null)
		{
			return EMPTY_BYTES;
		}
		
		try 
		{
			LinkedBuffer buffer = threadSafeBuffer.get();
			buffer.clear();
			
			CacheItem<Object> container = new CacheItem<Object>();
			container.setObject(obj);
			
			return ProtobufIOUtil.toByteArray(container, containerSchema, buffer);
		}
		catch (Exception e)
		{
			// 记录日志，返回null
			log.error("ProtostuffCacheCoder.encode failed ", e);
			return null;
		}
    }

	@SuppressWarnings("unchecked")
    @Override
    public T decode(byte[] bytes)
    {
		// 参数合法性校验
		if (bytes == null || bytes.length == 0)
		{
			return null;
		}
		
		try
		{
			CacheItem<T> container = containerSchema.newMessage();
			ProtobufIOUtil.mergeFrom(bytes, container, containerSchema);
			
			return container.getObject();
		}
		catch (Exception e)
		{
			// 记录日志，返回null
			log.error("ProtostuffCacheCoder.decode failed ", e);
			return null;
		}
    }

}
