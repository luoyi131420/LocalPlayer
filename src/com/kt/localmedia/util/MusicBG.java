package com.kt.localmedia.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

public class MusicBG {

	private Context mContext;
	public MusicBG(Context c){
		this.mContext= c;
	}
	
	public ArrayList<HashMap<String, Object>> scanAllAudioFiles(){
		//生成动态数组，并且转载数据  
		     ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();  

		//查询媒体数据库
		Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		//遍历媒体数据库
		if(cursor.moveToFirst()){
		 
		       while (!cursor.isAfterLast()) { 
		       
		        //歌曲编号
		        int id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));  
		        //歌曲标题
		        String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.TITLE));  
		        //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
		        String album = cursor.getString(cursor.getColumnIndexOrThrow(AudioColumns.ALBUM));  
		        //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
		        String artist = cursor.getString(cursor.getColumnIndexOrThrow(AudioColumns.ARTIST));  
		        //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
		        String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.DATA));    
		        //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
		        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(AudioColumns.DURATION));    
		        //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
		        Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaColumns.SIZE));
		       
		        if(size>1024*800){//大于800K
		        HashMap<String, Object> map = new HashMap<String, Object>();
		        map.put("musicId", id);  
		        map.put("musicTitle", tilte);  
		        map.put("musicFileUrl", url); 
		           map.put("music_file_name", tilte);  
		           mylist.add(map);  
		        }
		        cursor.moveToNext(); 
		       } 
		       }
		return mylist;
		}

}
