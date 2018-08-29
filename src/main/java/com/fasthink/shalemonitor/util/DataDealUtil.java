package com.fasthink.shalemonitor.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasthink.shalemonitor.domain.Data;

/**
 * 样例：  5a000c0107e2010e17040a0000004e6c0d0a 
 * 5a00 0c  01    07e2  01 0e 17 04 0a 0000  004e  6c  0d0a
 * 头         长度    设备ID   年         月     日      时    分    秒      毫秒        数值       SUM  结尾
 *
 */
public class DataDealUtil {

	private static final Logger log = LoggerFactory.getLogger(DataDealUtil.class);
	
	private static String PHEAD = "5a000c";
	private static String END = "0d0a";
	private static int LENGTH = 36;

	public static byte[] deal(byte[] before, byte[] now, List<Data> datas) {
		byte[] bytes = null;
		if (before == null) {
			bytes = now;
		} else {
			bytes = new byte[before.length + now.length];
			System.arraycopy(before, 0, bytes, 0, before.length);
			System.arraycopy(now, 0, bytes, before.length, now.length);
		}

		String hexStr = bytesToHexString(bytes);

		if (hexStr == null || hexStr == "") {
			return null;
		}

		String[] hexDatas = hexStr.split(PHEAD);
		for (String str : hexDatas) {
			if (str.length() < (LENGTH - PHEAD.length())) {
				continue;
			}

			String deviceId = str.substring(0, 2);
			// 获取long类型的日期值
			long time = getDateLong(str.substring(2, 20));

			// String t = getDateStr(str.substring(2, 20));
			// System.out.println(t);

			// 获取频率值
			int value = Integer.parseInt(str.substring(20, 24), 16);
			Data data = new Data(deviceId, time, value, Data.DATATYPE_SOURCE);
			datas.add(data);
			log.debug("get data from bluetooth, data={}", data);
		}
		// 未处理完的数据尾巴
		return hexStringToBytes(hexStr.substring(hexStr.lastIndexOf(END)
				+ END.length()));
	}

	/**
	 * Convert byte[] to hex string.
	 * 这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	private static long getDateLong(String dateStr) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date date = null;
		try {
			date = format.parse(getDateStr(dateStr));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	private static String getDateStr(String data) {
		StringBuffer sb = new StringBuffer();
		// 年
		sb.append(Integer.parseInt(data.substring(0, 4), 16));
		// 月
		sb.append(fillZero(
				Integer.toString(Integer.parseInt(data.substring(4, 6), 16)), 2));
		// 日
		sb.append(fillZero(
				Integer.toString(Integer.parseInt(data.substring(6, 8), 16)), 2));
		// 时
		sb.append(fillZero(
				Integer.toString(Integer.parseInt(data.substring(8, 10), 16)),
				2));
		// 分
		sb.append(fillZero(
				Integer.toString(Integer.parseInt(data.substring(10, 12), 16)),
				2));
		// 秒
		sb.append(fillZero(
				Integer.toString(Integer.parseInt(data.substring(12, 14), 16)),
				2));
		// 毫秒
		sb.append(fillZero(
				Integer.toString(Integer.parseInt(data.substring(14, 18), 16)),
				3));
		return sb.toString();
	}

	private static String fillZero(String parm, int length) {
		String result = parm;
		if (parm.length() != length) {
			for (int i = 0; i < length - parm.length(); i++) {
				result = "0" + result;
			}
		}
		return result;
	}
	
	// public static void main(String[] args) {
	// String aa =
	// "000c0107e2010e17040000000052660d0a5a000c0107e2010e1704000000003c500d0a5a000c0107e2010e170400000000566a0d0a5a000c0107e2010e170400000000495d0d0a5a000c0107e2010e17040000000044580d0a5a000c0107e2010e1704000000003f530d0a5a000c0107e2010e17040000000050640d0a5a000c0107e2010e17040000000054680d0a5a000c0107e2010e170400000000485c0d0a5a000c0107e2010e17040000000052660d0a5a000c0107e2010e170401000000495e0d0a5a000c0107e2010e1704010000004d620d0a5a000c0107e2010e1704010000003f540d0a5a000c0107e2010e1704010000004c610d0a5a000c0107e2010e1704010000005d720d0a5a000c0107e2010e17040100000051660d0a5a000c0107e2010e170401000000495e0d0a5a000c0107e2010e1704010000003c510d0a5a000c0107e2010e170401000000475c0d0a5a000c0107e2010e17040100000052670d0a5a000c0107e2010e1704010000004d620d0a5a000c0107e2010e170401000000556a0d0a5a000c0107e2010e1704010000003d520d0a5a000c0107e2010e1704010000005d720d0a5a000c0107e2010e1704010000004d620d0a5a000c0107e20100";
	// String bb = "5a";
	// byte[] r = deal(hexStringToBytes(bb), hexStringToBytes(aa));
	// System.out.println(bytesToHexString(r));
	// }

}
