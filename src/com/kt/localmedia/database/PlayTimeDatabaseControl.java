package com.kt.localmedia.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PlayTimeDatabaseControl {
	
	private static final String TAG = "PlayTimeDatabaseControl";
	private PlayTimeDatabaseHelper palytimeDB;
	Context mContext;
	
	public PlayTimeDatabaseControl(Context context) {
		mContext = context;
		palytimeDB = new PlayTimeDatabaseHelper(context);
	}
		
	public void savePlayTime(String path,int time){

		SQLiteDatabase db = palytimeDB.getReadableDatabase();
		ContentValues cv = new ContentValues();
		try {
			String where = PlayTimeDatabaseConfig.VIDEO_PATH + "=?";
			Cursor cursor = db.query(PlayTimeDatabaseConfig.TABLE_NAME, null, where, 
					new String[] {path},null, null, null);
			cursor.moveToFirst();			
			if(cursor!=null&&cursor.getCount()>0){				
				cv.put(PlayTimeDatabaseConfig.PLAY_TIME, time);
				db.update(PlayTimeDatabaseConfig.TABLE_NAME, cv, where,new String[] {path});

			}else{
				cv.put(PlayTimeDatabaseConfig.VIDEO_PATH, path);
				cv.put(PlayTimeDatabaseConfig.PLAY_TIME, time);
				db.insert(PlayTimeDatabaseConfig.TABLE_NAME, null, cv);
			}
			db.close();
			cursor.close();		
		} catch (Exception e) {	}

	}

	public int queryPlayTime(String path){
		
		SQLiteDatabase db = palytimeDB.getReadableDatabase();
		try {
			String where = PlayTimeDatabaseConfig.VIDEO_PATH + "=?";
			Cursor cursor = db.query(PlayTimeDatabaseConfig.TABLE_NAME, null, where, 
					new String[] {path},null, null, null);
			cursor.moveToFirst();
			db.close();
			if(cursor!=null&&cursor.getCount()>0){
				int time = cursor.getInt(cursor.getColumnIndex(PlayTimeDatabaseConfig.PLAY_TIME));
				cursor.close();
				return time;
			}
			cursor.close();		
		} catch (Exception e) {	
			return 0;
		}
		return 0;
	}
	
	public void clearPlayTime(){	
		SQLiteDatabase db = palytimeDB.getReadableDatabase();
		
		try {
			Cursor cursor = db.query(PlayTimeDatabaseConfig.TABLE_NAME, null, null, 
					null,null, null, null);
			cursor.moveToFirst();
			//System.out.println("--clearPlayTime--->cursor.getCount() = "+cursor.getCount());
			if(cursor!=null&&cursor.getCount()>0){
				db.delete(PlayTimeDatabaseConfig.TABLE_NAME,null,null);
				db.close();
			}
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}