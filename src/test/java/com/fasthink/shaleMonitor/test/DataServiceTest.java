package com.fasthink.shaleMonitor.test;

import java.util.ArrayList;
import java.util.List;

import com.fasthink.shalemonitor.domain.Data;
import com.fasthink.shalemonitor.service.impl.DataServiceImpl;

public class DataServiceTest 
{
	
	public static void main(String[] args)
    {
		test();
		perfTest();
		perfTest();
		perfTest();
		perfTest();
    }
	
	public static void test()
	{
		List<Data> source = new ArrayList<Data>();
		source.add(new Data("d1", 1, 105, "1"));
		source.add(new Data("d1", 1, 101, "1"));
		source.add(new Data("d1", 1, 98, "1"));
		source.add(new Data("d1", 1, 96, "1"));
		source.add(new Data("d1", 1, 108, "1"));
		source.add(new Data("d1", 1, 89, "1"));
		source.add(new Data("d1", 1, 111, "1"));
		source.add(new Data("d1", 1, 93, "1"));
		source.add(new Data("d1", 1, 92, "1"));
		source.add(new Data("d1", 1, 107, "1"));
		
		for (Data d : source)
		{
			System.out.println(d.getValue());
		}
		
//		List<Data> result = getWeightedAverage(source, 4);
		List<Data> result = DataServiceImpl.getMovingAverage(source, 4);
		System.out.println("result------------");
		for (Data d : result)
		{
			System.out.println(d.getValue());
		}
	}
	
	private static void perfTest()
	{
		int total = 360000;
		int N = 40;
		
		List<Data> source = new ArrayList<Data>(total);
		for (int i = 0; i < total; i++)
		{
			source.add(new Data("1", 1200000 + i, 90 + getRandomInt(0, 20), "1"));
		}
		
//		long start0  = System.currentTimeMillis();
//		getWeightedAverage(source, N);
//		long end0 = System.currentTimeMillis();
//		System.out.println(total + "个数，计算加权平均用时：" + (end0 - start0) + "ms");
		
		long start1 = System.currentTimeMillis();
		DataServiceImpl.getMovingAverage(source, N);
		long end1 = System.currentTimeMillis();
		System.out.println(total + "个数，计算滑动平均用时：" + (end1 - start1) + "ms");
		
		DataServiceImpl.getWeightedAverage(source, N);
		long end2 = System.currentTimeMillis();
		System.out.println(total + "个数，计算加权平均用时：" + (end2 - end1) + "ms");
	}
	
	private static int getRandomInt(int a, int b)
	{
		if (a > b || a < 0)
			return -1;
		// 下面两种形式等价
		// return a + (int) (new Random().nextDouble() * (b - a + 1));
		return a + (int) (Math.random() * (b - a + 1));
	}
}
