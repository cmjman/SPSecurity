package com.shining.spsecurity;

public class Util {
	
	private static Util util;

	public Util(){
		
	}
	
	public static Util getInstance(){
		
		return util;
	}
	
	public static String replaceSubString(String str,int n){
		
		String sub="";
	
		try {
			
			sub = str.substring(0, str.length()-n);
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<n;i++){
                sb=sb.append("*");         
            }
		    sub+=sb.toString();
		    } catch (Exception e) {
		         e.printStackTrace();
		    }
		return sub;
	}
	
}
