package com.kt.localmedia.video;


public class UserMessage{
	public static final int MSG_SHOW_PROGRESS = 0x0005;
	public static final int MSG_HIDE_PROGRESS = 0x0006;
	public static final int MSG_SHOW_SUBTITLE_TEXT = 0x0010;
	public static final int MSG_HIDE_SUBTITLE_TEXT = 0x0011;
	public static final int MSG_SHOW_SUBTITLE_IMAGE = 0x0012;
	public static final int MSG_HIDE_SUBTITLE_IMAGE = 0x0013;
	public static final int MSG_RELEASE_LOCK = 0x00014;
	public static final int MSG_LAST_SUBTITLE = 0x00020;
    public static final int MSG_UPDATE_SUBTITLE_TEXT_UI = 0x00021;
	public static final int MSG_SHOW_DOBLY_OSD = 0x00030;
	public static final int MSG_HIDE_DOBLY_OSD = 0x00031;
	public static final int MSG_SHOW_WIN_ERROR = 0x00032;
	public static final int MSG_HIDE_WIN_ERROR = 0x00033;
	
	public static final int MSG_PLAY_NEXT_VIDEO = 0x00040;
	public static final int MSG_EXIT_ACTIVITY = 0x00041;
	public static final int MSG_RESET_UI = 0x00042;
	public static final int MSG_HIDE_SETTING_OSD = 0x00043;
}
