package com.fasthink.shalemonitor.cache.coder;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasthink.shalemonitor.cache.CacheCoder;

 	
public class FastJsonCoder<T> implements CacheCoder<T> {
	 
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
 
    private Class<T> clazz;
    
    @Override
    public byte[] encode(Object obj)
    {
    	if (obj == null) {
            return new byte[0];
        }
        return JSON.toJSONString(obj, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T decode(byte[] bytes)
    {
		if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return (T) JSON.parseObject(str, clazz);
    }
}
