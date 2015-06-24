package com.kt.localmedia.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.kt.localmedia.R;
import com.kt.localmedia.music.MusicInfo;
import com.kt.localmedia.music.poster.PosterInfo;
import com.kt.localmedia.video.VideoInfo;

	public class PlayerUtil {  
	private static final String TAG = "PlayerUtil";
	private static String musicTitle;
	private static String musicUrl;
	private static String hms_total;
	List<MusicInfo> musicList;
	List<VideoInfo> videoList;
	static Context mContext;
	private static String imageFilePath;
	private static String imageTitle; 
	private static String zimuPath;
	private static List<MusicInfo> musicInfos = new ArrayList<MusicInfo>(); 
	private static List<VideoInfo> videoInfos = new ArrayList<VideoInfo>(); 
	private static List<PosterInfo> posterInfos = new ArrayList<PosterInfo>(); 
	private static List<MusicInfo> musicBackgroundInfos = new ArrayList<MusicInfo>(); 
	
//	public static void setKTMusicList(List<MusicInfo> music_list){
//		musicInfos = music_list;
//	}
	
//	public  void setKTMusicList(Context context,List<MusicInfo> music_list){
//		mContext = context;
//		musicInfos = music_list;
//	}
		
//	public static List<MusicInfo> getKTMusicList(Context context) { 	
//		return musicInfos;
//	}
	
	public static void setBackgroundMusicList(List<MusicInfo> music_list){
		musicBackgroundInfos = music_list;
	}
			
	public static List<MusicInfo> getBackgroundMusicList(Context context) { 	
		return musicBackgroundInfos;
	}
	
//	public  void setKTVideoList(List<VideoInfo> video_list){
//		videoInfos = video_list;
//	}
		
//	public static List<VideoInfo> getKTVideoList(Context context) { 	
//		return videoInfos;
//	}

	
	public static void setKTPosterList(List<PosterInfo> poster_list){
		posterInfos = poster_list;
	}
	
	public static List<PosterInfo> getKTPosterList() { 	
		return posterInfos;
	}

	public List<PosterInfo> getImagesList(Context context) {  
		List<PosterInfo> imageList = new ArrayList<PosterInfo>(); 
		Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
				"(_display_name like '%.jpg'or _display_name like '%.png'" +
				"or _display_name like '%.gif'or _display_name like '%.bmp')",
				null, MediaColumns.DISPLAY_NAME);  	 
		Log.i(TAG,"getImagesList : cursor.getCount()="+cursor.getCount());
		for (int i = 0; i < cursor.getCount(); i++) {    
			cursor.moveToNext();  
			imageTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME));  
			imageFilePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.DATA)); 
			PosterInfo posterinfo =new PosterInfo();
			posterinfo.setPosterTitle(imageTitle);
			posterinfo.setPosterPath(imageFilePath);
			imageList.add(posterinfo); 
		} 
		return imageList;
	}
	/** 
	* ���ڴ���ݿ��в�ѯ�������Ϣ��������List���� 
	*  
	* @return 
	*/  
	public static List<MusicInfo> getKTMusicInfos(Context context) {  
	Cursor cursor = context.getContentResolver().query(  
	MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, 
	"(_display_name like '%.mp3'or _display_name like '%.wma'" +
	"or _display_name like '%.wav')", 
	null,MediaColumns.DISPLAY_NAME);  
	
	List<MusicInfo> musicInfos = new ArrayList<MusicInfo>();  	
		for (int i = 0; i < cursor.getCount(); i++) {  
			cursor.moveToNext();  
			MusicInfo musicInfo = new MusicInfo();  
			int id = cursor.getInt(cursor  
					.getColumnIndex(BaseColumns._ID));             
			String title = cursor.getString((cursor   
					.getColumnIndex(MediaColumns.TITLE)));           
			String artist = cursor.getString(cursor  
					.getColumnIndex(AudioColumns.ARTIST));            
			if(artist.equals("<unknown>")){artist="未知";}
			long duration = cursor.getLong(cursor  
					.getColumnIndex(AudioColumns.DURATION));          
			long size = cursor.getLong(cursor  
					.getColumnIndex(MediaColumns.SIZE));             
			String url = cursor.getString(cursor  
					.getColumnIndex(MediaColumns.DATA));                
			int isMusic = cursor.getInt(cursor  
					.getColumnIndex(AudioColumns.IS_MUSIC));         
			if (isMusic != 0) {     
				musicInfo.setMusicId(id);  
				musicInfo.setMusicTitle(title);  
				musicInfo.setMusicArtist(artist);  
				musicInfo.setMusicDuration(duration);  
				musicInfo.setMusicSize(size);  
				musicInfo.setMusicUrl(url);  
				musicInfos.add(musicInfo);  
				//setKTMusicList(musicInfos);
			}  
		}  
		return musicInfos;  
	}  

	/** 
	* ��List���������Map������ݣ�ÿһ��Map������һ�����ֵ��������� 
	* @param mp3Infos 
	* @return 
	*/  
	public static List<HashMap<String, String>> getMusicMaps(  
		List<MusicInfo> musicInfos) {  
		List<HashMap<String, String>> musiclist = new ArrayList<HashMap<String, String>>();  
		for (Iterator iterator = musicInfos.iterator(); iterator.hasNext();) {  
			MusicInfo mp3Info = (MusicInfo) iterator.next();  
			HashMap<String, String> map = new HashMap<String, String>();  
			map.put("title", mp3Info.getMusicTitle());  
			map.put("Artist", mp3Info.getMusicArtist());  
			map.put("duration", formatTime(mp3Info.getMusicDuration()));  
			map.put("size", String.valueOf(mp3Info.getMusicSize()));  
			map.put("url", mp3Info.getMusicUrl());  
			musiclist.add(map);  
		}  
		return musiclist;  
	}  

	/** 
	* ��ʽ��ʱ�䣬������ת��Ϊ��:���ʽ 
	* @param time 
	* @return 
	*/  
	public static String formatTime(long time) {  
		String min = time / (1000 * 60) + "";  
		String sec = time % (1000 * 60) + "";  
		if (min.length() < 2) {  
			min = "0" + time / (1000 * 60) + "";  
		} else {  
			min = time / (1000 * 60) + "";  
		}  
		if (sec.length() == 4) {  
			sec = "0" + (time % (1000 * 60)) + "";  
		} else if (sec.length() == 3) {  
			sec = "00" + (time % (1000 * 60)) + "";  
		} else if (sec.length() == 2) {  
			sec = "000" + (time % (1000 * 60)) + "";  
		} else if (sec.length() == 1) {  
			sec = "0000" + (time % (1000 * 60)) + "";  
		}  
		return min + ":" + sec.trim().substring(0, 2);  
	}
	
	public static String videoFormatTime(long time) { 
		SimpleDateFormat formatter_total = new SimpleDateFormat("HHmmss");  
		formatter_total.setTimeZone(TimeZone.getTimeZone("GMT+00:00")); 
		hms_total = formatter_total.format(time).trim();  
		String str1 = hms_total.substring(0, 2);
		String str2 = hms_total.substring(2,4);
		String str3 = hms_total.substring(4);
		String newStr = str1+":"+str2+":"+str3;
		return newStr; 
	}
	
	public static String formatDuration(final Context context, int duration) {
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format(context.getString(R.string.details_ms), m, s);
        } else {
            durationValue = String.format(context.getString(R.string.details_hms), h, m, s);
        }
        return durationValue;
    }
	
} 