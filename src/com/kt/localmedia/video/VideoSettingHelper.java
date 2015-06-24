package com.kt.localmedia.video;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.DBUtils;
import com.kt.localmedia.util.DBUtils.Def;
import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.video.subtitle.SubtitleContent;

public class VideoSettingHelper{
	private final String TAG="KTPlayer";

	private SubtitleAndTrackInfo mPlayerInfo;
	private Context mContext;
	private MyVideoPlayer  mVideoView;
	private MediaPlayer mMediaPlayer;
	private int mOldSubtitleIndex=-1;
	private int mAudioChannelMode = 2;

	//menu_tems_2d3d_mode
	private int mModeLevel=0;

	//menu_items_screen_scale
	private int mScreenScaleMode=SCREEN_MODE_FULL;
	public static final int SCREEN_MODE_ORIGINAL = 0;
	public static final int SCREEN_MODE_169 = 1;
	public static final int SCREEN_MODE_43 = 2;
	public static final int SCREEN_MODE_FULL = 3;

	//menu_items_repeat_mode
	private int mRepeatMode;
//	public final int sSingle = 0;
//	public final int sRepeatOne = 1;
//	public final int sRepeatAll = 2;
//	public final int sSuiJi = 3;
	private static final String PREFS_NAME = "android.rk.RockVideoPlayer";
	
	//menu_items_subtitle
	private  ArrayList<String> mListVobsub = null;
	private  static boolean mRunningFlag = true;
	private int mScreenWidth = 1280;
	private static final int KEY_PARAMETER_GET_VIDEO_FRAME_RATE = 2001;
	private SubContentUtil.SubtitleDecoderResult mSubtitleDecoderResult = null;
	private int m2ndSubIndex =0;

	VideoSettingHelper(Context context){
		mContext = context;
		mPlayerInfo = new SubtitleAndTrackInfo() ;

		WindowManager windowManager = (WindowManager)context.getSystemService("window");
		DisplayMetrics dm = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
	}

	public void releaseHelper(){
		mMediaPlayer = null;
		if(null!=mListVobsub){
		    mListVobsub.clear();
		    mListVobsub = null;
		}
		if(null!=mPlayerInfo){
		    mPlayerInfo.clear();
		}
	}

	public void setVideoView(MyVideoPlayer view){
		mVideoView = view;
	}

	public void storeRepeatMode(int mode){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("RepeatMode", mode);
		editor.commit();
	}
	public  int getRepeatMode(){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		//int mode = settings.getInt("RepeatMode", sRepeatAll);
		int mode = settings.getInt("RepeatMode", Constant.PLAY_MODE_ORDER);
		return mode;
	}
	
	public int getScreenModeIndex(){
		return getScreenValue(mContext);
	}

	public void setScreenScale(int mode){
		//LogUtil.LogPlayer(TAG, "VideoSettingHelper setScreenScale mode="+mode);
		setScreenScale(mode,true);
	}
	
	public void setScreenScale(int mode,boolean write)
	{
		if(mVideoView!=null){
			//LogUtil.LogPlayer(TAG, "VideoSettingHelper setScreenMode mode="+mode);
			mVideoView.setScreenMode(mode);
		}
		mScreenScaleMode = mode;
		if(write){
			DBUtils.setScreenValue(mContext, mode);
		}
	}
	
	public static void setScreenValue(Context context, int mode){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit(); 
		editor.putInt("VideoscreenSize", mode);   			
		editor.commit();
	 }
	 
	 public static int getScreenValue(Context context){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
		return settings.getInt("VideoscreenSize", 3);	
	 }
	 
	public void storeAudioChannelMode(int mode){
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("AudioChannel", mode);
		editor.commit();
	}

