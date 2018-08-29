package com.fasthink.shalemonitor.cache.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生成模型的缓存key
 *
 */
public final class CacheKeyUtil
{

	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String getDataKey(String id)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("domain:").append("data:").append(id);
		return sb.toString();
	}
	
	/**
	 * cacheKey: deviceId:date   如：  deviceId1:2016-09-10
	 */
	public static String getZrangeDataKey(String deviceId, long time)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(deviceId).append(":").append(format.format(new Date(time)));
		return sb.toString();
	}
	
	
	public static String getSamplingCycle()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("domain:").append("samplingCycle:");
		return sb.toString();
	}

	public static String getAvgBlock()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("domain:").append("avgBlock:");
		return sb.toString();
	}

}
