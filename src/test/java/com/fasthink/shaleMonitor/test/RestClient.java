package com.fasthink.shaleMonitor.test;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

public class RestClient
{

	public static String webRoot = "http://192.168.1.122:8080/shaleMonitor/rest";

	public static void main(String[] args)
	{
		fetchHistoryData();
	}
	private static void exportExcel()
	{
		JSONObject json = new JSONObject();
		json.put("deviceId", "D002");
		json.put("fromTime", 1474334340295l);
		json.put("toTime", 1474334350295l);
		try
		{
			String path = "/dataAccess/exportExcel/" + URLEncoder.encode(json.toJSONString(), "UTF-8");
			get(path);
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	private static void fetchHistoryData()
	{
		JSONObject json = new JSONObject();
		json.put("deviceId", "D002");
		json.put("fromTime", 1474334340295l);
		json.put("toTime", 1474334350295l);
		try
		{
			String path = "/dataAccess/fetchHistoryData/" + URLEncoder.encode(json.toJSONString(), "UTF-8");
			get(path);
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	


	public static void post(String path)
	{
		HttpPost post = new HttpPost(webRoot + path);
		try
		{
			HttpResponse response = new DefaultHttpClient().execute(post);
			int code = response.getStatusLine().getStatusCode();
			System.out.println("code = " + code + ",content = " + EntityUtils.toString(response.getEntity()));
		} catch (Exception e)
		{
		}
	}

	public static void get(String path)
	{
		String urllistallbook = webRoot + path;
		System.out.println("url = "+urllistallbook);
		HttpGet httpGet = new HttpGet(urllistallbook);
		try
		{
			long start = System.currentTimeMillis();
			new DefaultHttpClient().execute(httpGet);
			System.out.println("耗时:" + (System.currentTimeMillis() - start));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
