package com.kt.localmedia.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kt.localmedia.music.poster.PosterInfo;

public class PosterDatabaseControl {
	
	private static final String TAG = "PosterDatabaseControl";
	private PosterDatabaseHelper posterDB;
	Context mContext;
	
	public PosterDatabaseControl(Context context) {
		mContext = context;
		posterDB = new PosterDatabaseHelper(context);
	}
	
	public PosterDatabaseControl() {}
	
	public boolean addPosterPath(String path){

		SQLiteDatabase db = posterDB.getReadableDatabase();
		ContentValues cv = new ContentValues();
		try {
			System.out.println("----> savePosterPath path="+path);
			cv.put(PosterDatabaseConfig.POSTER_PATH, path);
			db.insert(PosterDatabaseConfig.TABLE_NAME, null, cv);
			db.close();

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

	public boolean deletePosterPath(List<String> delete_path){	
		
		try {
			SQLiteDatabase db = posterDB.getReadableDatabase();
			for(int i =0;i< delete_path.size();i++){
				String path=delete_path.get(i);
				String where = PosterDatabaseConfig.POSTER_PATH + " = ?";
				String[] whereValue ={path};
				db.delete(PosterDatabaseConfig.TABLE_NAME, where,whereValue);
			}	
			db.close();	
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void clearPosterList(){	
		List<PosterInfo> list = new ArrayList<PosterInfo>();			
		try {
			list=queryPosterList(); 
			if(list.size()>0){
				SQLiteDatabase db = posterDB.getWritableDatabase();
				db.delete(PosterDatabaseConfig.TABLE_NAME,null,null);
				db.close();
			}			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean queryPosterPath(String path){
		SQLiteDatabase db = posterDB.getReadableDatabase();
		try {
			String where = PosterDatabaseConfig.POSTER_PATH + "=?";
			Cursor cursor = db.query(PosterDatabaseConfig.TABLE_NAME, null, where, 
					new String[] {path},null, null, null);
			cursor.moveToFirst();
			db.close();
			if(cursor!=null&&cursor.getCount()>0){
				cursor.close();
				return true;
			}	
		} catch (Exception e) {	}
		return false;
	}
	
	public List<PosterInfo> queryPosterList(){
		List<PosterInfo> listPoster = new ArrayList<PosterInfo>();
		SQLiteDatabase db = posterDB.getReadableDatabase();
		try {
			String[] columns = new  String[] {PosterDatabaseConfig.POSTER_PATH}; 
			Cursor cursor = db.rawQuery("select " + PosterDatabaseConfig.POSTER_PATH + " from " + PosterDatabaseConfig.TABLE_NAME,null);
			Log.i(TAG,"===queryPosterList-->cursor.getCount()="+cursor.getCount());
			if(cursor!=null&&cursor.getCount()>0){
				while(cursor.moveToNext()){
					PosterInfo posterInfo = new PosterInfo();
					//posterInfo.setPosterPath(cursor.getString(0));
					posterInfo.setPosterPath(cursor.getString(cursor.getColumnIndex(PosterDatabaseConfig.POSTER_PATH)));
					listPoster.add(posterInfo);
				}
			}
			cursor.close();
			db.close();
		} catch (Exception e) {	}
		return listPoster;
	}
}