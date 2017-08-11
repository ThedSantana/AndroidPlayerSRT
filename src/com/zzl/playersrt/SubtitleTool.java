package com.zzl.playersrt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.util.Log;

public class SubtitleTool {
	private final static String EXPRESSION = "[0-9]+";
	private final static String EXPRESSION1 = "[0-9][0-9]:[0-5][0-9]:[0-5][0-9],[0-9][0-9][0-9] --> [0-9][0-9]:[0-5][0-9]:[0-5][0-9],[0-9][0-9][0-9]";
	private static final String TAG = "SubtitleTool";

	/**
	 * @description 解析srt字幕文件
	 * @param filepath
	 * @return
	 * @version 1.0
	 */
	public static ArrayList<SRT> parseSrt(String filepath) {
		String charset = getCharset(filepath);// 判断文件编码格式
		ArrayList<SRT> srts = new ArrayList<SRT>();
		String line = null;
		String startTime, endTime;
		String nowRow = "", oldRow = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(filepath)), charset));
			SRT srt = null;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					// 匹配为空行
				} else if (Pattern.matches(EXPRESSION, line)) {
					// 匹配为标号
					nowRow = line;
				} else if (Pattern.matches(EXPRESSION1, line)) {
					// 匹配为时间
					startTime = line.substring(0, 12);
					endTime = line.substring(17, 29);
					int start = TimeToMs(startTime) - 500;// 发现字幕会延后大概半秒，所以把字幕往前提了，同理字幕前后调整也是在这调
					int end = TimeToMs(endTime);
					if (srt != null) {
						srts.add(srt);// 把本条字幕添加到字幕集中
						srt = null;
					}
					srt = new SRT();
					srt.setBeginTime(start);
					srt.setEndTime(end);
				} else {
					// 其他为内容
					if (!oldRow.equals(nowRow)) {
						byte[] b = line.getBytes();
						String str = new String(b, "utf-8");
						if (srt != null) {
							// 此while处理特效字幕,特效字幕中会包含如下东西：
							// {\fad(500,500)}{\pos(320,30)}{\fn方正粗倩简体\b1}{\bord0}{\fs20}{\c&H24EFFF&}特效&时轴：{\fs20}
							// {\c&HFFFFFF&} 土皮
							while (str.contains("{") && str.contains("}")
									&& (str.indexOf("{") < str.indexOf("}"))) {
								str = str.replace(
										str.substring(str.indexOf("{"),
												str.indexOf("}") + 1), "");
							}
							// 去掉黑块
							if (!str.equalsIgnoreCase("■")) {
								srt.setSrt1(str);
							}
						}
					} else {
						byte[] b = line.getBytes();
						String str = new String(b, "utf-8");
						if (srt != null) {
							// 此while处理特效字幕
							while (str.contains("{") && str.contains("}")
									&& (str.indexOf("{") < str.indexOf("}"))) {
								str = str.replace(
										str.substring(str.indexOf("{"),
												str.indexOf("}") + 1), "");
							}
							// 去掉黑块
							if (!str.equalsIgnoreCase("■")) {
								srt.setSrt2(str);
							}
						}
					}
					oldRow = nowRow;
				}
			}
			if (srt != null) {
				srts.add(srt);// 把最后一条字幕添加到字幕集中
				srt = null;
			}
			reader.close();
			return srts;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<SRT> parseSrtFromNetwork(String filepath) {

		Log.d(TAG, "zzl---filepath==" + filepath);
		String charset = getCharsetFromNetwork(filepath);// 判断文件编码格式

		if (charset == null) {

			return null;
		}

		ArrayList<SRT> srts = new ArrayList<SRT>();
		String line = null;
		String startTime, endTime;
		String nowRow = "", oldRow = "";
		try {
			String srtname = filepath.substring(filepath.lastIndexOf("/") + 1);

			filepath = filepath.substring(0, filepath.lastIndexOf("/")) + "/"
					+ URLEncoder.encode(srtname, "utf-8");// 中文字符
			URL url = new URL(filepath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(2 * 1000);
			conn.setReadTimeout(2 * 1000);
			// 取得inputStream，并进行读取
			conn.connect();

			InputStream input = conn.getInputStream();

			Log.d(TAG, "zzl---charset==" + charset);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input, charset));

			SRT srt = null;
			while ((line = reader.readLine()) != null) {

				// Log.d("","line===="+line);
				if (line.equals("")) {
					// 匹配为空行
				} else if (Pattern.matches(EXPRESSION, line)) {
					// 匹配为标号
					nowRow = line;
				} else if (Pattern.matches(EXPRESSION1, line)) {
					// 匹配为时间
					startTime = line.substring(0, 12);
					endTime = line.substring(17, 29);
					int start = TimeToMs(startTime);// -500
													// 发现字幕会延后大概半秒，所以把字幕往前提了，同理字幕前后调整也是在这调
					int end = TimeToMs(endTime);

					Log.d(TAG, "--startTime==" + startTime + start
							+ "--endTime" + endTime + end);
					if (srt != null) {
						srts.add(srt);// 把本条字幕添加到字幕集中
						srt = null;
					}
					srt = new SRT();
					srt.setBeginTime(start);
					srt.setEndTime(end);
				} else {
					// 其他为内容
					if (!oldRow.equals(nowRow)) {
						byte[] b = line.getBytes();
						String str = new String(b, "utf-8");
						if (srt != null) {
							// 此while处理特效字幕,特效字幕中会包含如下东西：
							// {\fad(500,500)}{\pos(320,30)}{\fn方正粗倩简体\b1}{\bord0}{\fs20}{\c&H24EFFF&}特效&时轴：{\fs20}
							// {\c&HFFFFFF&} 土皮
							while (str.contains("{") && str.contains("}")
									&& (str.indexOf("{") < str.indexOf("}"))) {
								str = str.replace(
										str.substring(str.indexOf("{"),
												str.indexOf("}") + 1), "");
							}

							// 去掉黑块
							if (!str.equalsIgnoreCase("■")) {
								srt.setSrt1(str);

								Log.d(TAG, "str==" + str);
							}
						}
					} else {
						byte[] b = line.getBytes();
						String str = new String(b, "utf-8");
						if (srt != null) {
							// 此while处理特效字幕
							while (str.contains("{") && str.contains("}")
									&& (str.indexOf("{") < str.indexOf("}"))) {
								str = str.replace(
										str.substring(str.indexOf("{"),
												str.indexOf("}") + 1), "");
							}
							// 去掉黑块
							if (!str.equalsIgnoreCase("■")) {
								srt.setSrt2(str);

								if (str.contains("<")) { // <font size="13">How
															// does this story
															// begin?</font>

									String[] strs1 = str.split(">");

									String src2 = strs1[1];

									if (src2.contains("<")) {

										String[] strs2 = src2.split("<");

										String strDone = strs2[0];

										System.out.println(strDone);

										srt.setSrt2(strDone);
									}
								}
							}
						}
					}
					oldRow = nowRow;
				}
			}
			if (srt != null) {
				srts.add(srt);// 把最后一条字幕添加到字幕集中
				srt = null;
			}
			reader.close();
			return srts;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @description 时间轴转换为毫秒
	 * @param time
	 * @return
	 * @version 1.0
	 */
	private static int TimeToMs(String time) {
		int hour = Integer.parseInt(time.substring(0, 2));
		int mintue = Integer.parseInt(time.substring(3, 5));
		int scend = Integer.parseInt(time.substring(6, 8));
		int milli = Integer.parseInt(time.substring(9, 12));
		int msTime = (hour * 3600 + mintue * 60 + scend) * 1000 + milli;
		return msTime;
	}

	/**
	 * @description 判断文件的编码格式
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @version 1.0
	 */
	public static String getCharsetFromNetwork(String fileName) {
		String code = null;

		try {

			String srtname = fileName.substring(fileName.lastIndexOf("/") + 1);

			fileName = fileName.substring(0, fileName.lastIndexOf("/")) + "/"
					+ URLEncoder.encode(srtname, "utf-8");

			URL url = new URL(fileName);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(2 * 1000);
			conn.setReadTimeout(2 * 1000);
			// 取得inputStream，并进行读取
			conn.connect();
		
			InputStream input = conn.getInputStream();

			BufferedInputStream bin = new BufferedInputStream(input);
			int p = (bin.read() << 8) + bin.read();
			switch (p) {
			case 0xefbb:
				code = "UTF-8";
				break;
			case 0xfffe:
				code = "Unicode";
				break;
			case 0xfeff:
				code = "UTF-16BE";
				break;
			default:
				code = "GBK";
			}
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}

		return code;
	}

	public static String getCharset(String fileName) {
		String code = "UTF-8";
		try {
			BufferedInputStream bin = new BufferedInputStream(
					new FileInputStream(fileName));
			int p = (bin.read() << 8) + bin.read();
			switch (p) {
			case 0xefbb:
				code = "UTF-8";
				break;
			case 0xfffe:
				code = "Unicode";
				break;
			case 0xfeff:
				code = "UTF-16BE";
				break;
			default:
				code = "GBK";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code;
	}

}
