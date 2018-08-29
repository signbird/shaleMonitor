package com.fasthink.shalemonitor.cache.coder;

import com.fasthink.shalemonitor.cache.CacheCoder;

/**
 * 字符串序列化工具
 */
public class StringCoder implements CacheCoder<String>
{

	@Override
    public byte[] encode(String str)
    {
		if (str != null && str.length() > 0)
		{
			return str.getBytes(UTF_8);
		}
		
		return EMPTY_BYTES;
    }

	@Override
    public String decode(byte[] bytes)
    {
		if (bytes != null && bytes.length > 0)
		{
			return new String(bytes, UTF_8);
		}
		
		return EMPTY_STRING;
    }
}
