/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.fasthink.shalemonitor.websocket;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * websocket服务端，用于创建和维护websocket连接。
 *
 */
@ServerEndpoint(value = "/websocket/{clientId}", encoders={DataMsgEncoder.class})
public class WsServer
{

	private static final Logger log = LoggerFactory.getLogger("WsServer");
	
	private Session session;
	
	private String clientId;
	
	private WsClientPool wsClientPool = WsClientPool.getInstance();
	
	public static final String DEFAULT_DEVICEID = "01";
	
    @OnOpen
	public void onOpen(@PathParam("clientId") String clientId, Session session)
	{
//		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
//		cacheManager = (CacheManager) wac.getBean("cacheManager");
		
    	log.info("WebSocket onOpen, clientId={}", clientId);
    	
		this.session = session;
		this.clientId = clientId;
		wsClientPool.addOnlineClient(clientId, session);
		wsClientPool.setClientDevice(clientId, DEFAULT_DEVICEID);
	}

	@OnMessage
	public void onMessage(String deviceId)
	{
	
		log.info("onMessage deviceId={}, clientId={}", deviceId, clientId);
		if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(deviceId))
		{
			if (wsClientPool.getOnlineClientMap().containsKey(clientId))
			{
				// 更新该client需要接收的deviceId
				wsClientPool.getClientDeviceMap().put(clientId, deviceId);
			}
			else
			{
				log.warn("Client offline, clientId={}", clientId);
			}
		}
		try
        {
			session.getBasicRemote().sendText("received message:" + clientId + "_" + deviceId);
        } catch (IOException e)
        {
	        e.printStackTrace();
        }
	}
	

    @OnClose
	public void onClose(Session session, CloseReason closeReason)
	{
    	log.info("WebSocket onClose, clientId={}", clientId);
		wsClientPool.delOfflineClient(clientId);
	}

	
	@OnError
	public void onError(Throwable t) throws Throwable
	{
	}
	
}
