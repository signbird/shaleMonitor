package com.fasthink.shalemonitor.common;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtil
{

	public static final int SECONDS_IN_DAY = 60 * 60 * 24;
	public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

	public static boolean isSameDayOfMillis(final long ms1, final long ms2)
	{
		final long interval = ms1 - ms2;
		return interval < MILLIS_IN_DAY && interval > -1L * MILLIS_IN_DAY && toDay(ms1) == toDay(ms2);
	}

	private static long toDay(long millis)
	{
		return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
	}
	
	/**
	 * 获取已知时间戳所在天的开始时间
	 * @param millis
	 * @return
	 */
	public static long getStartOfDay(long millis){
		Calendar dayEnd = Calendar.getInstance();
		dayEnd.setTimeInMillis(millis);
		dayEnd.set(Calendar.HOUR_OF_DAY, 0);
		dayEnd.set(Calendar.SECOND, 0);
		dayEnd.set(Calendar.MINUTE, 0);
		dayEnd.set(Calendar.MILLISECOND, 0); 
		
        return dayEnd.getTimeInMillis();
    }  
}
