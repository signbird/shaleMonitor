//package com.fasthink.shalemonitor.server;
//
//import java.io.IOException;
//import java.util.TimerTask;
//
//import javax.bluetooth.DiscoveryAgent;
//import javax.bluetooth.LocalDevice;
//import javax.bluetooth.UUID;
//import javax.microedition.io.Connector;
//import javax.microedition.io.StreamConnection;
//import javax.microedition.io.StreamConnectionNotifier;
//
//import org.springframework.context.ApplicationContext;
//
///**
// * 参考：
// * http://blog.csdn.net/svizzera/article/details/77434917
// * 
// * javadoc 地址：
// * http://www.bluecove.org/bluecove/apidocs/javax/bluetooth/package-summary.html
// */
//public class WaitThread extends TimerTask
//{
//	private ApplicationContext context;
//
//	public WaitThread(ApplicationContext context)
//	{
//		this.context = context;
//	}
//
//	@Override
//	public void run()
//	{
//		waitForConnection();
//	}
//
//	private void waitForConnection()
//	{
//		
//		// 本机蓝牙设备
//		LocalDevice local = null;
//
//		//接入通知
//		StreamConnectionNotifier notifier;
//		// 流连接
//		StreamConnection connection = null;
//		
//		// setup the server to listen for connection
//		try
//		{
//			local = LocalDevice.getLocalDevice();
//			local.setDiscoverable(DiscoveryAgent.GIAC);
//
//			/*
//			 * 作为服务端，被请求
//			 */
//			UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
//			String url = "btspp://localhost:" + uuid.toString() + ";name=DataServer";
//			System.out.println("url = "+url);
//			notifier = (StreamConnectionNotifier) Connector.open(url);
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			return;
//		}
//		// waiting for connection
//		while (true)
//		{
//			try
//			{
//				System.out.println("waiting for connection...");
//				connection = notifier.acceptAndOpen();
//
//				Thread processThread = new Thread(new ProcessConnectionThread(connection, context));
//				processThread.start();
//			} catch (Exception e)
//			{
//				e.printStackTrace();
//				return;
//			}
//		}
//	}
//}
