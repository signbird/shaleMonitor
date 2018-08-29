package com.fasthink.shalemonitor;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fasthink.shalemonitor.server.BluetoothService;

public class Bootstrap implements ServletContextListener {
	
	private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
	
	public void contextDestroyed(ServletContextEvent event) {
	}

	public void contextInitialized(ServletContextEvent event) {
		try {
			// spring 上下文， 用于获取bean
			ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());

			log.info("begin to start service.....");
			
			// 没有蓝牙的服务 注释掉就可以运行
			Thread a = new Thread(new BluetoothService(context));
			a.start();
			log.info("start service success.");
		} catch (Throwable e) {
			System.out.println(e);
		}
	}
}