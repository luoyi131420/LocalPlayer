/*
 * Copyright (C) 2009 The Rockchip Android MID Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kt.localmedia.util;

import java.io.File;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.widget.Toast;

import com.kt.localmedia.R;
public class DBUtils{
	private static final String TAG = "DBUtils";
	private static final String PREFS_NAME = "android.rk.RockVideoPlayer";
	
	 public static String[] mCols = new String[] {
			 MediaStore.Video.Media.DISPLAY_NAME,
			 MediaStore.Video.Media.DURATION,
			 MediaStore.Video.Media.MIME_TYPE,
			 MediaStore.Video.Media.SIZE,
			 MediaStore.Video.Media._ID,
			 MediaStore.Video.Media.DATA,
			 MediaStore.Video.Media.BOOKMARK
	 };
	 
	public static ContentResolver getResolver(Context context)
	{
		return context.getContentResolver();
	}
	public static long getVideoIdByUri(Context context,Uri mUri){
		Cursor cur = getResolver(context).query(mUri, mCols, null, null, null);
		if(cur != null && cur.getCount() >= 0){
			cur.moveToFirst();
			long video_id = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
			return video_id;
		}
		return Long.MIN_VALUE;
	}
	public static Uri getTrueUri(Context context,Uri mUri){
		LogUtil.LogOther(TAG,"Enter getTrueUri() mUri.getPath():" + mUri.getPath());
		if(mUri == null || mUri.getPath() == null){
			return mUri;
		}
		if(mUri.toString().startsWith("file://")){
			Uri uri = MediaStore.Video.Media.getContentUri("external");
			Cursor cur = getResolver(context).query(uri, mCols, null, null, null);
			String path = mUri.getPath();
			if(cur.moveToFirst()){
				while(!cur.isAfterLast()){
					if(cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)).equals(path)){
						Uri result = ContentUris.withAppendedId(uri,cur.getInt(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
						cur.close();
						cur = null;
						return result;
					}else{ 
						cur.moveToNext();
					}
				}
			}
			if(cur != null){
				cur.close();
				cur = null;
			}
		}		
		return mUri;	
	}

	public static Uri getTrueUri(Context context,String path){
		Uri uri = MediaStore.Video.Media.getContentUri("external");
		Cursor cur = getResolver(context).query(uri, mCols, null, null, null);
		if((null!=cur)&&cur.moveToFirst()){
			while(!cur.isAfterLast()){
				if(cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)).equals(path)){
					Uri result = ContentUris.withAppendedId(uri,cur.getInt(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
					cur.close();
					cur = null;
					return result;
				}else{ 
					cur.moveToNext();
				}
			}
		}

		if(cur != null){
			cur.close();
			cur = null;
		}

		return null;	
	}

	public static Cursor getCurrentCursor(Context context,Uri currenturi)
	{
		//LogUtil.LogOther(TAG,"Enter getCurrentCursor() currenturi:" + currenturi.toString());
		Uri uri = MediaStore.Video.Media.getContentUri("external");
		//LogUtil.LogOther(TAG,"Enter getCurrentCursor() uri:" + uri.toString());
		Cursor cur = getResolver(context).query(uri, mCols, null, null, null);
		//LogUtil.LogOther(TAG,"Enter getCurrentCursor() cur:" + cur);
		
		if((cur!=null)&&cur.moveToFirst()){
			while(!cur.isAfterLast()){
				if(currenturi.equals(ContentUris.withAppendedId(uri,cur.getInt(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID))))){
					return cur;
				}else{ 
					cur.moveToNext();
				}
			}
		}
		if(cur != null){
			cur.close();
			cur = null;
		}
		return null;		
	}

	public static Cursor getCurrentCursor(Context context,String path)
	{
		Uri uri = MediaStore.Video.Media.getContentUri("external");
		//LogUtil.LogOther(TAG,"getCurrentCursor(), path = "+path);
		Cursor cur = getResolver(context).query(uri, mCols, null, null, null);
		String videoPath = null;
		if(cur != null)
		{
			if(cur.moveToFirst())
			{
				while(!cur.isAfterLast())
				{
					videoPath = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
					if(videoPath.equals(path))
					{
						return cur;
					}
					else
					{ 
						cur.moveToNext();
					}
				}
			}
		}


		return null;
	}
	
	public static Cursor getCurrentCursor(Context context,int position){
		Cursor cur = null;
		ContentResolver resolver = context.getContentResolver();
		Uri uri = MediaStore.Video.Media.getContentUri("external");
		cur = resolver.query(uri,mCols,null,null,null);
		if(cur == null)
		{
			return null;
		}
		if(cur.moveToFirst()){	 
			 while(!cur.isAfterLast()){
				 cur.moveToPosition(position + 1);			 
			 }
		}
		return cur;		
	}

	public static Cursor getNextCursor(Context context,Uri currenturi)
	{
		//LogUtil.LogOther(TAG,"Enter getNextCursor()");
		Cursor cur = getCurrentCursor(context,currenturi);
		LogUtil.LogOther(TAG,"Current cur = " + cur);
		if(cur == null)
			return null;
		else
		{
			if(cur.isLast())
			{
				cur.moveToFirst();
				return cur;
			}						
			else
			{
				cur.moveToNext();
				return cur;
			}
		}
	}

	public static Cursor getPrevCursor(Context context,Uri currenturi)
	{
		//LogUtil.LogOther(TAG,"Enter getPrevCursor()");
		Cursor cur = getCurrentCursor(context,currenturi);
		LogUtil.LogOther(TAG,"Current cur = " + cur);
		if(cur == null)
			return null;
		else
		{			
			if(cur.isFirst())
			{
				cur.moveToLast();
				return cur;
			}
			else
			{
				cur.moveToPrevious();
				return cur;
			}
		}
	}

	public static boolean checkVideoAvailable(Context context,Cursor cur){
		//Uri uri = MediaStore.Video.Media.getContentUri("external");
		//Uri videouri =ContentUris.withAppendedId(uri,cur.getInt(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
		String videofile = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
		LogUtil.LogOther(TAG,"videofile = " + videofile);
		File file = new File(videofile);
		if (file.exists()){
			cur.close();
			return true;
		}else{
			cur.close();
			return false;
		}
	}
	
	public static boolean checkVideoAvailable(Context context,String videofile){
		LogUtil.LogOther(TAG,"videofile = " + videofile);
		File file = new File(videofile);
		if (file.exists()){
			return true;
		}else{
			return false;
		}
	}

	public static void deleteCurrentVideo(Context context,Uri uri) 
	{
		try {
			getResolver(context).delete(uri, null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public static void deleteViedoFile(Context context,String fileName)
//	{
//	    LogUtil.LogOther(TAG," deleteViedoFile fileName = " + fileName);
//	    
//		if((fileName.indexOf("sdcard"))!= -1){
//		    LogUtil.LogOther(TAG,"going here !!!!!!!!!!!!!!!!!!!!!!");
//			File f = new File(fileName);
//			
//			if(f.exists() && f.isFile()){
//                LogUtil.LogOther(TAG, fileName + " is exist and is a file");
//                boolean result = false;
//                result = f.delete();
//                if (!result) {
//                    LogUtil.LogOther(TAG,"Could not delete " + fileName);
//                    //(Toast.makeText(context, R.string.alert_cantdelete, Toast.LENGTH_SHORT)).show();
//                    (Toast.makeText(context, R.string.alert_cantdelete, Toast.LENGTH_SHORT)).show();
//                }else
//                    (Toast.makeText(context, R.string.alert_deletesuccess, Toast.LENGTH_SHORT)).show();
//            }else{
//                LogUtil.LogOther(TAG, fileName + " not exist!!!");
//                (Toast.makeText(context, R.string.alert_deletesuccess, Toast.LENGTH_SHORT)).show();
//            }
//			
//		}else{
//		    LogUtil.LogOther(TAG,"going here @@@@@@@@@@@@@@@@@@@@@@@@@");
//			File f = new File(fileName);
//			if(f.exists() && f.isFile()){
//				LogUtil.LogOther(TAG, fileName + " is exist and is a file");
//				boolean result = false;
//				result = f.delete();
//		        if (!result) {
//		            LogUtil.LogOther(TAG,"Could not delete " + fileName);
//		            //(Toast.makeText(context, R.string.alert_cantdelete, Toast.LENGTH_SHORT)).show();
//		            (Toast.makeText(context, R.string.alert_cantdelete, Toast.LENGTH_SHORT)).show();
//		        }else
//		        	(Toast.makeText(context, R.string.alert_deletesuccess, Toast.LENGTH_SHORT)).show();
//			}else{
//				LogUtil.LogOther(TAG, fileName + " not exist!!!");
//				(Toast.makeText(context, R.string.alert_deletesuccess, Toast.LENGTH_SHORT)).show();
//			}
//		}
//	}

	 public static boolean isMediaScannerScanning(Context context) {
	        boolean result = false;
	        Cursor cursor = getResolver(context).query(MediaStore.getMediaScannerUri(),
	                new String [] { MediaStore.MEDIA_SCANNER_VOLUME }, null, null, null);
	        if (cursor != null) {
	            if (cursor.getCount() == 1) {
	                cursor.moveToFirst();
	                result = "external".equals(cursor.getString(0));
	            }
	            cursor.close();
	        }
	        LogUtil.LogOther(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> isMediaScannerScanning returning " + result);
	        return result;
	 }

	 public static Cursor query(Context context, Uri uri, String[] projection,
	            String selection, String[] selectionArgs, String sortOrder) {
	    	LogUtil.LogOther(TAG,"Enter query()");
	    	try {
	            ContentResolver resolver = context.getContentResolver();
	            if (resolver == null) {
	                return null;
	            }
	            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
	         } catch (UnsupportedOperationException ex) {
	            return null;
	        }
	    }

	 public static int FindPosition(Context context,Uri mUri){
		 int pos = -1;	
		 if (mUri == null)
			 return pos;
		 Uri uri = MediaStore.Video.Media.getContentUri("external");
		 Cursor cur = query(context,uri, mCols,null, null, null);
		 if(cur != null){
			 cur.moveToFirst();
			 while(!cur.isAfterLast()){
				 Uri tempUri = Uri.withAppendedPath(uri, cur.getString(cur.getColumnIndex(MediaStore.Video.Media._ID)));
				 LogUtil.LogOther(TAG,"mUri/tempUri = " + mUri + "/" + tempUri);
				 if(mUri.equals(tempUri)){
					 return cur.getPosition();
				 }
				 else cur.moveToNext();
			 }
		 }
		 cur.close();
		 return pos;
	 }

	 public interface Def{
		 public final int MODE_SYSTEM = 1;
		 public final int MODE_USER = 2;
		 public final int VOLUME_RAISE = 3;
		 public final int VOLUME_LOWER = 4;
	 }

	 public static void setBacklight(Context context,int mode){
		 setBacklight(context,mode,getBacklight(context,mode));
	 }

	 public static void setOriBacklight(Context context,int mode){
		 storeOriBacklight(context,mode);
		 setOriBacklight(context,mode,getOrigBacklight(context,mode));
	 }

	 public static void storeOriBacklight(Context context,int mode){
		 switch(mode){
		 case Def.MODE_SYSTEM:
			 storeOriSysBacklight(context,getOriSysBacklight(context));
			 break;
		 case Def.MODE_USER:
			 storeOriSysBacklight(context,getOriSysBacklight(context));
			 break;
		 }
		 
	 }

	 public static void storeOriSysBacklight(Context context,int value){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);   
		SharedPreferences.Editor editor = settings.edit();   
		editor.putInt("sysBacklight", value);   			
		 editor.commit();
		 Settings.System.putInt(context.getContentResolver(), 
	                Settings.System.SCREEN_BRIGHTNESS,value);
	 }

	 public static int getOriSysBacklight(Context context){
		 int value = 0;
		 try{		    
		     value = Settings.System.getInt(context.getContentResolver(), 
		                Settings.System.SCREEN_BRIGHTNESS);
	     }
	     catch( SettingNotFoundException e){
	    	 e.printStackTrace();
	     }
	     if(value == 0){
	    	 value = getBacklightValue(context,Def.MODE_SYSTEM); 
	     }
	     return value;
	 }
	 
	 public static void setOriBacklight(Context context,int mode,int value){
		 setBacklightValue(context,mode,value);
		 storeBacklightValue(context,mode,value);
	 }
	 
	 public static void setBacklight(Context context,int mode,int value){
		setBacklightValue(context,mode,value);
		storeBacklightValue(context,mode,value);
	 }
	 public static void setBacklightValueAndMode(Context context,int mode,int appmode,int value){
		 SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);   
			SharedPreferences.Editor editor = settings.edit(); 
		 switch(mode){
		 case Def.MODE_SYSTEM:
		 {
			 setBrightness(context,value);
			 editor.putInt("sysBacklight", value);
			 editor.putInt("appMode",appmode);
			 editor.commit();
			 Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,value);  
		 }
		 break;
		 case Def.MODE_USER:
		 {
			 setBrightness(context,value);
			 editor.putInt("userBacklight", value);
			 editor.putInt("appMode",appmode);
			 editor.commit();
		 }
		 break;
		 }
	 }
	 
	 public static int getBacklightMode(Context context){
		 int mode = 0;
		 SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
		 mode = settings.getInt("appMode", 0);   
		 return mode;
	 }

	 public static int getBacklight(Context context,int mode){
		 return getBacklightValue(context,mode);
	 }

	 public static void setBacklightValue(Context context,int mode,int value){
		 switch(mode){
		 case Def.MODE_SYSTEM:
		 {
			 setBrightness(context,value);
			 storeBacklightValue(context,Def.MODE_SYSTEM,value);
			 LogUtil.LogOther(TAG,"setBacklightValue value=" + value);
			 Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,value);  
		 }
		 break;
		 case Def.MODE_USER:
		 {
			 setBrightness(context,value);
		 }
		 break;
		 }
	 }

	 private static void setBrightness(Context context,int brightness)
	 {
	  
	 }

	 private static void storeBacklightValue(Context context,int mode,int lightness){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);   
		SharedPreferences.Editor editor = settings.edit();   
		 switch(mode){
		 case Def.MODE_SYSTEM:
		 {
			 editor.putInt("sysBacklight", lightness);   			
			 editor.commit();
			 Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,lightness);
		 }
		 break;
		 case Def.MODE_USER:
		 {
			 editor.putInt("userBacklight", lightness);   			
			 editor.commit();
		 }
		 break;
		 }
	 }

	 private static int getBacklightValue(Context context,int mode){
		 int lightness = 0;
		 SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
		 switch(mode){
		 case Def.MODE_SYSTEM:
		 {
			lightness = getOrigBacklight(context,Def.MODE_SYSTEM);
		 }
		 break;
		 case Def.MODE_USER:
		 {
			lightness = settings.getInt("userBacklight", 0);     
			if(lightness == 0){
				 lightness = getOrigBacklight(context,Def.MODE_SYSTEM);
			 }
		 }
		 break;
		 }
		return lightness;
	}

	 private static int getOrigBacklight(Context context,int mode){
		 int value = 0;
		 switch(mode){
		 case Def.MODE_SYSTEM:
		 {
			 try{		    
			     value = Settings.System.getInt(context.getContentResolver(), 
			                Settings.System.SCREEN_BRIGHTNESS);
			     return value;
		     }
		     catch( SettingNotFoundException e){
		    	 e.printStackTrace();
		    	 LogUtil.LogOther(TAG,"Get System info error ");
		     }
		 }
		 break;
		 case Def.MODE_USER:
		 {
			 value = getBacklightValue(context,Def.MODE_USER);
		 }
		 break;
		 }
		 
	     return value;
	 }

	 public static void setbackBacklight(Context context,int mode){
		 switch(mode){
		 case Def.MODE_SYSTEM:
		 {
			 setBacklightValue(context,Def.MODE_SYSTEM,getBacklightValue(context,Def.MODE_SYSTEM));
		 }
		 break;
		 case Def.MODE_USER:
		 {
			 setBacklightValue(context,Def.MODE_USER,getBacklightValue(context,Def.MODE_SYSTEM));
			 //setBacklightValue(context,Def.MODE_SYSTEM,getBacklightValue(context,Def.MODE_SYSTEM));
		 }
		 break;
		 }
	 }

	 public static void adjustSystemVolume(Context context,int direction){
	 	if(direction==Def.VOLUME_LOWER){direction=AudioManager.ADJUST_LOWER;}
		if(direction==Def.VOLUME_RAISE){direction=AudioManager.ADJUST_RAISE;}
	 	AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		manager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, direction,AudioManager.FLAG_PLAY_SOUND);
	 }

	 public static int getSystemVolume(Context context){
	 	 AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		 return manager.getStreamVolume(AudioManager.STREAM_SYSTEM);
	 }

	 public static int getMaxSystemVolume(Context context){
		 AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		 return manager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
	 }

	 public static void adjustMusicVolume(Context context,int direction){
	 	if(direction==Def.VOLUME_LOWER){direction=AudioManager.ADJUST_LOWER;}
		if(direction==Def.VOLUME_RAISE){direction=AudioManager.ADJUST_RAISE;}
	 	AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction,AudioManager.FLAG_PLAY_SOUND);
	 }

	 public static int getMusicVolume(Context context){
		 AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		 return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
	 }
	 public static int getMaxMusicVolume(Context context){
		 AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		 return manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	 }
 
	 public static void setScreenValue(Context context, int mode){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit(); 
		editor.putInt("VideoscreenSize", mode);   			
		editor.commit();
	 }
	 
	 public static int getScreenValue(Context context){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
		return settings.getInt("VideoscreenSize", 3);	
	 }

	 public static int getVideoCount(Context context){
		 int count = 0;
		 Uri uri = MediaStore.Video.Media.getContentUri("external");
		 Cursor cur = query(context,uri, mCols,null, null, null);
		 if(cur != null){
			 cur.moveToFirst();
			 count = cur.getCount();			 
		 }
		 if(cur != null){
			 cur.close();
			 cur = null;
		 }
		 
		return count;
		 
	 }
	 
	 public static String getVideoPath(Context context, Uri uri){
		 String path = null;
		 if(uri == null)
			 return null;
		 
		 Cursor cur = DBUtils.getCurrentCursor(context, uri);
		 if(cur == null)
			 return path;
		 else{
			 try {
				path = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 
		 LogUtil.LogOther(TAG,"...........................getvideopath:" + path);
		 cur.close();
		 return path;
	 }
	 
	 public static String getPath(Context context,Uri mUri){
			if(mUri == null || mUri.getPath() == null){
				return null;
			}
			if(mUri.toString().startsWith("file://")){
				Uri uri = MediaStore.Video.Media.getContentUri("external");
				Cursor cur = context.getContentResolver().query(uri, mCols, null, null, null);
				String path = mUri.getPath();
				if(cur == null){
					return null;
				}
				if(cur.moveToFirst()){
					while(!cur.isAfterLast()){
						String temp = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
						if(temp.equals(path)){
							cur.close();
							cur = null;
							return temp.trim();
						}else{ 
							cur.moveToNext();
						}
					}
				}
				if(cur != null){
					cur.close();
					cur = null;
				}
			}else{
				Cursor cur = context.getContentResolver().query(mUri, mCols, null, null, null);
				if(cur == null){
				    return null;
				}
				if(cur.moveToFirst()){
					return cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
				}
				cur.close();
				cur = null;
			}
			return null;	
		}
	 
	 public static void storeSubtitleAudioTrackValue(Context context,String key,int value){
			SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);   
			SharedPreferences.Editor editor = settings.edit();   
			editor.putInt(key, value);   			
			editor.commit();
	 }
	 
	 public static int getSubtitleAudioTrackValue(Context context,String key){
			SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);   
			return settings.getInt(key, 0);   			
	 }
	 public static boolean isSeviceWorked(Context context,String serviceName)  
	{  
		ActivityManager myManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);  
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager.getRunningServices(30);  
		for(int i = 0 ; i<runningService.size();i++)  
		{  
			if(runningService.get(i).service.getClassName().toString().equals(serviceName))  
			{
				return true;  
			}  
		}  
		return false;  
	}
}
