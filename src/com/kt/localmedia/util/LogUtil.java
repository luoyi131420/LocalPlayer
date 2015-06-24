/*******************************************************************
* Company:     30kt
* Description:   
* @author:    luoyi
* Create at:   2015-3-18 15:15:00
*******************************************************************/ 

package com.kt.localmedia.util;

import android.util.Log;

public class LogUtil {
    private final static int       DEBUG_NONE = 0;
	private final static int        DEBUG_ALL = 1;
	private final static int     DEBUG_COSTUM = 2;

	public final static int DEBUG_MEDIAPLAYER = 4;
	public final static int    DEBUG_SUBTITLE = 8;
	public final static int          DEBUG_UI = 16;
	public final static int       DEBUG_OTHER = 32;

	public static int mLogLevel = DEBUG_ALL;

	public static int Log(String tag, String log){
		return Log(mLogLevel, tag, log);
	}

	public static int LogUI(String tag, String log){
		return Log(DEBUG_UI, tag, log);
	}

	public static int LogPlayer(String tag, String log){
		return Log(DEBUG_MEDIAPLAYER, tag, log);
	}

	public static int LogOther(String tag, String log){
		return Log(DEBUG_OTHER, tag, log);
	}

	public static int LogE(String tag, String log, Throwable tr){
		Log.e(tag, log, tr);
		return 0;
	}

	
	public static int Log(int level, String tag, String log){
		if(DEBUG_NONE==mLogLevel){
			return -1;
		}
		switch(level){
			case DEBUG_MEDIAPLAYER:
				Log.e(tag, "<DEBUG_MEDIAPLAYER> " +log);
				break;
			case DEBUG_SUBTITLE:
				Log.e(tag, "<DEBUG_SUBTITLE> " +log);
				break;
			case DEBUG_UI:
				Log.e(tag, "<DEBUG_UI> " +log);
				break;
			case DEBUG_OTHER:
				Log.e(tag, "<DEBUG_OTHER> " +log);
				break;
			default:
				if(DEBUG_ALL==mLogLevel){
				    Log.e(tag, "<DEBUG_ALL> " +log);
				}
				break;
		}
		return 0;
	}	
}
