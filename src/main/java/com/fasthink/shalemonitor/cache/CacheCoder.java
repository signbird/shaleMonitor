package com.fasthink.shalemonitor.cache;

import java.nio.charset.Charset;

/**
 * 缓存对象序列化
 */
public interface CacheCoder<T>
{
	/**
	 * UTF-8字符集常量
	 */
	Charset UTF_8 = Charset.forName("UTF-8");
	
	/**
	 * 空字节数组常量
	 */
	byte[] EMPTY_BYTES = new byte[0];
	
	/**
	 * 空字符串常量
	 */
	String EMPTY_STRING = "";
	
	/**
	 * 对象序列化
	 * 
	 * 注意：此方法不能返回null
	 */
	byte[] encode(T obj);
	
	/**
	 * 对象反序列化
	 */
	T decode(byte[] bytes);

}
