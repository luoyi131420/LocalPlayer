package com.kt.localmedia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.widget.TextView;

public class ZimuMain  {


//	private StringBuffer buffer= new StringBuffer();
	private TextView textView;
	private List<SRT> listSrt= new ArrayList<SRT>();
	private Context mContext;
	private BufferedReader reader;
	private StringBuffer linesb;
	private StringBuffer contentsb;
	private int sDelay,eDelay;
	public ZimuMain(Context context){
		mContext = context;
	}
	
	String regx = "^\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}$";
	int startTime,endTime,content;
	/*
	 * 
	 * */
	public  List<SRT> getString(String videoPath,String code,int startDelay,int endDelay) {
		
		// TODO Auto-generated method stub
	this.sDelay = startDelay;
	this.eDelay = endDelay;
		try {
			
			InputStreamReader inputStreamReader = null;
			File file  = new File(videoPath);
			
			if(file.exists()){
				
			
			InputStream inputStream = new FileInputStream(file);
//			InputStream inputStream = mContext.getResources().getAssets().open("test3.srt");
			inputStreamReader = new InputStreamReader(inputStream, code);
			reader = new BufferedReader(inputStreamReader);
			linesb = new StringBuffer("");
			contentsb = new StringBuffer("");
			String line;
			
			while ((line = reader.readLine()) != null) {
				
					linesb.append(line);
					linesb.append("@");
					
				if(line.equals("")){
					setSRTLine();
				}
			}
			        setSRTLine();
		  }
			
		}
			catch (IOException e) {
			e.printStackTrace();
		}
	
		return listSrt;
	}
	
	  public  boolean matcher(String str, String nr){
		    
			Pattern pa= Pattern.compile(str);
		    
			return pa.matcher(nr).find();
		}  
	
	  
	  private void setSRTLine(){
		  SRT srt = new SRT();
			String newLine = linesb.toString();
			String [] newLinearr = newLine.split("@");
		
			for(int i=0;i<newLinearr.length;i++){
				if(matcher(regx,newLinearr[i])){
					
					String []timeLiner = newLinearr[i].split("-->");  
					
					String shours = timeLiner[0].substring(0, 2);
					String smimintes = timeLiner[0].substring(3, 5);
					String sseconds = timeLiner[0].substring(6, 8);
					String slast   = timeLiner[0].substring(9, 12);
					
					String ehours = timeLiner[1].substring(1, 3);
					String emimintes = timeLiner[1].substring(4, 6);
					String eseconds = timeLiner[1].substring(7, 9);
					String elast   = timeLiner[1].substring(10, 13);
					
					
					int shoursNum = Integer.parseInt(shours);
					int smimintesNum = Integer.parseInt(smimintes);
					int ssecondsNum = Integer.parseInt(sseconds);
					int slastNum = Integer.parseInt(slast);
					
					int ehoursNum = Integer.parseInt(ehours);
					int emimintesNum = Integer.parseInt(emimintes);
					int esecondsNum = Integer.parseInt(eseconds);
					int elastNum = Integer.parseInt(elast);
					//System.out.println("------>sDelay="+sDelay+",eDelay="+eDelay);
					startTime = (shoursNum*3600+smimintesNum*60+ssecondsNum)*1000+slastNum+sDelay;
					endTime   = (ehoursNum*3600+emimintesNum*60+esecondsNum)*1000+elastNum+eDelay;

				}
				else if(newLinearr[i].length()>0&&i>1){
					contentsb.append(newLinearr[i]);
					
				}
			}
			srt.setStartTime(startTime);
			srt.setEndTime(endTime);
			srt.setContent(contentsb.toString());
			listSrt.add(srt);
			contentsb.delete(0, linesb.length()-1);
			linesb.delete(0, linesb.length()-1);
	  }
}
