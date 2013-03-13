package com.shining.spsecurity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	

	private final File DATA_DIRECTORY=new File("/sdcard/test");
	
	private static final String TAG="MainActivity";
	
	private boolean result_RootCommand=false;
	
	private SqliteDao dao;
	
	private String[] rootCommand=new String[1];
	
	private StringBuilder returnString=new StringBuilder();
	
	private TextView text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		text=(TextView)findViewById(R.id.log);
		
		rootCommand[0]="find data/data/ -name 'webview.db' -o -name '*.xml'|cpio -dmpv sdcard/test";
		
		ScanTask scanTask=new ScanTask();
		scanTask.execute(rootCommand); 
	}
	
	private class ScanTask extends  AsyncTask<String, Void,Boolean>{

	
		protected Boolean doInBackground(String... params) {
			
			result_RootCommand = runRootCommand(params);
			
			if(result_RootCommand){
				System.out.println("rootCommand run!");
			}
			long t1=System.currentTimeMillis();
			
			getFileList(DATA_DIRECTORY);
			
			long t2=System.currentTimeMillis();
			
			Log.v(TAG, "getFileList Time:"+(t2-t1));
			
			return true;
		}
		
		protected void onPostExecute(Boolean result){
			
			if(result){
				text.setText(returnString);
			}
		}
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
 
            BufferedReader br = new BufferedReader(new InputStreamReader(  
                    process.getErrorStream()));  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                Log.d(TAG, line);  
                System.out.println(line);
            }  
            
            process.waitFor();
     
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
				   Log.v(TAG, "Now Scanning:"+f);
				   
			   if(f.getName().endsWith(".xml") && XMLParser(f)){
				  
				   System.out.println("XML:"+f);
				   returnString.append("\nXML:"+f);
		
			   	}else if(f.getName().equals("webview.db") && DBScaner(f)){
				   	
				   	System.out.println("DB:"+f);
				   	returnString.append("\nDB:"+f);
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
					returnString.append("\npassword:"+Util.getInstance().replaceSubString(str,5));
					result=true;
				}
			
		
			if(i == XmlPullParser.START_TAG && parser.getName().equals("string") 
											&& parser.getAttributeValue(0).equals("email")){
					
					String str=parser.nextText();
					System.out.println("email:"+str);
					returnString.append("\nemail:"+Util.getInstance().replaceSubString(str,5));
					result=true;
				}
			
			if(i == XmlPullParser.START_TAG && parser.getName().equals("string") 
					&& parser.getAttributeValue(0).equals("phone")){

					String str=parser.nextText();
					System.out.println("phone:"+str);
					returnString.append("\nphone:"+Util.getInstance().replaceSubString(str,5));
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
		try { 
        
            if (file.exists()){ 
            	
            	dao.init(getApplicationContext(), file.toString());
            	dao=SqliteDao.getInstance();
            	if(dao.check())
            		returnString.append(dao.getResult());
            	dao.close();

            }
        } catch (Exception e) { 
        	e.printStackTrace();
        } 
		return result;
	}
	


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
