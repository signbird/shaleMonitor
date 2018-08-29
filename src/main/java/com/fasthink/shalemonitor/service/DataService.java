package com.fasthink.shalemonitor.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.fasthink.shalemonitor.domain.Data;

public interface DataService
{

	void save(Data data);
	
	/**
	 * 获取某个时间点的数据值
	 * @param deviceId
	 * @param time
	 * @return
	 */
//	Map<String/*type*/, Data> getValue(String deviceId, long time);
	
	
	/**
	 * 导出数据为excel 命名格式：deviceId_yyyyMMddHH_yyyyMMddHH.xls   
	 * excel内容：第一列时间 yyyy-mm-dd HH:mm:ss.ff  第二列数值
	 * @param deviceId
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	File exportData(String deviceId, long fromTime, long toTime);
	
	/**
	 * 获取某个时间范围内某种类型的数据
	 * @param deviceId
	 * @param fromTime
	 * @param toTime
	 * @param type
	 * @return
	 */
	List<Data> getRange(String deviceId, long fromTime, long toTime, String type);
	
	/**
	 * 获取某个时间范围的所有数据
	 * @param deviceId
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	Map<String/*type*/, List<Data>> getRange(String deviceId, long fromTime, long toTime, int max);
	
	void dealData(List<Data> datas);
	
	void push(String deviceId, Map<String/*type*/, Data> datas);
	
	/**
	 * 设置采样周期
	 * @param periodCount
	 */
	void setPeriodCount(int periodCount);
	
	int getPeriodCount();
}
