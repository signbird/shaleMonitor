package com.fasthink.shalemonitor.server;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasthink.shalemonitor.domain.Data;
import com.fasthink.shalemonitor.service.DataService;
import com.fasthink.shalemonitor.util.DataDealUtil;


public class BluetoothClient implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(BluetoothClient.class);
	
	private DataService dataService;
	
	// 流连接
	private StreamConnection streamConnection = null;
	// 输入流
	private DataInputStream inputStream;
	
	public BluetoothClient(StreamConnection streamConnection, DataService dataService) {
		this.streamConnection = streamConnection;
		this.dataService = dataService;
	}

	@Override
	public void run() {
		try {
			log.info("start client..." + streamConnection);
			inputStream = streamConnection.openDataInputStream();

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			byte[] lastLeft = null;
			int length;
			
			List<Data> datas = new ArrayList<Data>();
			while ((length = inputStream.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			    lastLeft = DataDealUtil.deal(lastLeft, result.toByteArray(), datas);
			    
			    dataService.dealData(datas);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				if (streamConnection != null)
					streamConnection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
