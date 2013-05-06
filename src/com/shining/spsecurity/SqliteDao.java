package com.shining.spsecurity;



import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqliteDao {

	private static SqliteDao dao;
	
	private static SQLiteDatabase database;
	
	private StringBuilder sb=new StringBuilder();
	
	public SqliteDao(Context context,String filename) {
		SqliteDao.database = context.openOrCreateDatabase(filename,
				Context.MODE_PRIVATE, null);
	}
	
	public static SqliteDao getInstance() {
		
		return dao;
	}
	
	public static void init(Context context,String filename){
		if(dao==null)
			dao=new SqliteDao(context,filename);
	}
	
	public static void close(){
		
		database.close();
		dao=null;
	}
	
	public Boolean check(){
		
		Cursor cursor = database.rawQuery("select password from password",
				new String[] {});
	
		while(cursor.moveToNext()){
			String password=cursor.getString(0);
			System.out.println("DBPassword:"+password);
			Util.getInstance();
			sb.append("\nDBPassword:"+Util.replaceSubString(password,5));
		}
		
		cursor.close();
		
		if(sb.length()==0)
			return false;
		return true;
	}
	
	public StringBuilder getResult(){
		
		return sb;
	}

}
