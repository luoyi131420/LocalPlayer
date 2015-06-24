package com.kt.localmedia.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kt.localmedia.music.MusicInfo;

public class MusicDatabaseControl {
	
	private static final String TAG = "MusicDatabaseControl";
	private MusicDatabaseHelper musicDB;
	Context mContext;
	
	public MusicDatabaseControl(Context context) {
		mContext = context;
		musicDB = new MusicDatabaseHelper(context);
	}
	
	public MusicDatabaseControl() {}
	
	public boolean addMusicPath(List<String> add_path) {
		SQLiteDatabase db = musicDB.getWritableDatabase();
		ContentValues values = new ContentValues();	
		try {
			for(int i=0;i<add_path.size();i++){
				String path = add_path.get(i);
				values.put(MusicDatabaseConfig.MUSIC_PATH, path);
				db.insert(MusicDatabaseConfig.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (db.isOpen()) {
				db.close();
			}
		}
		return true;
	}
	
	public boolean deleteMusicPath(List<String> delete_path){	
		
		try {
			SQLiteDatabase db = musicDB.getReadableDatabase();
			for(int i =0;i< delete_path.size();i++){
				String path=delete_path.get(i);
				String where = MusicDatabaseConfig.MUSIC_PATH + " = ?";
				String[] whereValue ={path};
				db.delete(MusicDatabaseConfig.TABLE_NAME, where,whereValue);
			}	
			db.close();		
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void clearMusicList(){	
		List<MusicInfo> list = new ArrayList<MusicInfo>();			
		try {
			list=queryMusicList(); 
			if(list.size()>0){
				SQLiteDatabase db = musicDB.getWritableDatabase();
				db.delete(MusicDatabaseConfig.TABLE_NAME,null,null);
				db.close();
			}			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean queryMusicPath(String path){
		SQLiteDatabase db = musicDB.getReadableDatabase();
		try {
			String where = MusicDatabaseConfig.MUSIC_PATH + "=?";
			Cursor cursor = db.query(MusicDatabaseConfig.TABLE_NAME, null, where, 
					new String[] {path},null, null, null);
			cursor.moveToFirst();
			db.close();
			if(cursor!=null&&cursor.getCount()>0){
				cursor.close();
				return true;
			}
			cursor.close();		
		} catch (Exception e) {	}
		return false;
	}

	public List<MusicInfo> queryMusicList(){
		List<MusicInfo> listMusic = new ArrayList<MusicInfo>();
		SQLiteDatabase db = musicDB.getReadableDatabase();
		try {
			String[] columns = new  String[] {MusicDatabaseConfig.MUSIC_PATH}; 
			//String where = MusicDatabaseConfig.MUSIC_TITLE + "=?";
//			Cursor cursor = db.query(MusicDatabaseConfig.TABLE_NAME,columns, null, null,
//					null, null, null);
			Cursor cursor = db.rawQuery("select " + MusicDatabaseConfig.MUSIC_PATH + " from " + MusicDatabaseConfig.TABLE_NAME,null);
			Log.i(TAG,"queryMusicList：cursor.getCount()="+cursor.getCount());
			if(cursor!=null&&cursor.getCount()>0){
				while(cursor.moveToNext()){
					MusicInfo musicInfo = new MusicInfo();
					musicInfo.setMusicUrl(cursor.getString(0));
					listMusic.add(musicInfo);
				}
			}
			cursor.close();
			db.close();
		} catch (Exception e) {	}
		return listMusic;
	}
}