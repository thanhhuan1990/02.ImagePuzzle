package com.hcm.imagepuzzlemaker.util;

import android.util.Log;

import com.hcm.imagepuzzlemaker.BuildConfig;

public class ConsoleLogger {

	/****************************************************************************************************
	 * Private members
	 ***************************************************************************************************/
	private static final String 	TAG 				= "HomeGallery";
	private static boolean 			sIsEnableLog 		= BuildConfig.DEBUG;
	
/****************************************************************************************************
 * Public method
 ***************************************************************************************************/
	//------------------------------------------------------------------------------------------------
	/**
	 * Print the log when enter the function
	 * 
	 */
	public static void logEnterFunction() {
		String strTid = String.valueOf(Thread.currentThread().getId());		
		if (sIsEnableLog) {		
			Log.i(TAG, " ");
			Log.i(TAG, "=[" +strTid + "]===[ENTER]=== " + getMethodName() + "() ============================================");
		}
	}

	//------------------------------------------------------------------------------------------------
	/**
	 * Print the log when leave the function
	 */
	public static void logLeaveFunction() {
		String strTid = String.valueOf(Thread.currentThread().getId());
		if (sIsEnableLog) {
			Log.i(TAG, "=[" +strTid + "]===[LEAVE]=== " + getMethodName() + "() ============================================");
			Log.i(TAG, " ");
		}
	}
	
	//------------------------------------------------------------------------------------------------
	/**
	 * Print the message log
	 * @param msg the message log
	 */
	public static void log(String msg) {
		if (sIsEnableLog) {
			Log.i(TAG, "*   " + msg);
		}
	}
	
	//------------------------------------------------------------------------------------------------
	/**
	 * Get the current method name
	 * Use {@link getStackTrace()} function
	 * @return the name of method that call this method
	 */
	private static String getMethodName() {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[4].getMethodName();
	}
	
}
