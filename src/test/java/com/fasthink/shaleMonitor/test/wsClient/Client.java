package com.fasthink.shaleMonitor.test.wsClient;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
 
@ClientEndpoint
public class Client {
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to endpoint: " + session.getBasicRemote());
        try {
            session.getBasicRemote().sendText("deviceId01");
        } catch (IOException ex) {
        }
    }
 
    @OnMessage
    public void onMessage(String message) {
        System.out.println("on message: " + message);
    }
 
    @OnError
    public void onError(Throwable t) {
    	System.out.println("onError...");
        t.printStackTrace();
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    	System.out.println("onClose..., " + closeReason);
    }
}