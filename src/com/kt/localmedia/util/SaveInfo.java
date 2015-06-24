package com.kt.localmedia.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class SaveInfo {
	SharedPreferences shareZimu;
	SharedPreferences.Editor editorZimu;
	SharedPreferences shareMusicPlayMode;
	SharedPreferences.Editor editorMusicPlayMode;
	SharedPreferences shareMusicPath;
	SharedPreferences.Editor editorMusicPath;
	SharedPreferences sharePlayPath;
	SharedPreferences.Editor editorPlayPath;;
	private static final String PREFS_NAME = "com.kt30.media";
	Context mContext;
	public SaveInfo(Context context,String type){	
		mContext = context;
		if(type.equals("zimu")){
			shareZimu=context.getSharedPreferences("com.kt30.media.zimu",Context.MODE_PRIVATE);
			editorZimu = shareZimu.edit();
		}

		if(type.contains("musicPlayMode")){
			shareMusicPlayMode=context.getSharedPreferences("com.kt30.media.musicPlayMode",Context.MODE_PRIVATE);
			editorMusicPlayMode = shareMusicPlayMode.edit(); 
		}	
		if(type.contains("musicPath")){
			shareMusicPath=context.getSharedPreferences("com.kt30.media.musicPath",Context.MODE_PRIVATE);
			editorMusicPath = shareMusicPath.edit();
		}
		if(type.contains("playPath")){
			sharePlayPath=context.getSharedPreferences("com.kt30.media.playPath",Context.MODE_PRIVATE);
			editorPlayPath = sharePlayPath.edit();
		}
	}

	public void saveZimuPath(String path){	
		editorZimu.putString("zimuPath", path); 
		editorZimu.commit(); 
	}
	
	public String getZimuPath(){		
		String zimuPath=shareZimu.getString("zimuPath", "");
		return zimuPath;		
	}
	
	public void saveZimuSettings(int color,int size,String code,int time){		
		editorZimu.putInt("zimuColor", color); 
		editorZimu.putInt("zimuSize", size); 
		editorZimu.putString("zimuCode", code); 
		editorZimu.putInt("zimuDelay", time); 
		editorZimu.commit(); 
	}
	
	public void clearZimuSettings(){	
		editorZimu.clear();  
		editorZimu.commit();
	}
	
	public int getZimuColor(){		
		int zimuColor=shareZimu.getInt("zimuColor", Color.WHITE);		
		return zimuColor;		
	}
	
	public int getZimuSize(){		
		int zimuSize=shareZimu.getInt("zimuSize", 36);
		return zimuSize;		
	}
	
	public String getZimuCode(){		
		String zimuCode=shareZimu.getString("zimuCode", "gb2312");
		return zimuCode;		
	}
	
	public int getZimuDelay(){		
		int zimuTime=shareZimu.getInt("zimuDelay", 0);
		return zimuTime;		
	}
		
	public void saveMusicPlayMode(int mode){	
		editorMusicPlayMode.putInt("musicPlayMode", mode); 
		editorMusicPlayMode.commit(); 
	}
	
	public int getMusicPlayMode(){		
		int mode=shareMusicPlayMode.getInt("musicPlayMode", 3);
		return mode;		
	}
	
	public void savePlayPath(String path){	
		editorPlayPath.putString("playPath", path); 
		editorPlayPath.commit(); 
	}
	
	public String getPlayPath(){		
		String playPath=sharePlayPath.getString("playPath", "");
		return playPath;		
	}
		
	public void saveMusicPath(String path){	
		editorMusicPath.putString("musicPath", path); 
		editorMusicPath.commit(); 
	}
	
	public String getMusicPath(){		
		String musicPath=shareMusicPath.getString("musicPath", "");
		return musicPath;		
	}
	
	public void clearMusicPath(){	
		editorMusicPath.clear();  
		editorMusicPath.commit();
	}
	
	
	
	
	public void storeVideoPlayMode(int mode){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("VideoPlayMode", mode);
		editor.commit();
	}
	public int getVideoPlayMode(){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		int mode = settings.getInt("VideoPlayMode", Constant.PLAY_MODE_ORDER);
		return mode;
	}
	
	public void storeVideoPlayPath(String path){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("VideoPlayPath", path);
		editor.commit();
	}
	
	public String getVideoPlayPath(){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		String playPath = settings.getString("VideoPlayPath", "");
		return playPath;
	}
	
	
	public void storeMusicPlayMode(int mode){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("MusicPlayMode", mode);
		editor.commit();
	}
//	public int getMusicPlayMode(){
//		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
//		int mode = settings.getInt("MusicPlayMode", Constant.PLAY_MODE_ORDER);
//		return mode;
//	}
	
	public void storeMusicPlayPath(String path){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("MusicPlayPath", path);
		editor.commit();
	}
	
	public String getMusicPlayPath(){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		String playPath = settings.getString("MusicPlayPath", "");
		return playPath;
	}
	
	public void storeZimuTextColor(int color){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("zimuColor", color);
		editor.commit();
	}
	
	public int getZimuTextColor(){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		int zimuColor = settings.getInt("zimuColor", Color.WHITE);
		return zimuColor;
	}
	
	public void storeZimuTextSize(int size){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("zimuSize", size);
		editor.commit();
	}
	
	public int getZimuTextSize(){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		int zimuSize = settings.getInt("zimuSize", 36);
		return zimuSize;
	}
}