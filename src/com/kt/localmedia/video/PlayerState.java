package com.kt.localmedia.video;

import com.kt.localmedia.util.LogUtil;


public class PlayerState {
	public static final String TAG = "KTPlayer";
	public static final int STATE_LOADING=0xF1;
	public static final int STATE_PLAYING=0xF2; //242
	public static final int STATE_PAUSE  =0xF3;
	public static final int STATE_SEEKING=0xF4;
	public static final int STATE_ERROR  =0xF5;
	public static final int STATE_END    =0xF6;
	public static int mState = STATE_LOADING;
	public static boolean isPrepared(){
		if(mState==STATE_LOADING){
			return false;
		}
		if((mState==STATE_ERROR)||(mState==STATE_END)){
			return false;
		}
		return true;
	}
	public static boolean isSeekable(){
		//LogUtil.LogPlayer(TAG,"isSeekable state="+mState);
		if(isPrepared()&&(mState!=STATE_SEEKING)){
			return true;
		}
		return false;
	}
	
	public static int getState(){
		return mState;
	}
	
	public static void setState(int state){
		//LogUtil.LogPlayer(TAG,"setState state="+state);
		if((state>=STATE_LOADING)&&(state<=STATE_END)){
			mState = state;
		}
		switch(state){
			case STATE_LOADING:
				LogUtil.LogPlayer(TAG, "Trace State<MediaPlayer>[PlayerState.STATE_LOADING]");
				break;
			case STATE_PLAYING:
				LogUtil.LogPlayer(TAG, "Trace State<MediaPlayer>[PlayerState.STATE_PLAYING]");
				break;
			case STATE_PAUSE:
				LogUtil.LogPlayer(TAG, "Trace State<MediaPlayer>[PlayerState.STATE_PAUSE]");
				break;
			case STATE_SEEKING:
				LogUtil.LogPlayer(TAG, "Trace State<MediaPlayer>[PlayerState.STATE_SEEKING]");
				break;
			case STATE_ERROR:
				LogUtil.LogPlayer(TAG, "Trace State<MediaPlayer>[PlayerState.STATE_ERROR]");
				break;
			case STATE_END:
				LogUtil.LogPlayer(TAG, "Trace State<MediaPlayer>[PlayerState.STATE_END]");
				break;
			default:
			    break;
		}
	}
}
