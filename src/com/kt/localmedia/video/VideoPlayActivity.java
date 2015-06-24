package com.kt.localmedia.video;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.database.PlayTimeDatabaseControl;
import com.kt.localmedia.util.AutoSize;
import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.CustomProgressDialog;
import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.util.SaveInfo;

public class VideoPlayActivity extends Activity implements OnClickListener
														
{
	private static final String TAG = "KTPlayer";
	public static final String VIDEO_LIST = "android.intent.rk.video_list";
	public static final String VIDEO_CURRENT = "android.intent.rk.video_current";
	private ArrayList<String> mVideos;
	private int mIndex = 0; 
	private Button mPlayBtn,mPreviousBtn,mNextBtn,mSettingBtn,mPlayMode,mZimuBtn;
	private SeekBar seekBar;  
	public  TextView mCurrentTime,mTotleTime;   
	private TextView mVideoTitleView;
	private String mVideoTitle;
	private List<VideoInfo> videoInfos; 
	private SurfaceView surfaceView;
	private MyVideoPlayer myVideoPlayer; 
	private RelativeLayout bofangtiao = null;
	public  static int listPosition;    
	private boolean isPlaying;
	public boolean isseekbarChange,isThreadBreak;;
	public int mmprogress;
	private long startTime;	
	private boolean isStart;
	private boolean bofangtiaoShow;
	private boolean threadStart = false;
	public static TextView zimu_tv;
	SaveInfo zimuSave,playModeSave,playPathSave,videoSizeSave;
	PlayTimeDatabaseControl dbHelper;
	public static String videoPath;
	public CustomProgressDialog dialog;	
	private MyReceiver myReceiver;  
	public VideoSettingHelper mSettingHelper=null;
	//TextView and Message For Subtitle 
	public TextView mSubtitleText = null;
	private ImageView mSubtitleImage = null;
	public boolean decodeSubtitleFinish = false;
	private VideoSetting mVideoSetting = null;
	private VideoPlayActivity mActivity = this;
	private Bitmap mBitmapSubtitle=null;
	
