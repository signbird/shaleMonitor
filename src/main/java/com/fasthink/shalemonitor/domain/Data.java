package com.fasthink.shalemonitor.domain;

/**
 * 数据模型 
 *
 */
public class Data
{

	/**
	 * 1--原始采样值
	 */
	public static String DATATYPE_SOURCE = "1";
	/**
	 * 2--滑动平均值
	 */
	public static String DATATYPE_AVERAGE = "2";
	/**
	 * 3--加权平均值
	 */
	public static String DATATYPE_WEIGHTED = "3";
	
	
	/**
	 * 唯一表示： deviceId_time
	 */
	private String id;
	
	/**
	 * 设备ID
	 */
	private String deviceId;
	
	/**
	 * 采样时间，UTC时间
	 */
	private long time;
	
	/**
	 * 数据值
	 */
	private Number value;
	
	/**
	 * 数据类型：1--原始采样值， 2--滑动平均值，3--加权平均值
	 */
	private String type = DATATYPE_SOURCE;
	
	public Data()
	{
	}
	
	public Data(String deviceId, long time, Number value, String type)
	{
		this.deviceId = deviceId;
		this.time = time;
		this.value = value;
		this.type = type;
		this.id = deviceId + '_' + time + '_' + getRandomInt(10000, 99999);
	}

	private static int getRandomInt(int a, int b)
	{
		if (a > b || a < 0)
			return -1;
		return a + (int) (Math.random() * (b - a + 1));
	}
	
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public Number getValue()
	{
		return value;
	}

	public void setValue(Number value)
	{
		this.value = value;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[Data={id=").append(id);
		sb.append(",deviceId=").append(deviceId);
		sb.append(",time=").append(time);
		sb.append(",value=").append(value);
		sb.append(",type=").append(type);
		sb.append("}]");
		return sb.toString();
	}
}
