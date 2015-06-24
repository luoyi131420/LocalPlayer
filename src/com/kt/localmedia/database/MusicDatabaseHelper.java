package com.kt.localmedia.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class MusicDatabaseHelper extends SQLiteOpenHelper {  
	
	final String CREATE_TABLE_SQL =("CREATE TABLE  IF NOT EXISTS " + MusicDatabaseConfig.TABLE_NAME + "("
									+ MusicDatabaseConfig.ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
									+ MusicDatabaseConfig.MUSIC_ID+" INTEGER,"
									+ MusicDatabaseConfig.MUSIC_TITLE+" TEXT,"
									+ MusicDatabaseConfig.MUSIC_PATH+" TEXT)");
	 
	MusicDatabaseHelper(Context context)   {         
		super(context, MusicDatabaseConfig.DB_NAME,null, MusicDatabaseConfig.DB_VERSION);          
	}
	@Override         
	public void onCreate(SQLiteDatabase db) {              
		// TODO ������ݿ�󣬶���ݿ�Ĳ���       
		db.execSQL(CREATE_TABLE_SQL);

	}              
	@Override     
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {    
		
		if(newVersion!=oldVersion){			
			onDeleteTable(db);//��ɾ��ɰ汾
			onCreate(db);//�����°�
			//db.execSQL(CREATE_TABLE_SQL);
		}
	}      
	
	
	public void onDeleteTable(SQLiteDatabase db){
		  //String sql = "DELETE FROM " + DatabaseConfig.TABLE_NAME +";";
		  String sql = "DROP TABLE IF EXISTS " +  MusicDatabaseConfig.TABLE_NAME;
		  db.execSQL(sql);
		  //dbHelper.free();
	 }

	@Override     
	public void onOpen(SQLiteDatabase db) {              
		super.onOpen(db);               
		// TODO ÿ�γɹ�����ݿ�����ȱ�ִ��     
	}

}