package com.kt.localmedia.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class PosterDatabaseHelper extends SQLiteOpenHelper {  
	
	final String CREATE_TABLE_SQL =("CREATE TABLE  IF NOT EXISTS " +PosterDatabaseConfig.TABLE_NAME + "("
									+PosterDatabaseConfig.ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
									+PosterDatabaseConfig.POSTER_ID+" INTEGER,"
									+PosterDatabaseConfig.POSTER_TITLE+" TEXT,"
									+PosterDatabaseConfig.POSTER_PATH+" TEXT)" );
									//+PosterDatabaseConfig.MUSIC_PATH+" TEXT)");
	 
	PosterDatabaseHelper(Context context)   {         
		super(context,PosterDatabaseConfig.DB_NAME,null,PosterDatabaseConfig.DB_VERSION);          
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
			//db.execSQL(CREATE_TABLE_SQL);
		}
	}      
	
	
	public void onDeleteTable(SQLiteDatabase db){
		  //String sql = "DELETE FROM " + DatabaseConfig.TABLE_NAME +";";
		  String sql = "DROP TABLE IF EXISTS " + PosterDatabaseConfig.TABLE_NAME;
		  db.execSQL(sql);
		  //dbHelper.free();
	 }

	@Override     
	public void onOpen(SQLiteDatabase db) {              
		super.onOpen(db);               
		// TODO ÿ�γɹ�����ݿ�����ȱ�ִ��     
	}

}