	  public int getAudioChannelMode(){
		  SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,0);
		  int mode = settings.getInt("AudioChannel", Constant.PLAY_MODE_ORDER);
		  return mode;
	  }

	  public int setSoundTrack(int index){
		  mAudioChannelMode = index;
		  storeAudioChannelMode(mAudioChannelMode);
		  setAudioChannelTrue(mAudioChannelMode);
		  if(mAudioChannelMode == 3)
		  {			  
		  }
		  return 0;
	  }

	  private void setAudioChannelTrue(int mode)
	  {
		  AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		  if(mode == 0){
			  LogUtil.LogPlayer(TAG, "setAudioChannelTrue left");
			  mAudioManager.setDtvOutputMode(AudioManager.EnumDtvSoundMode.E_LEFT);
		  }else if(mode == 1)
		  {
			  LogUtil.LogPlayer(TAG, "setAudioChannelTrue right");
			  mAudioManager.setDtvOutputMode(AudioManager.EnumDtvSoundMode.E_RIGHT);
		  }else if (mode == 2)
		  {
			  LogUtil.LogPlayer(TAG, "setAudioChannelTrue stereo");
			  mAudioManager.setDtvOutputMode(AudioManager.EnumDtvSoundMode.E_STEREO);
		  }else
		  {
			  LogUtil.LogPlayer(TAG, "setAudioChannelTrue Surround");
			  mAudioManager.setDtvOutputMode(AudioManager.EnumDtvSoundMode.E_STEREO);
		  }
	  }



	  public int adjustVolume(boolean isInc){
		if(isInc){
			DBUtils.adjustMusicVolume(mContext, Def.VOLUME_RAISE);
		}else{
			DBUtils.adjustMusicVolume(mContext, Def.VOLUME_LOWER);
		}
		return 0;
	}

	public int getVolume(){
		return DBUtils.getMusicVolume(mContext);
	}
	
	public int setAudioTrack(int index){
		if(mPlayerInfo == null || mPlayerInfo.getTraIndexMap() == null
				 || mPlayerInfo.getTraIndexMap().size() == 0){
			 return -1;
		 }
		int realIndex = mPlayerInfo.getTraIndexMap().get(index);
		LogUtil.LogPlayer(TAG,"index="+index+"; mapped index="+realIndex);
		try{
			mMediaPlayer.selectTrack(realIndex);
			//mVideoView.showDolbyOsdByMsg();
		}catch(IllegalStateException e){
			LogUtil.LogPlayer(TAG,"setAudioTrack(): IllegalStateException: set audio track fail");
		}catch(RuntimeException e){
			LogUtil.LogPlayer(TAG,"setAudioTrack(): RuntimeException: set subtitle fail");
		}
		
		return 0;
	}

	public int setPrimarySubtitle(int index){
		int delta = 0;

		LogUtil.LogPlayer(TAG,"setPrimarySubtitle index="+index);

		//stop subtitle thread and clean subtitle content
		releaseSubtitle();

		//probe External Subtitle Track
		ArrayList<String> subtitleList = mListVobsub;
		if(subtitleList!=null){
			delta = subtitleList.size();
			if(index<subtitleList.size()){
				setInternalSubtitleVisible(0);
				
				final String subtitlePath = subtitleList.get(index);
				subtitleThreadStart(subtitlePath);
				setSecondarySubtitle(0);
				return 1;
			}
		}

		//probe Embedded Subtitle Track
		setInternalSubtitleVisible(1);
		return setEmbeddedSubtitleTrack(index, delta);
	}

	private void setInternalSubtitleVisible(int visible){
		mMediaPlayer = mVideoView.getMediaPlayer();
		if(null != mMediaPlayer){
			LogUtil.LogPlayer(TAG,"setInternalSubtitleVisible visible="+visible);
			try{
			   mMediaPlayer.setSubtitleVisible(visible);
			}catch(Exception e){
			   e.printStackTrace();
			}
		}
	}

	public synchronized int setEmbeddedSubtitleTrack(int index, int delta){
		index -= delta;
		if(mPlayerInfo == null || mPlayerInfo.getSubIndexMap() == null
				 || mPlayerInfo.getTraIndexMap().size() == 0){
			 return -1;
		}
		if(index>=mPlayerInfo.getSubIndexMap().size()){
			return -1;
		}
		int realIndex = mPlayerInfo.getSubIndexMap().get(index);
		if(null!=mMediaPlayer){
			try{
				mMediaPlayer.selectTrack(realIndex);
				mOldSubtitleIndex=index;
				return 2;
			}
			catch(IllegalStateException e){
				LogUtil.LogPlayer(TAG,"setEmbeddedSubtitleTrack(): IllegalStateException: set subtitle fail");
			}
			catch(RuntimeException e){
				LogUtil.LogPlayer(TAG,"setEmbeddedSubtitleTrack(): RuntimeException: set subtitle fail");
			}
		}
		return 0;
	}

	public int initEmbeddedSubtitleAndTrack(){
		LogUtil.LogPlayer(TAG,"initEmbeddedSubtitleAndTrack");
		mMediaPlayer = mVideoView.getMediaPlayer();
		mPlayerInfo.clear();
		if (mMediaPlayer !=null) {
			MediaPlayer.TrackInfo[] trkInfo = mMediaPlayer.getTrackInfo();
			int type = -1;
			String value = null;
			mOldSubtitleIndex = -1;
			//LogUtil.LogPlayer(TAG,"===trkInfo.length="+trkInfo.length);
			for (int i =0; i <trkInfo.length; i++) {
				type = trkInfo[i].getTrackType();
				value = trkInfo[i].getLanguage();
				//LogUtil.LogPlayer(TAG,"===type="+type);
				if (type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT 
					|| type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {					
					mPlayerInfo.addToSubtitles(value, i);
					if(mOldSubtitleIndex==-1){
						mMediaPlayer.selectTrack(i);
						mOldSubtitleIndex= i;
					}
					LogUtil.LogPlayer(TAG,"Subtitle:TIMEDTEXT(3) value = "+value+", index="+i);
				} else if (type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
					mPlayerInfo.addToTracks(value, i);
					LogUtil.LogPlayer(TAG,"AudioTrack:"+type+", value = "+value);
				}
			}
		}
		return 0;
	}

	public String[] getNonBluarySupportedSubtitles(){
		LogUtil.LogPlayer(TAG,"getNonBluarySupportedSubtitles....");
		if(null==mMediaPlayer){return null;}
		//LogUtil.LogPlayer(TAG,"getNonBluarySupportedSubtitles....1");
		//calculate count of subtitle
		int count_sub = 0;
	   	ArrayList<String> subtitleList = mListVobsub;
		Object[] objList = mPlayerInfo.getSubtitles().toArray();
		if((subtitleList!=null)&&(subtitleList.size()>0)){
			count_sub += subtitleList.size();
		}
		count_sub += objList.length;
		if(count_sub==0){
			return null;
		}
		//LogUtil.LogPlayer(TAG,"getNonBluarySupportedSubtitles....2");
		//save vobsub list
		int delta = 0;
		String[] type = new String[count_sub];
		if((subtitleList!=null)&&(subtitleList.size()>0)){
			delta = subtitleList.size();
	        for (int i = 0; i < subtitleList.size(); i++) {
				type[i] = (String)subtitleList.get(i);
				LogUtil.LogPlayer(TAG,"NonBluary Subtitles type:"+type[i]);
	        }
		}
		//LogUtil.LogPlayer(TAG,"getNonBluarySupportedSubtitles....3");
		//save embedded subtitle list
		if (objList.length>0){
			for(int i = 0;i<objList.length;i++){
				type[i + delta] = (String)objList[i];
				LogUtil.LogPlayer(TAG,"NonBluary Subtitles type:"+type[i+delta]);
			}
		}
		//LogUtil.LogPlayer(TAG,"getNonBluarySupportedSubtitles....4");
		return type;

	}

	
	public String[] getPrimarySubtitles(){
//		if((null!=mMediaPlayer)&&mRkBlurayApi.isBluray()){
//			return mRkBlurayApi.getBluarySupportedSubtitles();
//		}
		return getNonBluarySupportedSubtitles();
	}

	//Secondary-Subtitles(2nd) of External Subtitle
	public String[] getSecondarySubtitles(){
	    LogUtil.LogPlayer(TAG,"getSecondarySubtitles()");
		if(null == mSubtitleDecoderResult){return null;}
		if(null == mSubtitleDecoderResult.subtitleContentMap){return null;}
		if(0 == mSubtitleDecoderResult.subtitleContentMap.size()){return null;}

		int index = 0;
		String[] type = new String[mSubtitleDecoderResult.subtitleContentMap.size()];
		Iterator<String> keyIt = mSubtitleDecoderResult.subtitleContentMap.keySet().iterator();
		while(keyIt.hasNext()){
			String keyStr = keyIt.next();
			type[index] = keyStr;
			LogUtil.LogPlayer(TAG,"2nd_Subtitle_Key ="+keyStr);
			index++;
		}
		return type;
	}

	public void setSecondarySubtitle(int index){
		m2ndSubIndex = 0;
		if((null!=mSubtitleDecoderResult)&&(null!=mSubtitleDecoderResult.subtitleContentMap)){
			if(index<mSubtitleDecoderResult.subtitleContentMap.size()){
				m2ndSubIndex = index;
			}
		}
	}
	
	public String[] getSupportedTracks(){
		if(null==mMediaPlayer){return null;}
		Object[] typeObject = mPlayerInfo.getTrack().toArray();
		String[] type = null;

		LogUtil.LogPlayer(TAG,"getSupportedTracks() ...");
		type = new String[typeObject.length];
		for(int i=0;i<type.length;i++){
			type[i] = (String)typeObject[i];
			LogUtil.LogPlayer(TAG,"SupportedTracks type:"+type[i]);
		}
		return type;
	}

	public void getSubtitleListFromUri(){
		if(mListVobsub!=null){
			mListVobsub.clear();
			mListVobsub=null;
		}

		if(mVideoView!=null){
			mListVobsub =mVideoView.getSubtitleListFromUri();
		}
	}

	class DecodeSubtitleThread extends Thread {  
        private String mSubtitlePath = null;
		private Map<String, List<SubtitleContent>> mContentMap=null;
		private boolean mDoneDecode = false;

		public DecodeSubtitleThread(final String path){
			mSubtitlePath = path;
			mDoneDecode = false;
		}
        public void run() {
			mDoneDecode = false;
			long startTime = System.currentTimeMillis();
			SubContentUtil.stopDecodeFlag = false;
			SubContentUtil.SubtitleDecoderResult result=SubContentUtil.decodeSubtitle2(mSubtitlePath);
			mSubtitleDecoderResult.setResult(result);
			mContentMap = result.subtitleContentMap;
			if (mContentMap == null){
				LogUtil.LogPlayer(TAG,"SubContentUtil.decodeSubtitle() Fail..............");
			}
			LogUtil.LogPlayer(TAG,"SubContentUtil.decodeSubtitle ConsumedTime:" + (System.currentTimeMillis() - startTime) + " ms");
			mDoneDecode = true;
        }

		public boolean isDoneDecode(){
			return mDoneDecode;
		}

		public Map<String, List<SubtitleContent>> getContentMap(){
			return mContentMap;
		}
    }

	private static Handler mSubtitleHandler=null;
	public void setSubtitleHandler(Handler handler){
		mSubtitleHandler = handler;
	}

	private int getVideoFrameRate(){
		int frameRate = 30;

		if(PlayerState.isSeekable()==false){
			return frameRate;
		}

		try{
			String mIsffmpeg = (String)invokeStaticMethod("android.os.SystemProperties", "get", new Class[]{String.class, String.class}, "sys.ffmpeg_sf.switch", null);	

			if (mIsffmpeg==null||"".equals(mIsffmpeg)||Integer.parseInt(mIsffmpeg)==0){
				Parcel parcel = (Parcel)invokeMethod(mMediaPlayer, "getParcelParameter", new Class[]{int.class}, KEY_PARAMETER_GET_VIDEO_FRAME_RATE);
				if(parcel!=null){
					frameRate = (int)parcel.readDouble();
					parcel.recycle();
					if(frameRate==0){//we try use old interface to get frameRate
						frameRate = (Integer)invokeMethod(mMediaPlayer, "getIntParameter", new Class[]{int.class}, KEY_PARAMETER_GET_VIDEO_FRAME_RATE);
					}
				}
				if(frameRate<10){
					frameRate = 10;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//LogUtil.LogPlayer(TAG,"getVideoFrameRate() frameRate="+frameRate);
		return frameRate;

	}

	class DisplaySubtitleThread extends Thread { 
        private String mSubtitlePath = null;
		private SubtitleContent mSubtitleContent = null;
		private SubtitleContent mOldSubtitleContent = null;
		private Map<String, List<SubtitleContent>> mContentMap=null;

		public DisplaySubtitleThread(Map<String, List<SubtitleContent>> contentMap,final String path){
			mContentMap = contentMap;
			mSubtitlePath = path;
		}

        public void run() {
			// TODO Auto-generated method stub
			mRunningFlag = true;
			while(mRunningFlag){
				//wait for mediaplayer utils playing
				if(PlayerState.isSeekable()==false){
					smartSleep();
					continue;
				}

				int delayTime = 0;
				try{
				    int position = mMediaPlayer.getCurrentPosition();			
				    mSubtitleContent = SubContentUtil.get2ndSubtitleContent(position, m2ndSubIndex, mContentMap);
				}catch(Exception e){
				    e.printStackTrace();
				}
				if(mSubtitleContent != null){
					delayTime = mSubtitleContent.getSubtitleEndTime() - mSubtitleContent.getSubtitleStartTime();
				}
				
				if((mSubtitleContent!=null)&&(mOldSubtitleContent!=null)
					    &&mSubtitleContent.getSubtitleIndex()==mOldSubtitleContent.getSubtitleIndex()){
					smartSleep();
					continue;
				}

				if((mSubtitleContent != null)&&(mSubtitleDecoderResult!=null)){
					//plug-in image or text subtitle
					if(mSubtitleDecoderResult.isPictureSub){
						SubContentUtil.decodePictureSubtitle(mSubtitlePath, 
									mSubtitleContent, mSubtitleDecoderResult, mScreenWidth);
						mOldSubtitleContent = mSubtitleContent;
						sendShowSubtitleMsg(delayTime, true);   //plug-in image-subtitle
						
					}else{
						sendShowSubtitleMsg(delayTime, false);  //plug-in text-subtitle
					}
					if(mSubtitleContent.getSubtitleEndTime() == Integer.MAX_VALUE){
						cleanMessages();
					    mSubtitleHandler.sendEmptyMessage(UserMessage.MSG_HIDE_SUBTITLE_TEXT);
					}
					
				}else{
					//no plug-in subtitle
					cleanMessages();
					mSubtitleHandler.sendEmptyMessage(UserMessage.MSG_HIDE_SUBTITLE_TEXT);
				}

				smartSleep();

				if(mSubtitleContent!=null){
					mSubtitleContent.recycleSubTitleBmp();
				}
				if(mOldSubtitleContent!=null){
					mOldSubtitleContent.recycleSubTitleBmp();
				}
			}
        } 

		public void smartSleep(){
			try {
				int framePerSec = 300; //getVideoFrameRate();
				if(mSubtitlePath.endsWith(".sub")){
					Long time = framePerSec > 10? (long)framePerSec : 10;
					if(!mSubtitleDecoderResult.isPictureSub){
						time *= 10;
					}
					Thread.sleep(time);
				}else{
					Thread.sleep(500L);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void cleanMessages(){
			mSubtitleHandler.removeMessages(UserMessage.MSG_HIDE_SUBTITLE_IMAGE);
			mSubtitleHandler.removeMessages(UserMessage.MSG_HIDE_SUBTITLE_TEXT);
			mSubtitleHandler.removeMessages(UserMessage.MSG_SHOW_SUBTITLE_TEXT);
			mSubtitleHandler.removeMessages(UserMessage.MSG_LAST_SUBTITLE);
			mSubtitleHandler.removeMessages(UserMessage.MSG_SHOW_SUBTITLE_IMAGE);
		}

		public void sendShowSubtitleMsg(int delay, boolean isSubImage){
			//LogUtil.LogPlayer(TAG,"sendShowSubtitleMsg delay=" + delay + "; isSubImage=" + isSubImage);
			Message msg = new Message();
			if(isSubImage && mSubtitleContent.hasSubTitleBmp()){
				msg.what = UserMessage.MSG_SHOW_SUBTITLE_IMAGE;
				msg.obj = mSubtitleContent.getSubtitleBmp();
			}else{
				msg.what = UserMessage.MSG_SHOW_SUBTITLE_TEXT;
				msg.obj = mSubtitleContent.getSubtitleLine();
			}
			cleanMessages();
			mSubtitleHandler.sendMessage(msg);
			if(mSubtitleContent.getSubtitleEndTime() == Integer.MAX_VALUE){
				mSubtitleHandler.sendEmptyMessage(UserMessage.MSG_LAST_SUBTITLE);
			}
			if(isSubImage){
				mSubtitleHandler.sendEmptyMessageDelayed(UserMessage.MSG_HIDE_SUBTITLE_IMAGE, delay);
			}else{
				mSubtitleHandler.sendEmptyMessageDelayed(UserMessage.MSG_HIDE_SUBTITLE_TEXT, delay);
			}
		}
    } 

    public void subtitleThreadStart(final String subtitlePath) {
		LogUtil.LogPlayer(TAG,"subtitleThreadStart ... ... subtitlePath="+subtitlePath);
		
		mSubtitleDecoderResult = new SubContentUtil.SubtitleDecoderResult();
		DecodeSubtitleThread subDecodeThread = new DecodeSubtitleThread(subtitlePath);
		subDecodeThread.start();

		while(false == subDecodeThread.isDoneDecode()){
			try{
				Thread.sleep(500L);
			}catch (InterruptedException e){}
		}

		getSecondarySubtitles();
		
		if(mSubtitleDecoderResult.isSuccess && (null!= mSubtitleDecoderResult.subtitleContentMap)){
		    DisplaySubtitleThread displayThread 
				= new DisplaySubtitleThread(mSubtitleDecoderResult.subtitleContentMap, subtitlePath);
		    displayThread.start();
		    LogUtil.LogPlayer(TAG,"displayThread.start");
		}
	}

	//stop subtitle thread and clean subtitle content
	public void releaseSubtitle(){		
		mRunningFlag = false;
		
		if((null!=mSubtitleDecoderResult)&&(null!=mSubtitleDecoderResult.subtitleContentMap)){
			LogUtil.LogPlayer(TAG,"releaseSubtitle() ...stop subtitle thread and clean subtitle content");
			try{
				Thread.sleep(500L);
			}catch (InterruptedException e){}
			mSubtitleDecoderResult.subtitleContentMap.clear();
			mSubtitleDecoderResult.subtitleContentMap = null;
			mSubtitleDecoderResult = null;
		}
//		mVideoView.resumeDefaultUIResource();
	}

	public static Object invokeStaticMethod(String className, String methodName, Class<?>[] types , Object... arguments) {
		try {
			Class<?> cls = Class.forName(className);
			return invokeStaticMethod(cls, methodName, types, arguments);
		}catch (Exception ex) {
			return null;
		}
	}
	public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] types, Object... arguments) {
		try {
			Method method = cls.getMethod(methodName, types);
			return method.invoke(null, arguments);
		}catch (Exception ex) {
			return null;
		}
	}
	public static Object invokeMethod(Object obj, String methodName, Class<?>[] types, Object... arguments){
		Class<?> cls = obj.getClass();
		Method method;
		Object result = null;
		try {
			method = cls.getMethod(methodName, types);
			result = method.invoke(obj, arguments);
		} catch (Exception ex) {
		}
		return result;
	}
		
	public class SubtitleAndTrackInfo{
		private ArrayList<String> subtitle = new ArrayList<String>();
		private ArrayList<String>track = new ArrayList<String>();
		private HashMap<Integer, Integer> subIndex = new HashMap<Integer,Integer>();
		private HashMap<Integer, Integer> traIndex = new HashMap<Integer, Integer>();

		public SubtitleAndTrackInfo(){
		}
		public ArrayList<String> getSubtitles(){
			return subtitle;
		}
		public ArrayList<String> getTrack(){
			return track;
		}
		public void addToSubtitles(String value,int index){
			subIndex.put(subtitle.size(), index);
			subtitle.add(value);
		}
		public void addToTracks(String value,int index){
			traIndex.put(track.size(), index);
			track.add(value);
		}
		public HashMap<Integer, Integer> getTraIndexMap(){
			return traIndex;
		}
		public HashMap<Integer, Integer> getSubIndexMap(){
			return subIndex;
		}
		public void clear(){
			subtitle.clear();
			track.clear();
			subIndex.clear();
			traIndex.clear();
		}
	}
}
