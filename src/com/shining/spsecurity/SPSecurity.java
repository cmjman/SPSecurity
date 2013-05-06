package com.shining.spsecurity;

import android.app.Application;
import android.os.StrictMode;

public class SPSecurity extends Application{
	
	@Override
	public void onCreate() {
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() 
				.detectDiskReads() 
				.detectDiskWrites()
				.detectNetwork()
				.penaltyLog() 
				.build());

		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects() 
				.penaltyLog() 
				.penaltyDeath()
				.build());
		
		super.onCreate();
	}

}
