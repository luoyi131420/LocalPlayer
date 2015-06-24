package com.kt.localmedia.video;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.database.PlayTimeDatabaseControl;
import com.kt.localmedia.util.AutoSize;
import com.kt.localmedia.util.ConfigUtil;
import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.CustomProgressDialog;
import com.kt.localmedia.util.DBUtils;
import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.util.PlayerUtil;
import com.kt.localmedia.util.SaveInfo;

public class MyVideoPlayer extends SurfaceView implements OnClickListener{
	private static final String TAG = "KTPlayer";
	public MediaPlayer mediaPlayer;       
	private SeekBar SeekBar = null;
	private Timer mTimer = new Timer();   
	private TimerTask mTimerTask;
    public  boolean isvideostart=false;
    SaveInfo playtime;
    int playTime = 0;
    public VideoSettingHelper mSettingHelper=null;
    private SaveInfo playPathSave,playModeSave;
    PlayTimeDatabaseControl dbHelper;
    public CustomProgressDialog mDialog;	
    public static boolean isPlay;
    private ArrayList<String> mVideos;
	private int mIndex = 0;	
	private static boolean isResume;
	private boolean isVideoPlayed=false;
	private int mAudioChannelMode = 2;
	private int mPlayMode = 2;
	public static int framePerSec = 25;
	private static int sMaxWidth = -1;
    private static int sMaxHeight = -1;
    private int mVideoWidth;
    private int mVideoHeight;
    public int mScreenWidth = 0;
	public int mScreenHeight = 0;
	private Context mContext;
	private VideoPlayActivity mActivity;
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private OnErrorListener mOnErrorListener;
        
    public MyVideoPlayer(Context context, AttributeSet attrs)
	{
    	super(context, attrs);
    	mContext = context;
		initVideoDisplayView();
	}
      
    private MediaPlayer.OnCompletionListener mCompletionListener =
	        new MediaPlayer.OnCompletionListener()
	  {
		  public void onCompletion(MediaPlayer mp) {
			  LogUtil.LogPlayer(TAG," MediaPlayer.OnCompletionListener mp=" + mp);
			  PlayerState.setState(PlayerState.STATE_END);
			  playOver();
	     }
	  };
	  
    private MediaPlayer.OnErrorListener mErrorListener =new MediaPlayer.OnErrorListener(){
    	@Override
    	public boolean onError(MediaPlayer mp, int what, int extra) {  
			// 播放错误
    		LogUtil.LogPlayer(TAG,"mediaPlayer onError what="+what+",extra="+extra);
    		if (mediaPlayer != null) {
    			mediaPlayer.reset();
    			mediaPlayer.setScreenOnWhilePlaying(true);
            }
    		//mDialog.dismiss();
    		PlayerState.setState(PlayerState.STATE_ERROR);
    		mActivity.finish();
            Toast.makeText(mActivity, mActivity.getString(R.string.error_stop_play), Toast.LENGTH_SHORT).show(); 
            return false;
		}
	};
    
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
	        new MediaPlayer.OnVideoSizeChangedListener()
	{
		public void onVideoSizeChanged(MediaPlayer mp, int width_s, int height_s) {
			int mode = DBUtils.getScreenValue(mContext);
			LogUtil.LogPlayer(TAG, "onVideoSizeChanged mode="+mode);
			setScreenMode(mode);
 		}
	};
	
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener =
	  		  new MediaPlayer.OnSeekCompleteListener(){
	  	public void onSeekComplete(MediaPlayer mp) {
	  		// TODO Auto-generated method stub
	  		LogUtil.LogPlayer(TAG, "onSeekComplete");
	  		PlayerState.setState(PlayerState.STATE_PLAYING);
	  	}

	};
	  
	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener()
	  {
		  public void onPrepared(MediaPlayer mp) {
	            // briefly show the mediacontroller
//				String mIsffmpeg = null;
//				mIsffmpeg = SystemProperties.get("sys.ffmpeg_sf.switch", "null");	
//				LogUtil.LogPlayer(TAG," MediaPlayer.OnPreparedListener onPrepared; FFMpegFlag = "+mIsffmpeg+"; MediaPlayer = " + mp);
//				if (mIsffmpeg  != "null"){
//					int i = Integer.parseInt(mIsffmpeg);
//				}
	            if (mOnPreparedListener != null) {
	                mOnPreparedListener.onPrepared(mediaPlayer);
	            }

				mActivity.mSettingHelper.setPrimarySubtitle(0);
				mActivity.mSettingHelper.setSecondarySubtitle(0);

				mPlayMode=mActivity.mSettingHelper.getRepeatMode();
				mAudioChannelMode = mActivity.mSettingHelper.getAudioChannelMode();
				LogUtil.LogPlayer(TAG, "mAudioChannelMode="+mAudioChannelMode);
				mActivity.mSettingHelper.setSoundTrack(mAudioChannelMode);
				
	            mVideoWidth = mp.getVideoWidth();
	            mVideoHeight = mp.getVideoHeight();
	            setScreenMode(DBUtils.getScreenValue(mContext));

				//Trace the State of MediaPlayer
	            LogUtil.LogPlayer(TAG, "isVideoPlayed="+isVideoPlayed);
	            //if(!isVideoPlayed){
	            checkPlayBookMark();
	            //}
//	            mediaPlayer.start();
//				PlayerState.setState(PlayerState.STATE_PLAYING);	
				//mDialog.dismiss();
				
//				VideoSetting.initAlterableStringPair();
//				setBackgroundColor(Color.TRANSPARENT);
//				startMTimer();
//				mActivity.showController();
//				mActivity.updateVideoTitle();
	        }
	  };
	  
