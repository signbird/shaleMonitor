package com.fasthink.shalemonitor.websocket;

import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.alibaba.fastjson.JSON;
import com.fasthink.shalemonitor.domain.Data;

public class DataMsgEncoder implements Encoder.Text<Map<String, Data>>
{

	@Override
	public void destroy()
	{
	}

	@Override
	public void init(EndpointConfig arg0)
	{
	}

	@Override
	public String encode(Map<String, Data> object) throws EncodeException
	{
		return JSON.toJSONString(object);
	}

}