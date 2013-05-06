package com.shining.spsecurity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



public class MainActivity extends Activity {
	

	private final File DATA_DIRECTORY=new File(Environment.getExternalStorageDirectory().getPath()+
											"/SPSecurityCacheData/data/data");
	
	private static final String TAG="MainActivity";
	
	private boolean result_RootCommand=false;
	
	private SqliteDao dao;
	
	private String[] rootCommand=new String[1];
	
	
	private LinkedList<String> list_result=new LinkedList<String>();
	
	private Button button_scan;
	
	private Button button_detail;
	
	
	private ProgressDialog progressDialog;

	

	private HoloCircularProgressBar progressBar;

	
	private static int count_op=0;
	
	private static int count_sum=0;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		        
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
   
		button_scan=(Button)findViewById(R.id.button_scan);
	
		button_detail=(Button)findViewById(R.id.button_detail);
		
		progressBar = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar);
	
		button_scan.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				File su = new File("/system/xbin/su");
				if (su.exists()){
				
					File f=new File(Environment.getExternalStorageDirectory().getPath()+"/SPSecurityCacheData");
					if(!f.exists())
						f.mkdirs();
				
					rootCommand[0]="find data/data/ -name 'webview.db' -o -name '*.xml'|cpio -dmpv sdcard/SPSecurityCacheData";
				
					ScanTask scanTask=new ScanTask();
					scanTask.execute(rootCommand); 
				}else{
					Log.v(TAG, "Your device is not rooted");
				}
			}
		});
		
	
		button_detail.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				Intent intent=new Intent(getBaseContext(),DetailActivity.class);
				intent.putExtra("result", list_result);
				startActivity(intent);
				
			}
			
		});
		
		button_detail.setClickable(false);
		
	}
	
	private void animate(final HoloCircularProgressBar progressBar, final float progress,final AnimatorListener listener) {
		
		final ObjectAnimator progressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
		progressBarAnimator.setDuration(3000);

		progressBarAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(final Animator animation) {
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				progressBar.setProgress(progress);
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		progressBarAnimator.addListener(listener);
		progressBarAnimator.reverse();
		progressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});
		progressBar.setMarkerProgress(progress);
		progressBarAnimator.start();
	}
	
	private class ScanTask extends  AsyncTask<String, Void,Boolean>{

	
		protected Boolean doInBackground(String... params) {
			
			result_RootCommand = runRootCommand(params);
			
			publishProgress();
			
			if(result_RootCommand){
				System.out.println("rootCommand run!");
			}
			
			long t3=System.currentTimeMillis();
			
			getFileList_op(DATA_DIRECTORY);
			
			long t4=System.currentTimeMillis();
			
			Log.v(TAG, "getFileList_op Time:"+(t4-t3));
			
			return true;
		}
		
		protected void onProgressUpdate(Void... params) {
	         
			progressDialog=new ProgressDialog(MainActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("扫描中，请稍等！");
			progressDialog.show();
			
	    }

		protected void onPostExecute(Boolean result){
			
			if(result){
				button_detail.setClickable(true);
			
				progressDialog.cancel();
				
				System.out.println((float) count_op*1.0/count_sum);
				
				animate(progressBar,(float) 0.0,new AnimatorListener() {
	
					@Override
					public void onAnimationCancel(final Animator animation) {
					}
	
					@Override
					public void onAnimationEnd(final Animator animation) {
						animate(progressBar,(float)1.0 -(float)(count_op*0.01),this);
					}
	
					@Override
					public void onAnimationRepeat(final Animator animation) {
					}
	
					@Override
					public void onAnimationStart(final Animator animation) {
					}
				});
				
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
			   
			   count_sum++;
			   
		   if(f.getName().endsWith(".xml") && XMLParser(f)){
			  
			   Log.v(TAG,"XML:"+f);
			 
			   list_result.add(f.toString());
			  
			   count_op++;
		   	}else if(f.getName().equals("webview.db") && DBScaner(f)){
			   	
		   		Log.v(TAG,"DB:"+f);
		   		list_result.add(f.toString());
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
		   Log.v(TAG, "Count_sum:"+ count_sum + "Count_op:"+count_op);
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
					Util.getInstance();
					list_result.add(Util.replaceSubString(str,5));
					result=true;
				}
			
		
			if(i == XmlPullParser.START_TAG && parser.getName().equals("string") 
											&& parser.getAttributeValue(0).equals("email")){
				
				
					
					String str=parser.nextText();
					System.out.println("email:"+str);
				
					Util.getInstance();
					list_result.add(Util.replaceSubString(str,5));
					result=true;
				}
			
			if(i == XmlPullParser.START_TAG && parser.getName().equals("string") 
					&& parser.getAttributeValue(0).equals("phone")){
				
			

					String str=parser.nextText();
					System.out.println("phone:"+str);
					Util.getInstance();
					
					list_result.add(Util.replaceSubString(str,5));
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
            	
            	SqliteDao.init(getApplicationContext(), file.toString());
            	dao=SqliteDao.getInstance();
            	if(dao.check()){
            		
            		
            		
            	
            	
            		list_result.add(dao.getResult().toString());
            	}
            	SqliteDao.close();

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
