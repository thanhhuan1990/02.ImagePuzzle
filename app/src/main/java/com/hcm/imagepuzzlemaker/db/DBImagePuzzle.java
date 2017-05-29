package com.hcm.imagepuzzlemaker.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBImagePuzzle {
	DatabaseHelper mHelper;

	/**
    * The database that the provider uses as its underlying data store
    */
    private static final String DATABASE_NAME = "imagepuzzle.db";
    
    /**
     * The database that the provider uses as its underlying data store
     */
 	private static final String DATABASE_TABLE_NAME = "tblResult";
 	
 	/**
     * Column name for the id of the alarm
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_ID = "id";
    
    /**
     * Column name for the requestCode of the alarm
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_TYPE = "type";
    
    public static final String COLUMN_TIME = "time";
    


	public DBImagePuzzle(Context context)
	{
		mHelper = new DatabaseHelper(context);
	}
	
	/*
	 * Insert new record to database 
	 */
	public void INSERT(int type, int time)
	{
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ").append(DATABASE_TABLE_NAME).append(" VALUES ");
		sb.append("(").append("null").append(", ");
		sb.append(type).append(", ");
		sb.append(time);
		sb.append("); ");  
		
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL(sb.toString());

		db.close();
		mHelper.close();

	}
	
	/*
	 * Query to get list of note in database
	 */
	public ArrayList<Result> query() {
		ArrayList<Result> list = new ArrayList<Result>();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor;

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM ").append(DATABASE_TABLE_NAME);
		
		cursor = db.rawQuery(sb.toString(), null);
		
		while (cursor.moveToNext()) {
			String id = cursor.getString(0);  
			int type = cursor.getInt(1);
			int time = cursor.getInt(2);
			
			list.add(new Result(Integer.parseInt(id), type, time));
		}

		cursor.close();
		mHelper.close();
		
		return list;
	}
	
	/*
	 * Clear all data in table if it's existed
	 */
	public void DROP()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM ").append(DATABASE_TABLE_NAME).append(";");
		
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL(sb.toString());
		mHelper.close();
	}
	
	class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		// Declare table with 4 column: (id - int auto increment, title - String , note - String, date - integer)
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_NAME + " ("
	                   + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
	                   + COLUMN_TYPE + " INTEGER,"
	                   + COLUMN_TIME + " INTEGER"
	                   + ");");
		}

		// Drop table if it's existed
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			StringBuffer sb = new StringBuffer();
			sb.append("DROP TABLE IF EXISTS ").append(DATABASE_NAME).append("");
			
			db.execSQL(sb.toString());
			onCreate(db);
		}
	}
}
