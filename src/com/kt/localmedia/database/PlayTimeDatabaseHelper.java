package com.kt.localmedia.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class PlayTimeDatabaseHelper extends SQLiteOpenHelper {  
	
	final String CREATE_TABLE_SQL =("CREATE TABLE  IF NOT EXISTS " + PlayTimeDatabaseConfig.TABLE_NAME + "("
									+ PlayTimeDatabaseConfig.ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
									+ PlayTimeDatabaseConfig.PLAY_TIME+" INTEGER,"
									+ PlayTimeDatabaseConfig.VIDEO_PATH+" TEXT)");
	 
	PlayTimeDatabaseHelper(Context context)   {         
		super(context, PlayTimeDatabaseConfig.DB_NAME,null, PlayTimeDatabaseConfig.DB_VERSION);          
	}
	@Override         
	public void onCreate(SQLiteDatabase db) {              
		// TODO ������ݿ�󣬶���ݿ�Ĳ���       
		db.execSQL(CREATE_TABLE_SQL);

	}              
	@Override     
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {    
		
		if(newVersion!=oldVersion){			
			onDeleteTable(db);
			onCreate(db);
		}
	}      
	
	
	public void onDeleteTable(SQLiteDatabase db){
		  String sql = "DROP TABLE IF EXISTS " +  PlayTimeDatabaseConfig.TABLE_NAME;
		  db.execSQL(sql);
	 }

	@Override     
	public void onOpen(SQLiteDatabase db) {              
		super.onOpen(db);               
	}

}