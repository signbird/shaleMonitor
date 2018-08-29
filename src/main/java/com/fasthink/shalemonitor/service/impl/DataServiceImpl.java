package com.fasthink.shalemonitor.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasthink.shalemonitor.cache.api.CacheKeyUtil;
import com.fasthink.shalemonitor.cache.api.CacheManager;
import com.fasthink.shalemonitor.common.TimeUtil;
import com.fasthink.shalemonitor.domain.Data;
import com.fasthink.shalemonitor.service.DataService;
import com.fasthink.shalemonitor.websocket.WsClientPool;

public class DataServiceImpl extends BaseServiceImpl implements DataService
{

	/**
	 * 间隔时间超过10min, 重新统计是否丢弃
	 */
	public static long TIME_BREAK_NO_ABANDON = 10 * 60 * 1000;
	
	/**
	 * app绘图时支持展示的最大数据点数
	 */
	public static int MAX_COUNT_FOR_APP = 2000;

	/**
	 * 来自配置文件 sys.supportAbandon
	 */
	private static boolean supportAbandon = true;

	/**
	 * 来自配置文件 sys.abandonCount
	 */
	private static int abandonCount = 100;

	/**
	 * 来自配置文件 sys.periodCount
	 */
	private static int periodCount = 40;

	private WsClientPool wsClientPool = WsClientPool.getInstance();

	/**
	 * 记录从各设备接收到的连续不变的数据个数，当其达到sys.abandonSign配置值时，丢弃后面的数据，直到数据发生变化。 依赖配置
	 * sys.abandon=true
	 */
	private Map<String/* deviceId */, InnerData> repeatMap = new ConcurrentHashMap<String, InnerData>();

	private static final Logger log = LoggerFactory.getLogger("DataServiceImpl");

	@Override
	public void save(Data data)
	{
		// 只存原始数据就OK了
		cacheManager.zadd(CacheKeyUtil.getZrangeDataKey(data.getDeviceId(), data.getTime()), data.getTime(),
		        data.getId());
		cacheManager.set(CacheKeyUtil.getDataKey(data.getId()), data);
	}

	/**
	 * 从缓存中取出该ID之前的periodCount-1 个数据，用于计算平均值
	 */
	private List<Data> getSourceDataList(Data data)
	{
//		Data data = cacheManager.get(CacheKeyUtil.getDataKey(dataId));
		Long setNum = cacheManager.zrank(CacheKeyUtil.getZrangeDataKey(data.getDeviceId(), data.getTime()), data.getId());

		if (setNum == null)
		{
			log.error("can't get dataId={} from cache, which must be there.", data.getId());
		}

		Set<String> idSet = cacheManager.zrange(CacheKeyUtil.getZrangeDataKey(data.getDeviceId(), data.getTime()), setNum - periodCount - 1,
				setNum);
		if (CollectionUtils.isEmpty(idSet) || idSet.size() < periodCount)
		{
			log.info("there's no enough datas before dataId={}", data.getId());
			return null;
		}

		List<String> idList = new ArrayList<String>(idSet.size());

		for (String memberId : idSet)
		{
			idList.add(CacheKeyUtil.getDataKey(memberId));
		}

		return cacheManager.getObjectByList(idList);
	}

