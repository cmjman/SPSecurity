package com.shining.spsecurity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	private final File DATA_DIRECTORY=new File("/sdcard/test");
	
	private static final String TAG="MainActivity";
	
	private boolean result_RootCommand=false;
	
	private String[] rootCommand=new String[4];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		rootCommand[0]="find /data/data/ -name 'webview.db'|xargs tar czf /sdcard/test/db.tgz";
		rootCommand[1]="tar zxvf /sdcard/test/db.tgz -C /sdcard/test";
		
		rootCommand[2]="find /data/data/ -name '*.xml'|xargs tar czf /sdcard/test/xml.tgz";
		rootCommand[3]="tar zxvf /sdcard/test/xml.tgz -C /sdcard/test";
		
		result_RootCommand = runRootCommand(rootCommand);
	
		if(result_RootCommand){
			System.out.println("rootCommand run!");
		}

		getFileList(DATA_DIRECTORY);		
	}
	
	public static boolean runRootCommand(String[] command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su"); 
            os = new DataOutputStream(process.getOutputStream());
            for(String cmd:command){
            	 os.writeBytes(cmd+"\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(  
                    process.getInputStream()));  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                Log.d(TAG, line);  
            }  
            try {  
                br.close();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            
        } catch (Exception e) {
				Log.d(TAG, "the device is not rooted, error message: " + e.getMessage());
                return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if(process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
	
	private void getFileList(File file){

	   File[] files = file.listFiles();

	   for(int i = 0; i < files.length; i++){
		   
		   File f = files[i];
		   if(f.isFile()){
			   
			   try{
				   
			   if(f.getName().endsWith(".xml") && XMLParser(f)){
				  
				   System.out.println("XML:"+f);
				   
			   	}else if(f.getName().equals("webview.db") && DBScaner(f)){
				   	
				   	System.out.println("DB:"+f);
			   	}
			   }catch(Exception e){
				   e.printStackTrace();
			   }
		   }
		   else if(f.isDirectory()){
			   getFileList(f);
		   }
	   }
	}

	
	private Boolean XMLParser(File file) {

	    ArrayList<String> password=new ArrayList<String>();
	    Boolean result=false;
	    
		try{
		FileInputStream fileInputStream=new FileInputStream(file);	
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();
		parser.setInput(fileInputStream, "UTF-8");

		for (int i = parser.getEventType(); i != XmlPullParser.END_DOCUMENT; i = parser.next()) {

			if (i == XmlPullParser.START_TAG && parser.getName().equals("string") 
											&& parser.getAttributeValue(0).equals("password")) {
				
					String str=parser.nextText();
					password.add(str);
					System.out.println("password:"+str);
					result=true;
				}
			
		
			if(i == XmlPullParser.START_TAG && parser.getName().equals("string") 
											&& parser.getAttributeValue(0).equals("email")){
					
					String str=parser.nextText();
					System.out.println("email:"+str);
					result=true;
				}
			
			if(i == XmlPullParser.START_TAG && parser.getName().equals("string") 
					&& parser.getAttributeValue(0).equals("phone")){

					String str=parser.nextText();
					System.out.println("phone:"+str);
					result=true;
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	private Boolean DBScaner(File file){
		
		Boolean result=false;
		SQLiteDatabase database=null;
		
		try { 
        
            if (file.exists()){ 
         
            	database = SQLiteDatabase.openOrCreateDatabase(file, null); 
            }
        } catch (Exception e) { 
        	e.printStackTrace();
        } 
		
		Cursor cursor = database.rawQuery("select password from password",
				new String[] {});
		while(cursor.moveToNext()){
			String password=cursor.getString(0);
			System.out.println("DBPassword:"+password);
			result=true;
		}
		
		cursor.close();
		database.close();
		
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
