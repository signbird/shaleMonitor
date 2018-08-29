package com.fasthink.shalemonitor.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasthink.shalemonitor.common.PropertiesConfig;
import com.fasthink.shalemonitor.service.DataService;
import com.fasthink.shalemonitor.service.SystemConfigService;

public class SystemConfigServiceImpl extends BaseServiceImpl implements SystemConfigService
{

	private DataService dataService;
	
	private static final Logger log = LoggerFactory.getLogger("SystemConfigServiceImpl");
	
	@Override
    public void changePeriodCount(int periodCount)
    {
		if (periodCount <= 0)
		{
			log.error("The periodCount be seted is invalid, periodCount={}", periodCount);
			return ;
		}
		
		dataService.setPeriodCount(periodCount);
		
		PropertiesConfig.writeData("resource.properties", "sys.periodCount", String.valueOf(periodCount));
    }
	
	
	public void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}

}
