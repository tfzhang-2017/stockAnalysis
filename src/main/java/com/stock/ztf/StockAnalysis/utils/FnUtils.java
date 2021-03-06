package com.stock.ztf.StockAnalysis.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class FnUtils {
	/**
	 * 按指定大小，分隔集合，将集合按规定个数分为n个部分
	 * 
	 * @param list
	 * @param len
	 * @return
	 */
	public static List<List<?>> splitList(List<?> list, int len) {
		if (list == null || list.size() == 0 || len < 1) {
			return null;
		}

		List<List<?>> result = new ArrayList<List<?>>();

		int size = list.size();
		int count = (size + len - 1) / len;

		for (int i = 0; i < count; i++) {
			List<?> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
			result.add(subList);
		}
		return result;
	}

	/*
	 * 随机生成国内IP地址
	 */
	public static String getRandomIp() {

		// ip范围
		int[][] range = { { 607649792, 608174079 }, // 36.56.0.0-36.63.255.255
				{ 1038614528, 1039007743 }, // 61.232.0.0-61.237.255.255
				{ 1783627776, 1784676351 }, // 106.80.0.0-106.95.255.255
				{ 2035023872, 2035154943 }, // 121.76.0.0-121.77.255.255
				{ 2078801920, 2079064063 }, // 123.232.0.0-123.235.255.255
				{ -1950089216, -1948778497 }, // 139.196.0.0-139.215.255.255
				{ -1425539072, -1425014785 }, // 171.8.0.0-171.15.255.255
				{ -1236271104, -1235419137 }, // 182.80.0.0-182.92.255.255
				{ -770113536, -768606209 }, // 210.25.0.0-210.47.255.255
				{ -569376768, -564133889 }, // 222.16.0.0-222.95.255.255
		};

		Random rdint = new Random();
		int index = rdint.nextInt(10);
		String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
		return ip;
	}

	/*
	 * 将十进制转换成ip地址
	 */
	public static String num2ip(int ip) {
		int[] b = new int[4];
		String x = "";

		b[0] = (int) ((ip >> 24) & 0xff);
		b[1] = (int) ((ip >> 16) & 0xff);
		b[2] = (int) ((ip >> 8) & 0xff);
		b[3] = (int) (ip & 0xff);
		x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);

		return x;
	}
	
	public static boolean isHostConnectable(String host, int port,int timeOut) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port),timeOut);
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return true;
    }
	
	public static boolean isHostReachable(String host, Integer timeOut) {
        try {
            return InetAddress.getByName(host).isReachable(timeOut);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	/**
	 * 转换List<T> to List<String>
	 * @param ts
	 * @return
	 */
	public static <T> List<String> toStringList(List<T> ts) {
        List<String> rets=new ArrayList<String>();
        for (T t : ts) {
        	rets.add(t.toString());
		}
        return rets;
    }
	/**
	 * 四舍五入，保留两位小数
	 * @param f
	 * @return
	 */
	public static float floatRound(float f,int n){
		  BigDecimal b = new BigDecimal(f);
		  //   b.setScale(2, BigDecimal.ROUND_HALF_UP)   表明四舍五入，保留两位小数
		  return b.setScale(n, BigDecimal.ROUND_HALF_UP).floatValue();  
	}
	
	public static float strToFloat(String str){
		if(null != str && !str.isEmpty() && !str.trim().equals("-")){
			return Float.parseFloat(str);
		}else{
			return 0f;
		}
	}
	
	public static void main(String[] args) throws ParseException {
//		System.out.println(floatRound(12.34f*0.1f,2));
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String str = "2019-3-7";
        Date date = dateFormatter.parse(str);
        dateFormatter.applyPattern("D");//
        System.out.println("一年中的第几天：" + dateFormatter.format(date));

        dateFormatter.applyPattern("d");
        System.out.println("一个月中的第几天：" + dateFormatter.format(date));

        dateFormatter.applyPattern("w");
        System.out.println("一年中的第几周：" + dateFormatter.format(date));

        dateFormatter.applyPattern("W");
        System.out.println("一个月中的第几周：" + dateFormatter.format(date));

        dateFormatter.applyPattern("E");
        System.out.println("一个星期中的天数：" + dateFormatter.format(date));
	}
}