	/**
	 * 计算当前数据点相应的平均值
	 * 
	 * @param datas
	 *            当前数据加上之前的39个数据集合(前面不够39个数，不计算平均值，这里传空)
	 * @param data
	 *            当前要计算平均值的数据
	 * @param type
	 *            需要计算的平均值类型： 滑动平均、加权平均
	 * @return
	 */
	private Data getValue(List<Data> datas, Data data, String type)
	{
		if (CollectionUtils.isEmpty(datas))
		{
			return null;
		}

		if (Data.DATATYPE_AVERAGE.equals(type))
		{
			double salt = 1.0 / periodCount;
			int sum = 0;
			for (Data d : datas)
			{
				sum += d.getValue().intValue();
			}

			double average = sum * salt;

			return new Data(data.getDeviceId(), data.getTime(), average, Data.DATATYPE_AVERAGE);
		} 
		else if (Data.DATATYPE_WEIGHTED.equals(type))
		{
			double salt = 2.0 / (periodCount * (periodCount + 2));
			int weightedSum = 0;
			for (int i = 0; i < periodCount; i++)
			{
				weightedSum += datas.get(i).getValue().intValue() * (i + 1);
			}

			double weighted = weightedSum * salt;

			return new Data(data.getDeviceId(), data.getTime(), weighted, Data.DATATYPE_WEIGHTED);
		} 
		else
		{
			log.warn("type in wrong format, type=" + type);
			return null;
		}
	}

	@Override
	public File exportData(String deviceId, long fromTime, long toTime)
	{
		List<Data> datas = getRange(deviceId, fromTime, toTime, Data.DATATYPE_SOURCE);
		return fromData2Excel(datas, getFileName(deviceId, fromTime, toTime));
	}