	public Handler mSubtitleHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case UserMessage.MSG_LAST_SUBTITLE:
				break;
			case UserMessage.MSG_SHOW_SUBTITLE_TEXT:
				//LogUtil.LogUI(TAG,"MSG_SHOW_SUBTITLE_TEXT...");				
				int color = zimuSave.getZimuColor();
				float size = zimuSave.getZimuSize();
				mSubtitleText.setTextColor(color);
				mSubtitleText.setTextSize(size);
				mSubtitleText.setText(Html.fromHtml((String)msg.obj));
				break;
			case UserMessage.MSG_HIDE_SUBTITLE_TEXT:
				if (myVideoPlayer.isPlay()) {
					mSubtitleText.setText("");
				} else {
					this.sendEmptyMessageDelayed(UserMessage.MSG_HIDE_SUBTITLE_TEXT, 500L);
				}
				break;
			case UserMessage.MSG_SHOW_SUBTITLE_IMAGE:
				LogUtil.LogUI(TAG,"Plug-In ImageSubtitle... Image="+(Bitmap) msg.obj);
				Bitmap subImage = (Bitmap) msg.obj;
				mSubtitleImage.setImageBitmap(subImage);
				if(mBitmapSubtitle!=null&&!mBitmapSubtitle.isRecycled()){
					mBitmapSubtitle.recycle();
				}
				mBitmapSubtitle = subImage;
				break;
			case UserMessage.MSG_HIDE_SUBTITLE_IMAGE:
				if (myVideoPlayer.isPlay()) {
					mSubtitleImage.setImageBitmap(null);
				} else {
					this.sendEmptyMessageDelayed(UserMessage.MSG_HIDE_SUBTITLE_IMAGE, 500L);
				}
				if(mBitmapSubtitle!=null&&!mBitmapSubtitle.isRecycled()){
					mBitmapSubtitle.recycle();
				}
				break;
            case UserMessage.MSG_UPDATE_SUBTITLE_TEXT_UI:
                break;	
			default:
				break;
			}
		}
	};
	
	Handler  currentTimeHandler = new Handler (){	    
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(myVideoPlayer.isPlay&&myVideoPlayer.isPlay()){
				long currentPosition = myVideoPlayer. mediaPlayer.getCurrentPosition();
				long duration = myVideoPlayer.mediaPlayer.getDuration();
				myVideoPlayer.setMusicTime(currentPosition,duration);	
			}
			super.handleMessage(msg);
		}
	};

	Handler bofangtiaoHandler = new Handler(){
	    
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			hideView();
			super.handleMessage(msg);
		}	
	};
	
	private void hideView(){
		bofangtiao.setVisibility(View.GONE);
		bofangtiaoShow = false;
		mVideoTitleView.setVisibility(View.GONE);
	}
	
	private void showView(){
		showPlayMode(mSettingHelper.getRepeatMode());
		bofangtiao.setVisibility(View.VISIBLE);
		bofangtiaoShow = true;
		updateVideoTitle();
	}
	
	//MediaPlayer mediaPlayer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//dialog =  CustomProgressDialog.createDialog(VideoPlayActivity.this);
		
		setContentView(R.layout.video_play_view);
		initExtroContent();
		findViewById(); 
		setViewOnclickListener();
		initViews();
		registerReceiver();
		
	}
	
	private void getVideoImdex(){
		final Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mVideos = extras.getStringArrayList(VIDEO_LIST);
		} else {
			mVideos = new ArrayList<String>();
		}

		if ((mVideos != null) && (mVideos.size() > 0)) {
			mIndex = extras.getInt(VIDEO_CURRENT);
			if (mIndex > mVideos.size()) {
				mIndex = 0;
			}
		} else {
			Uri uri = intent.getData();
			if (mVideos == null) {
				mVideos = new ArrayList<String>();
			}
			mVideos.add(uri.toString());
			mIndex = 0;
		}
	}
	
	private void initViews(){
		Intent intent = new Intent();  
		intent.setAction("com.kt.media.MUSIC_SERVICE");  
		stopService(intent);
		
		getVideoImdex();
		
		ViewGroup mVGSetting = null;
		mVGSetting = (ViewGroup) findViewById(R.id.layout_setting);
		zimuSave = new SaveInfo(this,"zimu");
		playModeSave = new SaveInfo(this,"video");
		playPathSave = new SaveInfo(this,"playPath");
		
		myVideoPlayer = (MyVideoPlayer) findViewById(R.id.m_surface_view);
//		myVideoPlayer.setOnErrorListener(VideoPlayActivity.this);
//		myVideoPlayer.setOnCompletionListener(VideoPlayActivity.this);
		myVideoPlayer.setActivity(mActivity);
		myVideoPlayer.setSeekBar(seekBar);
		myVideoPlayer.setDialog(dialog);
		myVideoPlayer.setVideoPlayed(false);
		myVideoPlayer.setPlayIndexAndList(mIndex,mVideos);
		
		mSettingHelper = new VideoSettingHelper(VideoPlayActivity.this);		
		mSettingHelper.setVideoView(myVideoPlayer);
		mVideoSetting =  new VideoSetting(VideoPlayActivity.this, mSettingHelper, mVGSetting);	
	}
	
	private void initExtroContent(){
		int width = getWindowManager().getDefaultDisplay().getWidth(); 
		int height = getWindowManager().getDefaultDisplay().getHeight(); 

		float bar_height=getResources().getDimension(com.android.internal.R.dimen.navigation_bar_height);

		AutoSize.getInstance().setWinSize(width, height+(int)bar_height);
	}
	
	public void showController(){
		showPlayMode(mSettingHelper.getRepeatMode());		
		isseekbarChange=false;
		isPlaying = true;
		showView();
		startTime();
		initPlaystate();		
		mPlayBtn.requestFocus();
	}
	
	public void startTime(){
		isStart = true;
		startTime = System.currentTimeMillis();
		checkTimeForViews();		
	}
	
	public void changePlayState(){
		if(!isPlaying){
			mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
			isPlaying = true;		
		}
		isseekbarChange=false;
		startTime();
	}
	
	public boolean isShowController(){
		return bofangtiaoShow;
	}
	
	public void updateVideoTitle() {
		int index = myVideoPlayer.getCurrentIndex();
		int n = mVideos.get(index).length();
		int m = mVideos.get(index).lastIndexOf("/");
		mVideoTitle=mVideos.get(index).substring(m+1, n);
		mVideoTitleView = (TextView) findViewById(R.id.current_title);
		mVideoTitleView.setText(mVideoTitle);
		if(bofangtiaoShow){
			mVideoTitleView.setVisibility(View.VISIBLE);
		}else{
			mVideoTitleView.setVisibility(View.GONE);
		}
	}
		
	private void findViewById(){
		//surfaceView = (SurfaceView)findViewById(R.id.surface_view);
		bofangtiao = (RelativeLayout)findViewById(R.id.bofang_tiao);
		mPlayBtn = (Button) findViewById(R.id.video_play);
		mPreviousBtn = (Button) findViewById(R.id.video_previous);
		mNextBtn = (Button) findViewById(R.id.video_next);	
		mSettingBtn = (Button) findViewById(R.id.setting_btn);
		mPlayMode = (Button) findViewById(R.id.play_mode);
		mZimuBtn = (Button) findViewById(R.id.zimu_btn);
		mCurrentTime = (TextView) findViewById(R.id.current_time);
		mTotleTime = (TextView) findViewById(R.id.totle_time);
		seekBar = (SeekBar) findViewById(R.id.video_seekbar);  
		zimu_tv = (TextView)findViewById(R.id.zimu_tv);
		
		mSubtitleText= (TextView) findViewById(R.id.txtsubtitle);
		mSubtitleText.getPaint().setDither(true);
		mSubtitleText.getPaint().setAntiAlias(true);
		mSubtitleText.setVisibility(View.VISIBLE);
		
		mSubtitleImage = (ImageView) findViewById(R.id.imgsubtitle);
		mSubtitleImage.setVisibility(View.VISIBLE);

	}
	
	private void setViewOnclickListener() {		 	
		mPlayBtn.setOnClickListener(this);		
		mSettingBtn.setOnClickListener(this);			
		mPreviousBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);			
		mPlayMode.setOnClickListener(this);	
		mZimuBtn.setOnClickListener(this);	
		seekBar.setOnKeyListener(new SeekBarOnKeyListener()); 
		seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener()); 
	}
	
	private void registerReceiver(){
		myReceiver = new MyReceiver();  
		IntentFilter filter = new IntentFilter();  
		filter.addAction(Constant.FINISH_ACTION);
		registerReceiver(myReceiver, filter);
	}
	
	private class SeekBarOnKeyListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction()==KeyEvent.ACTION_DOWN){
				if(keyCode==22||keyCode==21){
					isseekbarChange=true;		
					isStart=false;
					if(threadStart==false){
					    threadStart=true;
						startMTimeThread();
					}		
				}
			}
			else if(event.getAction()==KeyEvent.ACTION_UP){
				if(keyCode==22||keyCode==21){
					if(isThreadBreak){
						isThreadBreak = false;
						if(myVideoPlayer.isPlay()){
							myVideoPlayer.mediaPosition = myVideoPlayer. mediaPlayer.getCurrentPosition();
						}
						currentTimeHandler.sendEmptyMessage(0);
					}
					
					isseekbarChange=false;	
					threadStart=false;
					if(!isPlaying){						
						mPlayBtn.setBackgroundResource(R.drawable.play_btn_selector);
						isPlaying = true;
					}
					startTime();
					mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
				}
			}				
				return false;
		}
	}
	
	private class SeekBarChangeListener implements OnSeekBarChangeListener {  
		@Override  
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
			if(fromUser) { 
				//if(myVideoPlayer.isPlay&&myVideoPlayer.isPlay()){
				if(myVideoPlayer.isPlay){
					mmprogress = progress * myVideoPlayer.mediaPlayer.getDuration() / seekBar.getMax();
					if(mmprogress<=0){
						myVideoPlayer.mediaPlayer.start();
						PlayerState.setState(PlayerState.STATE_PLAYING);
					}
				}
			}
		}
		
		@Override  
		public void onStartTrackingTouch(SeekBar seekBar) {  
		    
		}  
	
		@Override  
		public void onStopTrackingTouch(SeekBar seekBar) {  
		      
		} 
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if((keyCode==23)||(keyCode==21)||(keyCode==22)||(keyCode==20)||(keyCode==19)){
			if ((mPlayBtn.getId() == R.id.video_play)||(mNextBtn.getId() == R.id.video_next)
					||(mPreviousBtn.getId() == R.id.video_previous)||(mSettingBtn.getId() == R.id.setting_btn)
					||(mZimuBtn.getId() == R.id.zimu_btn)||(mPlayMode.getId() == R.id.play_mode)
					){
				startTime();
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();

		if((mVideoSetting!=null)&&(mVideoSetting.dispatchKeyEvent(event))){
			//LogUtil.LogUI(TAG,"   mVideoSetting.dispatchKeyEvent keyCode = "+keyCode);
			return true;
		}
		
		if((event.getAction()==event.ACTION_DOWN)&&onKeyDown(keyCode, event)){
			return true;
		}
		//LogUtil.LogPlayer(TAG, "dispatchKeyEvent");
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(!myVideoPlayer.isPlay){
			return true;
		}
//		LogUtil.LogPlayer(TAG, "keyCode="+keyCode);
//		if(keyCode == 7){
//			hideView();
//			showVideoSetting();	
//			return true;
//		}
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			//LogUtil.LogPlayer(TAG, "bofangtiaoShow="+bofangtiaoShow);
			if(bofangtiaoShow){
				hideView();
				return true;
			}
			
			boorMark();	
			return super.onKeyDown(keyCode, event);
		}

		if((keyCode==23)||(keyCode==21)||(keyCode==22)||(keyCode==20)||(keyCode==19)){
			if(!bofangtiaoShow){				
				if(keyCode==23){
					showView();
					mPlayBtn.requestFocus();
					return true;
				}				
				if(keyCode==22||keyCode==21){
					showView();
					seekBar.requestFocus();
					seekBar.invalidate();
					if(!myVideoPlayer.isPlay()){
						myVideoPlayer.startMPlay();
					}
				}
				return true;
			}

			if ((mPlayBtn.getId() == R.id.video_play)||(mNextBtn.getId() == R.id.video_next)
					||(mPreviousBtn.getId() == R.id.video_previous)||(mSettingBtn.getId() == R.id.setting_btn)
					||(mZimuBtn.getId() == R.id.zimu_btn)||(mPlayMode.getId() == R.id.play_mode)
					){
				isStart=false;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private void boorMark(){
		LogUtil.LogPlayer(TAG, "boorMark");
		/*记录播放时间和影片路径*/
		dbHelper = new PlayTimeDatabaseControl(this);	
		if(myVideoPlayer.isPlay()){
			playPathSave.savePlayPath(myVideoPlayer.getCurrentPath());
			int playTime = myVideoPlayer. mediaPlayer.getCurrentPosition();
			if(playTime>=myVideoPlayer. mediaPlayer.getDuration()){
				playTime = 0;
			}
			myVideoPlayer.killMPlay(); 
			this.finish();
			dbHelper.savePlayTime(myVideoPlayer.getCurrentPath(),playTime);
		}else{
			this.finish();
		}
	}
	
	public void initPlaystate(){ 
		if(isPlaying){		
			 mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
		}
		else {
			mPlayBtn.setBackgroundResource(R.drawable.play_btn_selector);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.video_play:
			if(isPlaying){	
				mPlayBtn.setBackgroundResource(R.drawable.play_btn_selector);
				if(myVideoPlayer.isPlay()){
					myVideoPlayer.pauseMPlay();
				}
				isPlaying = false;   	
			}
			else 
			{						
				mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
				if(myVideoPlayer.isPlay){
					myVideoPlayer.startMPlay();
					startTime();							
				}
				isPlaying = true;  
			}
			break;
		case R.id.video_previous:
			if(PlayerState.isPrepared()){
				myVideoPlayer.previousPlay();
			}
			break;
		case R.id.video_next:
			if(PlayerState.isPrepared()){
				myVideoPlayer.nextPlay();
			}
			break;
		case R.id.setting_btn:		
			isStopFlag = true; 
//			Intent intent = new Intent(VideoPlayActivity.this,SettingActivity.class);
//			startActivity(intent);
			hideView();
			showVideoSetting();	;	
			break;
		case R.id.play_mode:			
			//int playMode=playModeSave.getVideoPlayMode();
			int playMode=mSettingHelper.getRepeatMode();
			if(playMode==Constant.PLAY_MODE_REPEAT_ONE){
				playMode = Constant.PLAY_MODE_REPEAT_ALL;
			}else if(playMode==Constant.PLAY_MODE_REPEAT_ALL){
				playMode = Constant.PLAY_MODE_ORDER;
			}else if(playMode==Constant.PLAY_MODE_ORDER){
				playMode = Constant.PLAY_MODE_RANDOM;
			}else if(playMode==Constant.PLAY_MODE_RANDOM){
				playMode =Constant.PLAY_MODE_REPEAT_ONE;
			}
			startTime();
			showPlayMode(playMode);
			break;
		case R.id.zimu_btn:	
			isStopFlag = true;
			Intent mIntent = new Intent(VideoPlayActivity.this,ZimuSettingActivity.class);
			startActivityForResult(mIntent,0);
			break;
		}
	}
	
	private boolean isStopFlag;
	@Override  
	protected void onPause() { 
		if(!isStopFlag){
			this.finish();
		}else{
			isStopFlag = false;
		}
		super.onPause(); 
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub	
		myVideoPlayer.isPlay = false;
		myVideoPlayer.killMPlay();
		this.finish();
		//zimuSave.clearZimuSettings();
		if(myReceiver!=null)
			{unregisterReceiver(myReceiver);}
		super.onDestroy();
	}
		
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		myVideoPlayer.setMaxWh();
		LogUtil.LogPlayer(TAG, "onConfigurationChanged");
	}
	
	private void showPlayMode(int mode){

		switch(mode){
		case Constant.PLAY_MODE_REPEAT_ONE:
			mPlayMode.setBackgroundResource(R.drawable.mode_one_xunhuan_selector);
			Toast.makeText(this,R.string.danqu, Toast.LENGTH_SHORT).show(); 
		    break;
		case Constant.PLAY_MODE_REPEAT_ALL:
			mPlayMode.setBackgroundResource(R.drawable.mode_xunhuan_selector);
			Toast.makeText(this, R.string.liebiao, Toast.LENGTH_SHORT).show(); 
			break;
		case Constant.PLAY_MODE_ORDER:
			mPlayMode.setBackgroundResource(R.drawable.mode_shunxu_selector);
			Toast.makeText(this, R.string.shunxu, Toast.LENGTH_SHORT).show(); 
			break;	
		case Constant.PLAY_MODE_RANDOM:
			mPlayMode.setBackgroundResource(R.drawable.mode_suiji_selector);
			Toast.makeText(this, R.string.suiji, Toast.LENGTH_SHORT).show(); 
			break;
		}
		
		//playModeSave.saveVideoPlayMode(mode);
		mSettingHelper.storeRepeatMode(mode);
	}
		
	public void showVideoSetting(){
		hideView();
		if(null!=mVideoSetting){
		    mVideoSetting.showSetting();
		}
	}

	public void hideVideoSetting(){
		if(null!=mVideoSetting){
		   mVideoSetting.hideSetting();
		}
	}
	
	//拖动时间条
	  private void startMTimeThread(){
		  new Thread(){
		
			@Override
			public void run() {
				// TODO Auto-generated method stub
			 while(true){
					
					try {
						Thread.sleep(100);
						if(myVideoPlayer.isPlay&&mmprogress>0){							
							if(mmprogress==myVideoPlayer.mediaPlayer.getDuration()){
								if(!myVideoPlayer.isPlay())
									{myVideoPlayer.startMPlay();}
								//PlayerState.setState(PlayerState.STATE_SEEKING);
								myVideoPlayer.mediaPlayer.seekTo(mmprogress);								
								currentTimeHandler.sendEmptyMessage(0);
								isThreadBreak = true;
								break;
							}else{
								PlayerState.setState(PlayerState.STATE_SEEKING);
								myVideoPlayer.seekMTo(mmprogress);
								//myVideoPlayer.mediaPlayer.seekTo(mmprogress);
								currentTimeHandler.sendEmptyMessage(0);
							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(isseekbarChange==false){
						myVideoPlayer.mediaPosition=mmprogress;
						if(!myVideoPlayer.isPlay())
							{myVideoPlayer.startMPlay();}						
						myVideoPlayer.seekMTo(mmprogress);
						//myVideoPlayer.mediaPlayer.seekTo(mmprogress);
						currentTimeHandler.sendEmptyMessage(0);
						myVideoPlayer.setLastTime();
						PlayerState.setState(PlayerState.STATE_PLAYING);
						break;
					}
				}			
				super.run();
			}
			  
		  }.start();
	  }

	  
	  private void checkTimeForViews(){
			new Thread(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
				
					while(true){
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
								// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.out.println("---->isStart="+isStart);
						if(isStart){
							if(!myVideoPlayer.isPlay()){
								//System.out.println("---->break 1;");
								break;
							}
							long mtime = System.currentTimeMillis();
							if(mtime-startTime>7000){								
								bofangtiaoHandler.sendEmptyMessage(0);					
								break;
							}
						}else{
							//System.out.println("---->break 2;");
							break;
						}
					}
					
				super.run();
			}
				
		}.start();
	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data)  
	{  			
		if(0==requestCode&&1==resultCode)  
		{  
//			if(!MyZimu.VIDEOPATH.equals("")){
//				new Thread(){
//					@Override
//					public void run() {
//						myZimu.getCurrentList();
//						super.run();
//					}				
//				}.start();
//				
//			}
		} 

		super.onActivityResult(requestCode, resultCode, data);  
	}	
	
	public class MyReceiver extends BroadcastReceiver {  
		@Override  
		public void onReceive(Context context, Intent intent) {  
			String action = intent.getAction(); 
			Log.i(TAG,"++++++>action="+action);
			if(action.equals(Constant.FINISH_ACTION)){ 
				finish();
			}
		}  
	} 
}