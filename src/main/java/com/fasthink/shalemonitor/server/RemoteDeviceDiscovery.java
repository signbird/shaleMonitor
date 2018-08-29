package com.fasthink.shalemonitor.server;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasthink.shalemonitor.util.BluCatUtil;
import com.intel.bluetooth.RemoteDeviceHelper;

public class RemoteDeviceDiscovery {

	private static final Logger log = LoggerFactory.getLogger(RemoteDeviceDiscovery.class);
	
	public final static Set<RemoteDevice> devicesDiscovered = new HashSet<RemoteDevice>();

	public static void runDiscovery() throws IOException, InterruptedException {
		findDevices();
	}

	private static void findDevices() throws IOException, InterruptedException {

		final Object inquiryCompletedEvent = new Object();

		devicesDiscovered.clear();

		DiscoveryListener listener = new DiscoveryListener() {
			public void inquiryCompleted(int discType) {
				log.info("#" + "搜索完成");
				synchronized (inquiryCompletedEvent) {
					inquiryCompletedEvent.notifyAll();
				}
			}

			@Override
			public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
				devicesDiscovered.add(remoteDevice);

				try {
					log.info("#发现设备" + remoteDevice.getFriendlyName(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			@Override
			public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
				log.info("#" + "servicesDiscovered");
			}

			@Override
			public void serviceSearchCompleted(int arg0, int arg1) {
				log.info("#" + "serviceSearchCompleted");
			}
		};

		synchronized (inquiryCompletedEvent) {

			LocalDevice ld = LocalDevice.getLocalDevice();

			log.info("#本机蓝牙名称:" + ld.getFriendlyName());

			boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,listener);
			
			if (started) {
				log.info("#" + "等待搜索完成...");
				inquiryCompletedEvent.wait();
				LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(listener);
				log.info("#发现设备数量：" + devicesDiscovered.size());
			}
		}

	}

	public static Set<RemoteDevice> getDevices() {

		return devicesDiscovered;
	}

	public static String deviceName(RemoteDevice d) {

		String address = d.getBluetoothAddress();

		String name = "";
		try {
			name = d.getFriendlyName(false);
		} catch (IOException e) {
			log.info("#Error: " + e.getMessage());
			try {
				name = d.getFriendlyName(false);
			} catch (IOException e2) {
				log.info("#Error: " + e2.getMessage());
			}

		}

		String rssi = "NA";

		String toret = "";

		if (BlucatState.csv)
			toret += (new Date()).getTime() + ", ";

		toret += BluCatUtil.clean(address) + ", " + "\"" + BluCatUtil.clean(name) + "\", " + "Trusted:"
				+ d.isTrustedDevice() + ", " + "Encrypted:" + d.isEncrypted();

		if (BlucatState.rssi) {
			try {
				rssi = String.valueOf(RemoteDeviceHelper.readRSSI(d));
			} catch (Throwable e) {

				String url = "btl2cap://" + d.getBluetoothAddress() + ":1";

				try {
					BlucatState.connection = Connector.open(url, Connector.READ_WRITE, true);
					rssi = String.valueOf(RemoteDeviceHelper.readRSSI(d));
					BlucatState.connection.close();

				} catch (IOException e1) {
				}
			}

			toret += ", " + rssi;

		}

		return toret;

	}

}
