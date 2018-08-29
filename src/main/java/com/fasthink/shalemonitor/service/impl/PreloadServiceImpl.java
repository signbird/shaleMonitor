package com.fasthink.shalemonitor.service.impl;

import java.util.Arrays;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import com.fasthink.shalemonitor.domain.Data;
import com.fasthink.shalemonitor.service.DataService;

public class PreloadServiceImpl implements InitializingBean, ServletContextAware
{

	private static final Logger log = LoggerFactory.getLogger("PreloadServiceImpl");
	
	DataService dataService;
	
	@Override
    public void setServletContext(ServletContext arg0)
    {
    }

	@Override
    public void afterPropertiesSet() throws Exception
    {
		log.info("== start service success...");
		
		// for test
//		Thread test = new Thread(new ForTest());
//		test.start();
	}
	
	
	public void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}


	class ForTest implements Runnable
	{
		@Override
        public void run()
        {
			while (true)
			{
				try
	            {
		            Thread.sleep(2000);
	            } catch (InterruptedException e)
	            {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
	            }
				
				String deviceId = "01";
				Data data = new Data(deviceId, System.currentTimeMillis(), getRandomInt(200, 600), Data.DATATYPE_SOURCE);
				
				dataService.dealData(Arrays.asList(data));
			}
        }
	}
	
	private static int getRandomInt(int a, int b)
	{
		if (a > b || a < 0)
			return -1;
		return a + (int) (Math.random() * (b - a + 1));
	}

	
	
}
