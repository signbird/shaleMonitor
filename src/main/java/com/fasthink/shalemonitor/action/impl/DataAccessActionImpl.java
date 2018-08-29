package com.fasthink.shalemonitor.action.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasthink.shalemonitor.action.DataAccessAction;
import com.fasthink.shalemonitor.domain.Data;
import com.fasthink.shalemonitor.service.DataService;

@Controller
@Path("/rest/dataAccess")
public class DataAccessActionImpl implements DataAccessAction
{
	@Resource
	private DataService dataService;

	@GET
	@Path("/fetchHistoryData/{parm}")
	@Consumes(
	{ MediaType.APPLICATION_JSON })
	@Produces("application/json; charset=UTF-8")
	public String fetchHistoryData(@PathParam("parm") String parm)
	{
		JSONObject json = JSON.parseObject(parm);
		String deviceId = json.getString("deviceId");
		long fromTime = json.getLongValue("fromTime");
		long toTime =  json.getLongValue("toTime");
		int max =  json.getIntValue("max");
		Map<String/* type */, List<Data>> dataList = dataService.getRange(deviceId, fromTime, toTime, max);
		if(dataList==null)
		{
			dataList = new HashMap<String/* type */, List<Data>>();
		}
		return JSONObject.toJSONString(dataList);
	}

	@GET
	@Path("/exportExcel/{parm}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response  exportExcel(@PathParam("parm") String parm)
	{
		JSONObject json = JSON.parseObject(parm);
		String deviceId = json.getString("deviceId");
		long fromTime = json.getLongValue("fromTime");
		long toTime =  json.getLongValue("toTime");
		File file = dataService.exportData(deviceId, fromTime, toTime);
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition", "attachment; filename=\""+ getFileName(deviceId, fromTime, toTime) +"\"");
		return response.build();
	}
	
	@GET
	@Path("/export/{parm}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces("application/json; charset=UTF-8")
	public Response  exportExcelDefault(@PathParam("parm") String parm)
	{
		JSONObject json = JSON.parseObject(parm);
		String deviceId = json.getString("deviceId");
		int min = json.getInteger("min");
		
		if (StringUtils.isEmpty(deviceId) || min < 1){
			return null;
		}
		long fromTime = System.currentTimeMillis() - min * 60 * 1000;
		long toTime =  System.currentTimeMillis();
		File file = dataService.exportData(deviceId, fromTime, toTime);
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition", "attachment; filename=\""+ file.getPath() +"\"");
		return response.build();
	}

	private String getFileName(String deviceId, long fromTime, long toTime)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(deviceId).append("_").append(formatDate(fromTime, "yyyymmddHH")).append("_")
		        .append(formatDate(toTime, "yyyymmddHH")).append(".xls");
		return sb.toString();
	}

	private String formatDate(long time, String formatStr)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		return sdf.format(new Date(time));
	}

	public DataService getDataService()
	{
		return dataService;
	}

	public void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}

	

}
