package com.fasthink.shalemonitor.action.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasthink.shalemonitor.action.SystemConfigAction;
import com.fasthink.shalemonitor.service.SystemConfigService;
import com.fasthink.shalemonitor.service.impl.BaseServiceImpl;

@Controller
@Path("/rest/systemConfig")
public class SystemConfigActionImpl extends BaseServiceImpl implements SystemConfigAction
{
	@Autowired
	private SystemConfigService systemConfigService;
	


	@GET
	@Path("/changePeriodCount/{periodCount}")
	@Consumes(
	{ MediaType.APPLICATION_JSON })
	@Produces("application/json; charset=UTF-8")
    public void changePeriodCount(@PathParam("periodCount")String periodCount)
    {
		JSONObject json = JSON.parseObject(periodCount);
		int count = json.getIntValue("periodCount");
		
	    systemConfigService.changePeriodCount(count);
    }

	
	public SystemConfigService getSystemConfigService()
	{
		return systemConfigService;
	}

	public void setSystemConfigService(SystemConfigService systemConfigService)
	{
		this.systemConfigService = systemConfigService;
	}
}