    public MediaPlayer getMediaPlayer()
	{
		return mediaPlayer;
	}
    
    private void initVideoDisplayView(){
		LogUtil.LogUI(TAG,"Enter initVideoDisplayView()");
		playPathSave = new SaveInfo(mContext,"playPath");

		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(ConfigUtil.IS_ADD_STATUS_BAR_HEIGHT){
			dm.heightPixels+=getResources().getDimension(com.android.internal.R.dimen.navigation_bar_height);
		}
		//add by xhr , in order for subtitle setting
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;
		mVideoHeight = dm.heightPixels;
		mVideoWidth = dm.widthPixels;
		sMaxWidth = dm.widthPixels;
		sMaxHeight = dm.heightPixels;
		getHolder().addCallback(mSHCallback);
		getHolder().setFixedSize(mScreenWidth,mScreenHeight);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		//this.setOnClickListener(this);
		requestFocus();
		setBackgroundColor(Color.BLACK);
		LogUtil.LogUI(TAG,"initVideoDisplayView() Ended");
	}
    
	public long  mediaPosition;
	private long duration;
	Handler handleProgress = new Handler() {      
		@Override
		public void handleMessage(Message msg) {			
			mediaPosition += 1000;
			if(mediaPlayer!=null){
				if(isPlay&&isPlay()){
					duration = mediaPlayer.getDuration();	
				}
	
				if (duration > 0) {
					if(mediaPosition>=duration){
						mediaPosition = duration;						
					}
	
					setMusicTime(mediaPosition,duration);
					long pos = SeekBar.getMax() * mediaPosition / duration;
					SeekBar.setProgress((int) pos);
				}
			}
		};
	};

	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			LogUtil.LogPlayer(TAG, "isResume="+isResume);
			if(isResume){
				startMPlay();
				VideoSetting.initAlterableStringPair();
				setBackgroundColor(Color.TRANSPARENT);
				startMTimer();
			}else{				
				showResumeDialog(mContext,playTime);
			}
			//LogUtil.LogPlayer(TAG, "mHandler handleMessage");
			mActivity.showController();
			mActivity.updateVideoTitle();
			super.handleMessage(msg);
		}
		   
	};
		
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback(){
		 public void surfaceChanged(SurfaceHolder holder, int format,
                int w, int h){
			LogUtil.LogPlayer(TAG,"mSHCallback surfaceChanged format="+format+"; w="+w+"; h="+h);
//			mSurfaceWidth = w;
//			mSurfaceHeight = h;
		}

		public void surfaceCreated(SurfaceHolder holder){
			LogUtil.LogPlayer(TAG,"mSHCallback surfaceCreated");
			mSurfaceHolder = holder;
			LogUtil.LogPlayer(TAG,"mSHCallback surfaceCreated openVideo");
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder){
			LogUtil.LogPlayer(TAG,"mSHCallback surfaceDestoryed");
			mSurfaceHolder = null;
		}
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(mVideoWidth, mVideoHeight);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
	
	private void openVideo(){
		if (mSurfaceHolder == null) {
			return;
		}
		stopPlayback();
		try { 
			new Thread(){		
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						PlayerState.setState(PlayerState.STATE_LOADING);
						mediaPlayer = new MediaPlayer();
						//mDialog.show();
						mediaPlayer.setOnPreparedListener(mPreparedListener);
						mediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
						mediaPlayer.setOnCompletionListener(mCompletionListener);
						mediaPlayer.setOnErrorListener(mErrorListener);
						//mediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
						mediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
						
						mediaPlayer.reset(); 
						mediaPlayer.setDataSource(mVideos.get(mIndex));
						
						mediaPlayer.setDisplay(mSurfaceHolder);
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						mediaPlayer.setScreenOnWhilePlaying(true);
						
						PlayerState.setState(PlayerState.STATE_LOADING);					
						mediaPlayer.prepare();
												
						isPlay = true;	
						
					} catch (IOException ex) {
				    	LogUtil.LogE(TAG, "IOException Occured! Please Check", ex);
						return;
					} catch (IllegalArgumentException ex) {
						LogUtil.LogE(TAG, "IllegalArgumentException Occured! Please Check", ex);
						return;
					} catch(IllegalStateException ex){
						LogUtil.LogE(TAG, "IllegalStateException Occured! Please Check", ex);
						return ;
					}catch(Exception e){
						e.printStackTrace();
						return;
					}	//          Ƶ  ַ
					super.run();
				}			
			}.start();
		
		} catch (Exception e) {
		}
		mActivity.mSettingHelper.setSubtitleHandler(mActivity.mSubtitleHandler);
		mActivity.mSettingHelper.getSubtitleListFromUri();
	}

	public void stopPlayback(){
		SubContentUtil.stopDecodeFlag = true;
		//stop subtitle thread and clean subtitle content
		mActivity.mSettingHelper.releaseSubtitle();

		if (mediaPlayer != null) {
			try{
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				System.gc();
			}catch(Exception e){
			}
		}
		mActivity.mSettingHelper.releaseHelper();
		PlayerState.setState(PlayerState.STATE_END);
	}
	
	public void stopSubtitle(){
		SubContentUtil.stopDecodeFlag = true;
		mActivity.mSettingHelper.releaseSubtitle();
		mActivity.mSettingHelper.releaseHelper();
		PlayerState.setState(PlayerState.STATE_END);
	}
	
	public ArrayList<String> getSubtitleListFromUri(){
	    if(!mVideos.get(mIndex).equals("")){
		    return SubContentUtil.getSubtitleFile(mActivity,mVideos.get(mIndex));
	    }
		return null;
	}
	
	private void checkPlayBookMark(){
		dbHelper = new PlayTimeDatabaseControl(mActivity);						
		String path = mVideos.get(mIndex);
		if(!isVideoPlayed){
			if(playPathSave.getPlayPath().equals(path)){
				playTime = dbHelper.queryPlayTime(path);
				if(playTime == 0){
					isResume = true;
					mHandler.sendEmptyMessage(0);
				}else{
					isResume = false;
					mHandler.sendEmptyMessage(0);							
				}
			}else{
				isResume = true;
				mHandler.sendEmptyMessage(0);				
			}
		}else{
			isResume = true;
			mHandler.sendEmptyMessage(0);
		}
	}
	
	public void startMTimer(){
		if(mTimer!=null){
			mTimer.cancel();
			mTimer = null ;
		}
		
		if(mTimerTask!=null){
			mTimerTask.cancel();
			mTimerTask = null ;
		}
		
		if(SeekBar!=null){
			if(mTimer==null){
				mTimer = new Timer(); 
			}
			if(mTimerTask==null){
					mTimerTask = new TimerTask() {       
					@Override
					public void run() {						
					try{	
						if (mediaPlayer == null){
							return;
						}
						else if (mediaPlayer.isPlaying() && mActivity.isseekbarChange == false) {						
							handleProgress.sendEmptyMessage(0);
						}
					
					}
					catch (Exception e) {
					
					}
				}
			};
		 }
		 if(mTimer!=null&&mTimerTask!=null){
			mTimer.schedule(mTimerTask, 0, 1001);
		 }
		}
	}
	
	public void cancelMTimer(){
		if(mTimer!=null){
			mTimer.cancel();
			mTimer = null ;
		}
		
		if(mTimerTask!=null){
			mTimerTask.cancel();
			mTimerTask = null ;
		}
	}
	
	public void stopMPlay(){
		mediaPlayer.stop();
	}
	
	public void pauseMPlay(){	
		if(isPlayerPrepared()&&mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			PlayerState.setState(PlayerState.STATE_PAUSE);
			System.gc();
		}
	}
	
	public void startMPlay(){
		//if(isPlayerPrepared()) {
			mediaPlayer.start();
			PlayerState.setState(PlayerState.STATE_PLAYING);
		//}
	}
	
	public boolean isPlay(){
		if(!iskilled){
			return mediaPlayer.isPlaying();	
		}
		else{
			return false;
		}
	}
		
	private boolean isPlayerPrepared(){
		return ((mediaPlayer!=null)&&PlayerState.isPrepared());
	}
	
	public void play(){
		isPlay = false;				
		mediaPosition=0;
		mActivity.mmprogress = 0;
		
		String path = mVideos.get(mIndex);
		Log.i(TAG,"----play path="+path);
		File file = new File(path);
		if(!file.exists()){
			mActivity.finish();
            Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exist_stop_play), Toast.LENGTH_SHORT).show(); 
            return;
		}		
		isVideoPlayed = true;
		openVideo();
