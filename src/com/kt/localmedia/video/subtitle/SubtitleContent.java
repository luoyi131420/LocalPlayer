/**
 * 
 */
package com.kt.localmedia.video.subtitle;

import android.graphics.Bitmap;

/**
 * @author hm
 *
 */
public class SubtitleContent  implements Comparable<SubtitleContent>{
	private int SubtitleIndex = 0;
	private int SubtitleStartTime = 0;
	private int SubtitleEndTime = 0;

	private String SubtitleLine = null;
	private Bitmap SubtitleBmp = null;
	private int mFilepos = 0;

	private String mLanguageClass = null;

	public SubtitleContent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getSubtitleIndex() {
		return SubtitleIndex;
	}

	public void setSubtitleIndex(int SubtitleIndex) {
		this.SubtitleIndex = SubtitleIndex;
	}

	public int getSubtitleStartTime() {
		return SubtitleStartTime;
	}

	public void setSubtitleStartTime(int SubtitleStartTime) {
		this.SubtitleStartTime = SubtitleStartTime;
	}

	public int getSubtitleEndTime() {
		return SubtitleEndTime;
	}

	public void setSubtitleEndTime(int SubtitleEndTime) {
		this.SubtitleEndTime = SubtitleEndTime;
	}

	public String getSubtitleLine() {
		return SubtitleLine;
	}

	public void setSubtitleLine(String SubtitleLine) {
		this.SubtitleLine = SubtitleLine;
	}

	public String getmLanguageClass() {
		return mLanguageClass;
	}

	public void setmLanguageClass(String mLanguageClass) {
		this.mLanguageClass = mLanguageClass;
	}
	
	public synchronized Bitmap getSubtitleBmp() {
		return SubtitleBmp;
	}

	public synchronized void setSubtitleBmp(Bitmap SubtitleBmp) {
		this.SubtitleBmp = SubtitleBmp;
	}
	
	public synchronized boolean hasSubTitleBmp(){
		return SubtitleBmp!=null&&!SubtitleBmp.isRecycled();
	}
	
	public synchronized void recycleSubTitleBmp(){
		if((SubtitleBmp!=null)&&(SubtitleBmp.isRecycled())){
			SubtitleBmp.recycle();
			SubtitleBmp = null;
		}
	}

	public int getmFilepos() {
		return mFilepos;
	}

	public void setmFilepos(int mFilepos) {
		this.mFilepos = mFilepos;
	}
	
	public int compareTo(SubtitleContent another) {
		// TODO Auto-generated method stub
		return new Integer(this.getSubtitleStartTime()).compareTo(new Integer(another.getSubtitleStartTime()));   
	}
	
	//can use clone too
	public SubtitleContent getSimpleCopy(){
		SubtitleContent newcontent = new SubtitleContent();
		newcontent.setmFilepos(this.getmFilepos());
		newcontent.setmLanguageClass(this.getmLanguageClass());
		newcontent.setSubtitleEndTime(this.getSubtitleEndTime());
		newcontent.setSubtitleIndex(this.getSubtitleIndex());
		newcontent.setSubtitleLine(this.getSubtitleLine());
		newcontent.setSubtitleStartTime(this.getSubtitleStartTime());
		newcontent.setSubtitleBmp(this.getSubtitleBmp());
		
		return newcontent;
	}
}
