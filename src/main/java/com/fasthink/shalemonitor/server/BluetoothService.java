package com.fasthink.shalemonitor.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasthink.shalemonitor.service.DataService;
import com.fasthink.shalemonitor.util.BluCatUtil;
import com.fasthink.shalemonitor.util.PropertyUtil;
import com.intel.bluetooth.RemoteDeviceHelper;

/**
 * http://www.bluecove.org/bluecove/apidocs/index.html
 * @author Administrator
 *
 */
public class BluetoothService extends TimerTask {
	
	private static List<String> clients;
	
	private static final Logger log = LoggerFactory.getLogger(BluetoothService.class);
	
	private ApplicationContext context;
	
	private DataService dataService;

	public BluetoothService(ApplicationContext context) {
		this.context = context;
	}
	
	private LocalDevice local = null;
	
	@Override
	public void run() {
		start();
	}
	
	public void start(){
		dataService = (DataService) context.getBean("dataService");
		
		String names = PropertyUtil.getProperty("sys.bluetooth.name", "WHJ");
		clients = Arrays.asList(names.split(","));
		log.info("property: bluetooth name list={}", clients);
		
		try {
			BluCatUtil.doctorDevice(); // 驱动检查
			RemoteDeviceDiscovery.runDiscovery(); // 搜索附近所有的蓝牙设备
			log.info("发现周围的蓝牙设备：", RemoteDeviceDiscovery.getDevices());
			
			local = LocalDevice.getLocalDevice();
			if (!local.setDiscoverable(DiscoveryAgent.GIAC)){
				log.info("请将蓝牙设置为可被发现");
			}
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		
		// 附近所有的蓝牙设备，必须先执行
		Set<RemoteDevice> devicesDiscovered = RemoteDeviceDiscovery.getDevices();
		
		joinClients(devicesDiscovered);
	}

	/**
	 * TODO  一個server是否支持同時接入多個client？
	 * @param devicesDiscovered
	 */
	private void joinClients(Set<RemoteDevice> devicesDiscovered) {
		// 连接
		for (RemoteDevice d : devicesDiscovered){
			String name = "";
			try {
				name = d.getFriendlyName(true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (clients.contains(name)){
				log.info("start to join {} ...", name);
				try {
					boolean auth = RemoteDeviceHelper.authenticate(d, "1234");
					log.info("authResult: " + auth);
					
					StreamConnection streamConnection = (StreamConnection) Connector.open("btspp://" + d.getBluetoothAddress() + ":1", Connector.READ_WRITE, true);
					log.info("连接成功：" + name + " " + streamConnection);
					
					// 启动client线程
					BluetoothClient client = new BluetoothClient(streamConnection, dataService);
					new Thread(client).run();
				} catch (Throwable e) {
					log.info("can't connect to {}, ", name , e);
				}
			}
		}
	}
	
}
