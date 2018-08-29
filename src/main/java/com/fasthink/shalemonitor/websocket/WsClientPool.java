package com.fasthink.shalemonitor.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WsClientPool
{

	/**
	 * 所有的在线终端
	 */
	private Map<String/*clientId*/, Session> onlineClientMap = new ConcurrentHashMap<String, Session>();
	
	/**
	 * 在线终端当前设置的deviceId
	 */
	private Map<String/*clientId*/,String/*deviceId*/> clientDeviceMap = new ConcurrentHashMap<String, String>();
	
	private static final Logger log = LoggerFactory.getLogger("WsClientPool");
	
	/**
	 * 单例模式
	 */
	private static WsClientPool instance = new WsClientPool();
	
	/**
	 * 返回单例对象
	 */
	public static WsClientPool getInstance()
	{
		return instance;
	}
	
	private WsClientPool()
	{
	}
	
	public Map<String, Session> getOnlineClientMap()
	{
		return onlineClientMap;
	}

	public void addOnlineClient(String clientId, Session session)
	{
		onlineClientMap.put(clientId, session);
	}
	
	public Map<String, String> getClientDeviceMap()
	{
		return clientDeviceMap;
	}
	
	public void setClientDevice(String clientId, String deviceId)
	{
		clientDeviceMap.put(clientId, deviceId);
	}
	
	public boolean needThisDevice(String deviceId)
	{
		return this.clientDeviceMap.containsValue(deviceId);
	}
	
	public void delOfflineClient(String clientId)
	{
		if (StringUtils.isNotEmpty(clientId) && onlineClientMap.containsKey(clientId))
		{
			CloseReason closeReason = new CloseReason(CloseCodes.NORMAL_CLOSURE, "server close webSocket session");
			try
	        {
				onlineClientMap.get(clientId).close(closeReason);
	        }
			// 忽略服务端关闭session的异常，打印了没啥意思 造成日志干扰
			// 这类异常一般是由于客户端非正常关闭了session, 目前发现的两种：java.lang.reflect.InvocationTargetException  和  IllegalStateException
			catch (IllegalStateException e)
			{
				log.info("[WebSocket] session has been closed when server try to close it, catched IllegalStateException.");
			}
			catch (Throwable e)
	        {
	        	log.warn("[WebSocket] error when server try to close webSocket session, catched Throwable Exception.");
	        }
			
			onlineClientMap.remove(clientId);
			clientDeviceMap.remove(clientId);
		}
	}
}


