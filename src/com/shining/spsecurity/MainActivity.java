package com.shining.spsecurity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.LinkedList;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	

	private final File DATA_DIRECTORY=new File("/sdcard/test/data/data");
	
	private static final String TAG="MainActivity";
	
	private boolean result_RootCommand=false;
	
	private SqliteDao dao;
	
	private String[] rootCommand=new String[1];
	
	private StringBuilder returnString=new StringBuilder();
	
	private TextView text;
	
	private Button button_scan;
	
	private static int count_op=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		text=(TextView)findViewById(R.id.log);
		button_scan=(Button)findViewById(R.id.button_scan);
		
		button_scan.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				rootCommand[0]="find data/data/ -name 'webview.db' -o -name '*.xml'|cpio -dmpv sdcard/test";
			
				ScanTask scanTask=new ScanTask();
				scanTask.execute(rootCommand); 
			}
		});
	}
	
	private class ScanTask extends  AsyncTask<String, Void,Boolean>{

	
		protected Boolean doInBackground(String... params) {
			
			result_RootCommand = runRootCommand(params);
			
			if(result_RootCommand){
				System.out.println("rootCommand run!");
			}
			
			long t3=System.currentTimeMillis();
			
			getFileList_op(DATA_DIRECTORY);
			
			long t4=System.currentTimeMillis();
			
			Log.v(TAG, "getFileList_op Time:"+(t4-t3));
			
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
	
	private void showFileInfo_op(File f){
		
		 try{
			   Log.v(TAG, "Now Scanning:"+f);
			   
		   if(f.getName().endsWith(".xml") && XMLParser(f)){
			  
			   Log.v(TAG,"XML:"+f);
			   returnString.append("\nXML:"+f);
			   count_op++;
	
		   	}else if(f.getName().equals("webview.db") && DBScaner(f)){
			   	
		   		Log.v(TAG,"DB:"+f);
			   	returnString.append("\nDB:"+f);
			   	count_op++;
		   	}
		   }catch(Exception e){
			   e.printStackTrace();
		   }
	}
	
	private void getFileList_op(File file){
	
		  File[] files = file.listFiles();
		  
		  LinkedList<File> list=new LinkedList<File>();

		   for(int i = 0; i < files.length; i++){
			   
			   File f = files[i];
			   if(f.isFile()){
				   
				   showFileInfo_op(f);
			   }
			   else if(f.isDirectory()){
				   
				   list.add(f);
			   }
		   }
		   
		   while(!list.isEmpty()){
			   
			  File f=(File) list.removeFirst();
			  
			  if(f.isDirectory()){
				   
				 files=f.listFiles();
				   
				   if(files==null)
					   continue;
				   
				   for(int i=0;i<files.length;i++){
					   
					   if(files[i].isDirectory()){
						   
						   	list.add(files[i]);
					   }else{
						   showFileInfo_op(files[i]);
					   }
				   }
			   }else{
				   showFileInfo_op(f);
			   }
		   }
		   Log.v(TAG, "Count_op:"+ count_op);
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
	
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