	private File fromData2Excel(List<Data> datas, String fileName)
	{

		OutputStream os = null;
		WritableWorkbook wwb = null;
//		String filePath = "/opt/exportFiles/" + fileName;
		String filePath = "D://exportFiles/" + fileName;
		File file = new File(filePath);
		// 如果指定文件不存在，则新建该文件
		if (!file.isFile())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			os = new FileOutputStream(file);// 创建一个输出流
			wwb = Workbook.createWorkbook(os);
			// 创建一个工作页，第一个参数的页名，第二个参数表示该工作页在excel中处于哪一页
			WritableSheet sheet = wwb.createSheet("exportData", 0);
			// 设置表头
			// 第一个参数表示列，第二个参数表示行
			Label label00 = new Label(0, 0, "time");// 填充第一行第一个单元格的内容
			Label label10 = new Label(1, 0, "value");

			try
			{
				sheet.addCell(label00);
				sheet.addCell(label10);
				if(!CollectionUtils.isEmpty(datas))
				{
					for (int i = 0; i < datas.size(); i++)
					{
						Data data = datas.get(i);
						Label labelTime = new Label(0, i + 1, formatDate(data.getTime(), "yyyy-mm-dd HH:MM:ss.sss"));
						Label labelValue = new Label(1, i + 1, data.getValue().toString());
						sheet.addCell(labelTime);
						sheet.addCell(labelValue);
					}
				}

			} catch (RowsExceededException e)
			{
				e.printStackTrace();
			} catch (WriteException e)
			{
				e.printStackTrace();
			}

			wwb.write();// 将内容写到excel文件中
			try
			{
				wwb.close();
			} catch (WriteException e)
			{
				e.printStackTrace();
			}
			os.flush();// 清空输出流
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return file;
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

	@Override
	public List<Data> getRange(String deviceId, long fromTime, long toTime, String type)
	{
		List<String> idList = getIdList(deviceId, fromTime, toTime);
		if(CollectionUtils.isEmpty(idList)){
			return null;
		}
		
		List<Data> sourceData = cacheManager.getObjectByList(idList);

		if (Data.DATATYPE_SOURCE.equals(type))
		{
			return sourceData;
		} else if (Data.DATATYPE_AVERAGE.equals(type))
		{
			return getMovingAverage(sourceData, periodCount);
		} else if (Data.DATATYPE_WEIGHTED.equals(type))
		{
			return getWeightedAverage(sourceData, periodCount);
		} else
		{
			log.warn("type in wrong format, type=" + type);
			return null;
		}

	}

	private List<String> getIdList(String deviceId, long fromTime, long toTime)
    {
	    if (fromTime > toTime)
		{
			return null;
		}

		List<String> idList = null;

		// 是否跨天。 跨天的数据ID存储在不同的key中，需要分别取
		if (TimeUtil.isSameDayOfMillis(fromTime, toTime))
		{
			Set<String> idSet = cacheManager.zrangeByScore(CacheKeyUtil.getZrangeDataKey(deviceId, fromTime), fromTime,
			        toTime);
			if (CollectionUtils.isNotEmpty(idSet))
			{
				idList = new ArrayList<String>(idSet.size());
				for (String id : idSet)
				{
					idList.add("domain:data:" + id);
				}
			}
		} 
		else
		{
			Set<String> idSet;
			idList = new ArrayList<String>(10000);

			// 取第一天
			idSet = cacheManager.zrangeByScore(CacheKeyUtil.getZrangeDataKey(deviceId, fromTime),
			        String.valueOf(fromTime), CacheManager.ZRANGEBYSCORE_END);
			if (CollectionUtils.isNotEmpty(idSet))
			{
				for (String id : idSet)
				{
					idList.add("domain:data:" + id);
				}
			}

			// 中间的取全天
			long nextTime = fromTime + TimeUtil.MILLIS_IN_DAY;
			while (nextTime < TimeUtil.getStartOfDay(toTime))
			{
				idSet = cacheManager.zrangeByScore(CacheKeyUtil.getZrangeDataKey(deviceId, nextTime),
				        CacheManager.ZRANGEBYSCORE_START, CacheManager.ZRANGEBYSCORE_END);
				if (CollectionUtils.isNotEmpty(idSet))
				{
					for (String id : idSet)
					{
						idList.add("domain:data:" + id);
					}
				}

				nextTime += TimeUtil.MILLIS_IN_DAY;
			}

			// 取最后一天
			idSet = cacheManager.zrangeByScore(CacheKeyUtil.getZrangeDataKey(deviceId, toTime),
			        CacheManager.ZRANGEBYSCORE_START, String.valueOf(toTime));
			if (CollectionUtils.isNotEmpty(idSet))
			{
				for (String id : idSet)
				{
					idList.add("domain:data:" + id);
				}
			}
		}

		if (CollectionUtils.isEmpty(idList))
		{
			log.info("there's no data from {} to {}, where deviceId={}", fromTime, toTime, deviceId);
			return null;
		}
		
	    return idList;
    }
	

	@Override
	public Map<String/* type */, List<Data>> getRange(String deviceId, long fromTime, long toTime, int max)
	{
		List<String> idList = getIdList(deviceId, fromTime, toTime);
		if (CollectionUtils.isEmpty(idList)){
			return null;
		}
		
		List<Data> sourceData = cacheManager.getObjectByList(idList);
		
		if (CollectionUtils.isEmpty(sourceData)){
			return null;
		}
		
		int realCount = sourceData.size();
		// for test
		max = max <= 0 ? MAX_COUNT_FOR_APP : max;
		int count = sourceData.size() > max ? max : sourceData.size();
		sourceData = sourceData.subList(0, count);
		
		List<Data> averageData = getMovingAverage(sourceData, periodCount);
		List<Data> weightedData = getWeightedAverage(sourceData, periodCount);
		
		Map<String, List<Data>> result = new HashMap<String, List<Data>>(3);
		result.put(Data.DATATYPE_SOURCE, sourceData);
		result.put(Data.DATATYPE_AVERAGE, averageData);
		result.put(Data.DATATYPE_WEIGHTED, weightedData);
		// 真实条数，用于前台分页。用这种变态的方式 省的再另调一次单独查个数了
		result.put(String.valueOf(realCount), null);
		return result;
	}
	
	/**
	 * 计算一列数据的滑动平均值列表
	 * 
	 * @param source
	 * @param periodCount
	 *            计算平均值的域大小
	 * @return
	 */
	public static List<Data> getMovingAverage(List<Data> source, int periodCount)
	{

		if (source == null || source.size() == 0)
		{
			throw new IllegalArgumentException("Empty source.");
		}
		if (periodCount < 1)
		{
			throw new IllegalArgumentException("periodCount must be greater than or equal to 1.");
		}
		if (source.size() < periodCount)
		{
			// source不够长，不计算
			log.warn("the data count={}, less then periodCount, so do not calculate average.");
			return null;
		}

		List<Data> result = new ArrayList<Data>(source.size());
		// 起始计算位置 （参数中开始设计有个skip，表示计算的起始位置， 经沟通 起始值固定从periodCount-1开始）
		int firstSerial = periodCount - 1;
		String deviceId = source.get(0).getDeviceId();

		double salt = 1.0 / periodCount;

		// 先计算前periodCount个数的sum
		int sum = 0;
		for (int i = 0; i < periodCount; i++)
		{
			sum += source.get(i).getValue().intValue();
		}

		// 后面的平均值用sum * salt sum滚动更新，先进先出
		double average = 0D;
		for (int i = firstSerial; i < source.size(); i++)
		{
			average = sum * salt;
			result.add(new Data(deviceId, source.get(i).getTime(), average, Data.DATATYPE_AVERAGE));

			if (i == source.size() - 1)
			{
				// 算最后一个时 不用再更新后面的sum了，溢出
				continue;
			}
			// 滚动更新sum
			sum = sum + source.get(i + 1).getValue().intValue() - source.get(i - periodCount + 1).getValue().intValue();
		}

		return result;
	}

	/**
	 * 计算一列数据的加权平均值列表
	 * 
	 * @param source
	 * @param periodCount
	 *            计算平均值的域大小
	 * @return
	 */
	public static List<Data> getWeightedAverage(List<Data> source, int periodCount)
	{

		if (source == null || source.size() == 0)
		{
			throw new IllegalArgumentException("Empty source.");
		}
		if (periodCount < 1)
		{
			throw new IllegalArgumentException("periodCount must be greater than or equal to 1.");
		}
		if (source.size() < periodCount)
		{
			// source不够长，不计算
			log.warn("the data count={}, less then periodCount, so do not calculate average.");
			return null;
		}

		List<Data> result = new ArrayList<Data>(source.size());
		String deviceId = source.get(0).getDeviceId();

		double salt = 2.0 / (periodCount * (periodCount + 2));

		// 先算第一个加权平均值
		double headAverage = 0D;
		int sum = 0;
		int temp = 0;
		for (int i = 0; i < periodCount; i++)
		{
			sum += source.get(i).getValue().intValue();
			temp += source.get(i).getValue().intValue() * (i + 1);
		}

		headAverage = temp * salt;
		Data firstData = new Data(deviceId, source.get(periodCount).getTime(), headAverage, Data.DATATYPE_WEIGHTED);
		result.add(firstData);

		// 后面的加权平均值依托其前面一个： Pn+1 = Pn + (n * Xn+1 - sum) * salt
		for (int i = periodCount; i < source.size(); i++)
		{
			double afterAverage = headAverage + (periodCount * source.get(i).getValue().intValue() - sum) * salt;
			result.add(new Data(deviceId, source.get(i).getTime(), afterAverage, Data.DATATYPE_WEIGHTED));

			sum = sum + source.get(i).getValue().intValue() - source.get(i - periodCount).getValue().intValue();
			headAverage = afterAverage;
		}

		return result;
	}

	@Override
	public void dealData(List<Data> datas)
	{
		if (CollectionUtils.isEmpty(datas))
		{
			return;
		}
		
		log.info("ws online deviceId={}", wsClientPool.getOnlineClientMap());

		for (Data data : datas){
			String deviceId = data.getDeviceId();
			
			// 是否丢弃
			boolean abandon = false;

			if (repeatMap.containsKey(deviceId))
			{
				if (repeatMap.get(deviceId).isAbandon(data.getValue().intValue(), data.getTime()))
				{
					log.info("The data[{}] from device[{}] has repeat more than [{}] times, so abandon it.",
					        data.getValue(), deviceId, DataServiceImpl.getAbandonCount());
					abandon = true;
				}
			} else
			{
				repeatMap.put(deviceId, new InnerData(data.getValue().intValue(), data.getTime()));
			}

			if (abandon)
			{
				// 重复的数据只是不保存， 但仍然要推给客户端，否则容易造成误解
				log.info("The data[{}] from device[{}] has repeat more than [{}] times, so abandon it.", data.getValue(),
				        deviceId, DataServiceImpl.getAbandonCount());
			} else
			{
				log.info("save data to redis, data={}", data);
				save(data);
			}
			
			if (wsClientPool.needThisDevice(deviceId))
			{
				for (Map.Entry<String, String> entry : wsClientPool.getClientDeviceMap().entrySet())
				{
					log.info("wsClient joined deviceId={}, data deviceId={}", entry.getValue(), deviceId);
					
					if (entry.getValue().equals(deviceId))
					{
						Map<String, Data> mapDatas = new HashMap<String, Data>(3);
						mapDatas.put(Data.DATATYPE_SOURCE, data);

						// 思路 ： 直接从缓存取出前39个，直接计算。 其实挺快的 完全来得及
						List<Data> sourceDatas = getSourceDataList(data);
						mapDatas.put(Data.DATATYPE_AVERAGE, getValue(sourceDatas, data, Data.DATATYPE_AVERAGE));
						mapDatas.put(Data.DATATYPE_WEIGHTED, getValue(sourceDatas, data, Data.DATATYPE_WEIGHTED));

						push(entry.getKey(), mapDatas);
					}
				}
			}
		}
	}

	@Override
	public void push(String clientId, Map<String, Data> datas)
	{
		Session wsSession = wsClientPool.getOnlineClientMap().get(clientId);

		if (wsSession == null)
		{
			log.error("Client offline,  clientId={}", clientId);
			return;
		}

		try
		{
			log.info("send data to clientId={}, data.value={}", clientId, datas.get(Data.DATATYPE_SOURCE).getValue());
			// 异步调用
			wsSession.getAsyncRemote().sendObject(datas);
		} catch (Throwable e)
		{
			log.error("Failed to push datas to clientId={}", clientId, e);
			return;
		}
	}

	@Override
	public int getPeriodCount()
	{
		return periodCount;
	}

	@Override
	public void setPeriodCount(int periodCount)
	{
		DataServiceImpl.periodCount = periodCount;
	}

	public static boolean isSupportAbandon()
	{
		return supportAbandon;
	}

	public static void setSupportAbandon(boolean supportAbandon)
	{
		DataServiceImpl.supportAbandon = supportAbandon;
	}

	public static int getAbandonCount()
	{
		return abandonCount;
	}

	public static void setAbandonCount(int abandonCount)
	{
		DataServiceImpl.abandonCount = abandonCount;
	}

}

/**
 * 用于数据去重（接收端休眠）的模型类
 *
 */
class InnerData
{
	int value;

	long time;

	int repeatCount;

	public InnerData(int value, long time)
	{
		this.value = value;
		this.time = time;
		this.repeatCount = 0;
	}

	public boolean isAbandon(int newValue, long newTime)
	{
		if (!DataServiceImpl.isSupportAbandon())
		{
			return false;
		}

		// 间隔时间超过10min, 重新统计
		if ((newTime - this.time) > DataServiceImpl.TIME_BREAK_NO_ABANDON)
		{
			this.time = newTime;
			this.value = newValue;
			repeatCount = 0;
			return false;
		} else
		{
			this.time = newTime;

			if (this.value == newValue)
			{
				if (this.repeatCount < DataServiceImpl.getAbandonCount())
				{
					repeatCount++;
					return false;
				} else
				{
					// 满足丢弃条件，丢弃
					return true;
				}
			} else
			{
				// 出现不同数据，重新统计
				this.value = newValue;
				repeatCount = 0;
				return false;
			}
		}

	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public int getRepeatCount()
	{
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount)
	{
		this.repeatCount = repeatCount;
	}

}