//		stopSubtitle();
//		mActivity.mSettingHelper.setPrimarySubtitle(0);
//		mActivity.mSettingHelper.setSecondarySubtitle(0);
//		try {
//			mediaPlayer.reset();  			
//			mediaPlayer.setDataSource(path);
//			mediaPlayer.prepare();
//			startMPlay();
//			VideoSetting.initAlterableStringPair();
//			setBackgroundColor(Color.TRANSPARENT);
//			isPlay = true;						
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		startMTimer();
		if(!mActivity.isShowController()){
			mActivity.showController();
		}else{
			mActivity.changePlayState();
		}		
		mActivity.updateVideoTitle();
//		
//		mActivity.mSettingHelper.setSubtitleHandler(mActivity.mSubtitleHandler);
//		mActivity.mSettingHelper.getSubtitleListFromUri();
	}
	
	public void playOver(){
		mActivity.hideVideoSetting();
		
//		int mode = playModeSave.getVideoPlayMode();
		int mode = mActivity.mSettingHelper.getRepeatMode();
		System.out.println("---->playMode = "+mode);
		switch(mode){
		case Constant.PLAY_MODE_REPEAT_ONE:
			break;
		case Constant.PLAY_MODE_REPEAT_ALL:
			mIndex++;  
    		if(mIndex == mVideos.size()) {  
    			mIndex = 0;  
    		}
			break;
		case Constant.PLAY_MODE_ORDER:
			mIndex++;
			if(mIndex<mVideos.size()){
				break;
			}else{
				mIndex = mVideos.size()-1;
				Toast.makeText(mActivity, mActivity.getString(R.string.play_over), Toast.LENGTH_SHORT).show(); 
				mActivity.finish();
				return;
			}		
		case Constant.PLAY_MODE_RANDOM:
			mIndex = getRandomIndex(mVideos.size()); 
			if(mIndex<=mVideos.size()){
				break;
			}else{
				return;
			}		
		}
		play();
	}
	
	public int getRandomIndex(int end) {  
		int index = (int) (Math.random() * end);  
		return index;  
	}
	
	boolean iskilled;
	public void killMPlay(){
		if(mTimer!=null)
			{mTimer.cancel();}		
		stopPlayback();
		mActivity.isseekbarChange=true;
		iskilled=true;
		isVideoPlayed=false;
	}
	
	public void nextPlay() {  
		mActivity.hideVideoSetting();
		
		//int mode = playModeSave.getVideoPlayMode();
		int mode = mActivity.mSettingHelper.getRepeatMode();
		switch(mode){
			case Constant.PLAY_MODE_REPEAT_ONE:
				Toast.makeText(mActivity, mActivity.getString(R.string.switch_other_mode), Toast.LENGTH_SHORT).show();
				break;
			case Constant.PLAY_MODE_REPEAT_ALL:
				mIndex++;
				if(mIndex >=mVideos.size()) {  
					mIndex = 0;					
				}
				play();
				break;		
			case Constant.PLAY_MODE_ORDER:
				mIndex++;
				if(mIndex <mVideos.size()) {  
					play();;
				} else {  
					mIndex = mVideos.size()-1;
					Toast.makeText(mActivity,mActivity.getString(R.string.no_next_mv), Toast.LENGTH_SHORT).show();  
				} 
				break;
			case Constant.PLAY_MODE_RANDOM:
				mIndex = getRandomIndex(mVideos.size());
				if(mIndex <=mVideos.size()) {  					
					play();
				}
				break;
		}
	}
	
	public void previousPlay() {
		mActivity.hideVideoSetting();
		
//		int mode = playModeSave.getVideoPlayMode();
		int mode = mActivity.mSettingHelper.getRepeatMode();
		switch(mode){
			case Constant.PLAY_MODE_REPEAT_ONE:
				Toast.makeText(mActivity, mActivity.getString(R.string.switch_other_mode), Toast.LENGTH_SHORT).show();
				break;
			case Constant.PLAY_MODE_REPEAT_ALL:
				mIndex--;
				if(mIndex <0){
					mIndex = mVideos.size()-1;
				}
				play();
				break;			
			case Constant.PLAY_MODE_ORDER:
				mIndex--;
				if(mIndex <0) {  
					mIndex = 0;
					Toast.makeText(mActivity, mActivity.getString(R.string.no_previous_mv), Toast.LENGTH_SHORT).show();  					
				}else {  
					play();
				}  
				break;
			case Constant.PLAY_MODE_RANDOM:
				mIndex = getRandomIndex(mVideos.size());
				if(mIndex <=mVideos.size()) { 					
					play();
				}
				break;
		}	
	}
	
	public void setMaxWh(){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(ConfigUtil.IS_ADD_STATUS_BAR_HEIGHT)
			dm.heightPixels+=mActivity.getResources().getDimension(com.android.internal.R.dimen.navigation_bar_height);
		sMaxWidth = dm.widthPixels;
		sMaxHeight = dm.heightPixels;
	}
	
	private float WHdegree = 0.0f;
	public void setScreenSize(int width,int height)
    {
		//LogUtil.LogPlayer(TAG, "setScreenSize sMaxWidth="+sMaxWidth+",sMaxHeight="+sMaxHeight);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	if((width > sMaxWidth)||(width==0)){
    		width = sMaxWidth;
    	}
    	if((height > sMaxHeight)||(height==0)){
    		height = sMaxHeight;
    	}
    	LogUtil.LogPlayer(TAG, "setScreenSize width="+width+",height="+height);  	
    	getHolder().setFixedSize(width, height);
    	WHdegree = width/(float)height;
    	mVideoWidth= width;
    	mVideoHeight = height;
    }

    public int setScreenMode(int mode){
		if(null==mediaPlayer){
			return -1;
		}
		int maxWidth = AutoSize.getInstance().getWidth();
		int maxHeight = AutoSize.getInstance().getHeight();
		int width  = mediaPlayer.getVideoWidth();
		int height = mediaPlayer.getVideoHeight();
//		LogUtil.LogPlayer(TAG, "setScreenMode maxWidth="+maxWidth+",maxHeight="+maxHeight);
//		LogUtil.LogPlayer(TAG, "setScreenMode width="+width+",height="+height);
		switch(mode){
		case 0:
			if(ConfigUtil.FULLSCREEN_MAINTIANXY){
				float ratio = (float)width/ (float)height;
				if(width > maxWidth){
					width = maxWidth;
					height = (int)(maxWidth/ratio);
				}
				if(height > maxHeight){
					height = maxHeight;
					width = (int)(height * ratio);
				}
			}
			break;
		case 1:
			if(maxWidth*9 > maxHeight*16){
				width = (int)(maxHeight/9.0f*16); height = maxHeight;
			}else{
				width = maxWidth; height=(int)(maxWidth/16.0f*9);
			}
			break;
		case 2:
			if(maxWidth*3 > maxHeight*4){
				width = (int)(maxHeight/3.0f*4); height = maxHeight;
			}else{
				width = maxWidth; height = (int)(maxWidth/4.0f*3);
			}
			break;
		case 3:
			if(mediaPlayer.getVideoWidth() == 0 || mediaPlayer.getVideoHeight() == 0){
				width = maxWidth; height = maxHeight;
				break;
			}
			
			if(ConfigUtil.FULLSCREEN_MAINTIANXY){
				if(maxWidth*height > maxHeight*width){
					width = (int)(maxHeight/(float)height*width); height = maxHeight;
				}else{
					height = (int)(maxWidth/(float)width*height); width = maxWidth; 
				}
			}else{
				width = maxWidth; height = maxHeight;
			}
			break;
		}
		LogUtil.LogPlayer(TAG, "MyVideoPlayer width="+width+",height="+height);
		setScreenSize(width, height);

		return 0;
    }
    
	private void showResumeDialog(Context context, final int playtime) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.resume_playing_title);
		builder.setMessage(String.format(
				context.getString(R.string.resume_playing_message),
				PlayerUtil.formatDuration(context, playtime / 1000)));
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		});
		
		builder.setPositiveButton(R.string.resume_playing_resume,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mediaPosition = playtime;
						startMPlay();
						mediaPlayer.seekTo(playtime);
						VideoSetting.initAlterableStringPair();
						setBackgroundColor(Color.TRANSPARENT);
						startMTimer();
						if(!mActivity.isShowController()){
							mActivity.showController();
							mActivity.updateVideoTitle();
						}else{
							mActivity.startTime();
						}					
					}
				});
		
		builder.setNegativeButton(R.string.resume_playing_restart,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startMPlay();
						VideoSetting.initAlterableStringPair();
						setBackgroundColor(Color.TRANSPARENT);
						startMTimer();
						if(!mActivity.isShowController()){
							mActivity.showController();
							mActivity.updateVideoTitle();
						}else{
							mActivity.startTime();
						}
					}
				});

		builder.setCancelable(false).show();
	}
	
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l){
		LogUtil.LogPlayer(TAG,"Enter setOnPreparedListener()");
		mOnPreparedListener = l;
	}


	public void setOnCompletionListener(OnCompletionListener l){
		mOnCompletionListener = l;
	}


	public void setOnErrorListener(OnErrorListener l){
		mOnErrorListener = l;
	}
	
    public void setDialog(CustomProgressDialog dialog){
    	mDialog = dialog;
	}
    
    public void setSeekBar(SeekBar skb){
    	SeekBar = skb;
	}
    
    public void setPlayIndexAndList(int index,ArrayList<String> list){
		if(list != null){
			mIndex = index;
			mVideos = list;
			LogUtil.LogPlayer(TAG,"setPlayIndexAndList() ,mIndex = "+mIndex+",mVideos size = "+mVideos.size());
		}
	}
   
    public void setActivity(VideoPlayActivity newactivity)
    {
    	mActivity = newactivity;
    }
    
    public void setVideoPlayed(boolean isvideoplayed){
    	isVideoPlayed = isvideoplayed;
    }
    
    private int last_time;
	public void seekMTo(int time){
		if(last_time==time){
			return;
		}

		last_time = time;
		mediaPlayer.seekTo(time);
	}
	public void setLastTime(){
		last_time = 0;
	}
	
	public void setMusicTime(long current,long total){			  
		mActivity.mCurrentTime.setText(""+PlayerUtil.videoFormatTime(current)); 	
		mActivity.mTotleTime.setText(""+PlayerUtil.videoFormatTime(total)); 
	}
	
	public String getCurrentPath(){
		return mVideos.get(mIndex);
	}
	
	public int getCurrentIndex(){
		return mIndex;
	}	
}
