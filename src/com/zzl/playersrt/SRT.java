
package com.zzl.playersrt;


public class SRT {
	private int beginTime;
	private int endTime;
	private String srt1;
	private String srt2;

	public int getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(int beginTime) {
		this.beginTime = beginTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public String getSrt1() {
		return srt1;
	}

	public void setSrt1(String srt1) {
		this.srt1 = srt1;
	}

	public String getSrt2() {
		return srt2;
	}

	public void setSrt2(String srt2) {
		this.srt2 = srt2;
	}

	public SRT() {
		super();
	}

	public SRT(int beginTime, int endTime, String srt1, String srt2) {
		super();
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.srt1 = srt1;
		this.srt2 = srt2;
	}

	@Override
	public String toString() {
		return "SRT [beginTime=" + beginTime + ", endTime=" + endTime + ", srt1=" + srt1 + ", srt2=" + srt2 + "]";
	}

}
