package com.kt.localmedia.music;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kt.localmedia.R;
import com.kt.localmedia.database.PosterDatabaseControl;
import com.kt.localmedia.music.poster.PosterInfo;
import com.kt.localmedia.music.poster.PosterManagerActivity;
import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.PlayerUtil;
import com.kt.localmedia.util.SaveInfo;


public class MusicPlayActivity extends Activity implements OnClickListener
{
	private static final String TAG = "KTMusicPlayActivity";
	private Button mPlayBtn,mPlayMode,mPreviousBtn,mNextBtn,mExitBtn,mPosterSetBtn;
	private ListView mMusicList;
	private MusicAdapter mMusicAdapter;
	public static SeekBar music_progressBar;  
	private static TextView mCurrentTime;   
	private static TextView mTotleTime;     
	public TextView mCurrentMusicTitle;    
	private ImageView mPoster;
	PosterDatabaseControl dbHelper;
	private ImageView iv;
	public static int listPosition;     
	private boolean isPlaying;
	private Bitmap bitmap;
	private PlayerReceiver playerReceiver;   
	private List<PosterInfo> posterList;
	private ViewFlipper viewFlipper = null;	  
	public Animation animation1;
	AnimationSet animationSet1;
	private static SaveInfo saveMusicPlayMode;
	public static boolean isPlayMusic;
	private ArrayList<String> mMusics;
	private int mIndex = 0;
	public static final String MUSIC_LIST = "android.intent.wmt_extra.music_list";
	public static final String MUSIC_CURRENT = "android.intent.wmt_extra.music_current";
	
	public static void setMusicTime(long current,long total){			  
		mCurrentTime.setText(""+PlayerUtil.formatTime(current)); 	
		mTotleTime.setText(""+PlayerUtil.formatTime(total)); 
	}
	
	Handler  currentTimeHandler = new Handler (){	 
		@Override
		public void handleMessage(Message msg) {  
			long currentPosition = MusicService.mediaPlayer.getCurrentPosition();
			long duration = MusicService.mediaPlayer.getDuration();
			setMusicTime(currentPosition,duration);
		super.handleMessage(msg);
		} 
	};
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.music_play_view);
		findViewById(); 
		setViewOnclickListener();			
		initViews();  
		registerReceiver();
	}
	
	private void findViewById(){
		mPlayBtn = (Button) findViewById(R.id.music_play);
		mPlayMode = (Button) findViewById(R.id.play_mode);
		mPreviousBtn = (Button) findViewById(R.id.music_previous);
		mNextBtn = (Button) findViewById(R.id.music_next);
		mExitBtn = (Button) findViewById(R.id.music_exit);	
		mPosterSetBtn = (Button) findViewById(R.id.poster_btn);
		mCurrentTime = (TextView) findViewById(R.id.current_time);
		mTotleTime = (TextView) findViewById(R.id.totle_time);
		mCurrentMusicTitle = (TextView) findViewById(R.id.current_title);
		mMusicList = (ListView) findViewById(R.id.music_list); 
		mPoster = (ImageView) findViewById(R.id.music_poster);	
		music_progressBar = (SeekBar) findViewById(R.id.music_seekbar);  
		iv =(ImageView)findViewById(R.id.iv);
		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
	}
	
	private void setViewOnclickListener() {		 
		mMusicList.setOnItemClickListener(new MusicListItemClickListener());  		
		mPlayBtn.setOnClickListener(this);		
		mPlayMode.setOnClickListener(this);			
		mPreviousBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);			
		mExitBtn.setOnClickListener(this);	
		mPosterSetBtn.setOnClickListener(this);
		music_progressBar.setOnClickListener(this);
		music_progressBar.setOnKeyListener(new SeekBarOnKeyListener()); 
		music_progressBar.setOnSeekBarChangeListener(new SeekBarChangeListener()); 
	}
	
	@Override  
	protected void onResume() {  
		super.onResume();  
		musicListThread();
		initPosterThread();
	} 
		
	private void initViews(){
		final Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mMusics = extras.getStringArrayList(MUSIC_LIST);
		} else {
			mMusics = new ArrayList<String>();
		}

		if ((mMusics != null) && (mMusics.size() > 0)) {
			mIndex = extras.getInt(MUSIC_CURRENT);

			if (mIndex > mMusics.size()) {
				mIndex = 0;
			}
		} else {
			Uri uri = intent.getData();
			if (mMusics == null) {
				mMusics = new ArrayList<String>();
			}
			mMusics.add(uri.toString());
			mIndex = 0;
		}

		playMusic();
		
		dbHelper = new PosterDatabaseControl(this);	
		isPlaying = true;  
		initPlaystate();
		saveMusicPlayMode = new SaveInfo(this,"musicPlayMode");
		showPlayMode(saveMusicPlayMode.getMusicPlayMode());
	}
	
	public void initPlaystate(){ 
		if(isPlaying){		
			 mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
		}
		else {	
			mPlayBtn.setBackgroundResource(R.drawable.play_btn_selector);
		}
	}
	
	private void registerReceiver(){         
		playerReceiver = new PlayerReceiver();  
		IntentFilter filter = new IntentFilter();  
		filter.addAction(Constant.UPDATE_TITLE);
		filter.addAction(Constant.UPDATE_INDEX);
		filter.addAction(Constant.PLAY_OVER);
		filter.addAction(Constant.PLAY_ERROR);
		filter.addAction(Constant.STOP_ACTION);
		registerReceiver(playerReceiver, filter);      
	} 
	
	Handler mHandler =new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(posterList!=null){
				posterList.clear();
				posterList=null;
			}
			posterList = dbHelper.queryPosterList();
			viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
			if(posterList.size()==0){
				posterList.clear();
				viewFlipper.setAutoStart(false);
				viewFlipper.stopFlipping();
				viewFlipper.removeAllViews();			
			}else{
				new AsyncLoadedImage(posterList.size()).executeOnExecutor(Executors.newCachedThreadPool());
			}
			super.handleMessage(msg);		
				
		}	
	};

	private void initPosterThread(){
		new Thread(){
			@Override
			public void run() {				
				mHandler.sendEmptyMessage(0);
			}
		}.start();
	}
	
	Handler mhandler =new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			mMusicList.setVisibility(View.VISIBLE);
			mMusicAdapter = new MusicAdapter(mMusics, MusicPlayActivity.this);
			mMusicList.setAdapter(mMusicAdapter);
			mMusicList.setSelection(mIndex);
			super.handleMessage(msg);		
				
		}	
	};
	
	private void musicListThread(){
		new Thread(){
			@Override
			public void run() {				
				mhandler.sendEmptyMessage(0);
			}
		}.start();
	}
	
	class AsyncLoadedImage extends AsyncTask<Object, Bitmap, Object> {  
		int Count;
		public AsyncLoadedImage(int num){
			Count = num;
		}
		 @Override  
	     protected Object doInBackground(Object... params) { 
			 for (int i = 0; i < Count; i++) {
				 try { 
					BitmapFactory.Options options = new BitmapFactory.Options();  
                    options.inSampleSize = 15;   
					bitmap=BitmapFactory.decodeFile(posterList.get(i).getPosterPath(),options);			
					if(bitmap!=null){
						publishProgress(bitmap);
						Thread.sleep(300);
					}
				 } catch (Exception e) {  
                     e.printStackTrace();  
                 }
			}
			 return null; 
		 }
		 @Override
		 protected void onProgressUpdate(Bitmap... values) {  
			 ImageView iv = new ImageView(MusicPlayActivity.this); 
			 iv.setImageBitmap(bitmap);
			 iv.setScaleType(ImageView.ScaleType.FIT_XY);
			 viewFlipper.addView(iv,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	     }
		 @Override  
	     protected void onPostExecute(Object result) {  
			 viewFlipper.setAutoStart(true);   //设置自动播放
			 viewFlipper.setFlipInterval(5000);
			 viewFlipper.setInAnimation(AnimationUtils.loadAnimation(MusicPlayActivity.this, R.anim.in_alpha));
			 viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MusicPlayActivity.this, R.anim.out_alpha));
			 if(viewFlipper.isAutoStart() && !viewFlipper.isFlipping()) {
				viewFlipper.startFlipping();
			 }
	     }
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.music_play:
			if(isPlaying){
				mPlayBtn.setBackgroundResource(R.drawable.play_btn_selector);
				//if(MusicService.mediaPlayer.isPlaying()){
					MusicService.pauseMusic();
				//}				
				isPlaying = false;  
	
			}
			else {
				mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
				MusicService.restartMusic(); 
				isPlaying = true;  		
			}
			break;
		case R.id.poster_btn:
			Intent snedIntent = new Intent();
			snedIntent.setClass(this, PosterManagerActivity.class);
			startActivity(snedIntent); 
			break;
		case R.id.music_previous:
			previousPlay();
			break;
		case R.id.music_next:
			nextPlay();
			break;
		case R.id.play_mode:	
			int playMode=saveMusicPlayMode.getMusicPlayMode();
			if(playMode==1){
				playMode = 2;
			}else if(playMode==2){
				playMode =3;
			}else if(playMode==3){
				playMode = 4;
			}else if(playMode==4){
				playMode = 1;
			}
			showPlayMode(playMode);
			break;
		case R.id.music_exit:
			exitDialog();
			break;
		}
	}
		
	public void playMusic() {  
		Intent intent = new Intent();  
		intent.setAction("com.kt.media.MUSIC_SERVICE"); 
		intent.putExtra("listPosition", mIndex); 
		intent.putStringArrayListExtra(MUSIC_LIST, mMusics);
		//intent.putExtra("MSG", MusicConstant.PlayerMag.PLAY_MAG);
		intent.putExtra("type", MusicConstant.PlayerMag.PLAY_NOT_BGMUSIC);
		intent.setClass(MusicPlayActivity.this, MusicService.class);  
		startService(intent);
		mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
		isPlaying = true;
	}
	
  
	public void previousPlay() {  
		int mode = getSavePlayMode();
		switch(mode){
			case 2:
				mIndex--;
				if(mIndex <0){
					mIndex = mMusics.size()-1;
				}
				playMusic();				 
				break;
			case 1:
			case 3:
				mIndex--;
				if(mIndex <0) {  
					mIndex = 0;
					Toast.makeText(this, getString(R.string.no_previous_ms), Toast.LENGTH_SHORT).show();  					
				}else {  
					playMusic(); 				
				}  
				break;
			case 4:
				mIndex = MusicService.getRandomIndex(mMusics.size());
				if(mIndex <=mMusics.size()) { 					
					playMusic();
				}
				break;
		}	 
	}  
	
	public void nextPlay() { 
		int mode = getSavePlayMode();
		switch(mode){
		case 2:
			mIndex++;
			if(mIndex >=mMusics.size()) {  
				mIndex = 0;					
			}
			playMusic();
			break;
		case 1:
		case 3:
			mIndex++;
			if(mIndex <mMusics.size()) {  
				playMusic();;
			} else {  
				mIndex = mMusics.size()-1;
				Toast.makeText(this, getString(R.string.no_next_ms), Toast.LENGTH_SHORT).show();  
			} 
			break;
		case 4:
			mIndex = MusicService.getRandomIndex(mMusics.size());
			if(mIndex <=mMusics.size()) {  					
				playMusic();
			}
			break;
		}
		
	}
	
	private boolean threadStart;
	public static boolean isseekbarChange,isThreadBreak;
	private class SeekBarOnKeyListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN){
				if(keyCode==22||keyCode==21){
					isseekbarChange=true;		
					if(threadStart==false){
					    threadStart=true;
					    MusicService.pauseMusic();
						startMTimeThread();
					}		
				}
			}
			else if(event.getAction()==KeyEvent.ACTION_UP){
				if(keyCode==22||keyCode==21){
					if(isThreadBreak){
						isThreadBreak = false;
						if(MusicService.mediaPlayer!=null){
							MusicService.mediaPosition = MusicService. mediaPlayer.getCurrentPosition();
						}
						currentTimeHandler.sendEmptyMessage(0);
					}
					isseekbarChange=false;	
					threadStart=false;
					MusicService.restartMusic();
					//if(!MusicService. mediaPlayer.isPlaying()){						
					mPlayBtn.setBackgroundResource(R.drawable.pause_btn_selector);
					isPlaying = true;
					//}
				}
			}
			return false;
		}
	}
	public static int mmprogress;
	private class SeekBarChangeListener implements OnSeekBarChangeListener {  
	
		@Override  
		public void onProgressChanged(SeekBar seekBar, int progress,  
		boolean fromUser) {  
			if(fromUser) {  
				if(MusicService.mediaPlayer!=null){
					mmprogress = progress * MusicService.mediaPlayer.getDuration() / seekBar.getMax();
					if(mmprogress<=0){
						MusicService.mediaPlayer.start();
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

	private class MusicListItemClickListener implements OnItemClickListener {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
			mIndex = position;  
			playMusic();    
		}  		
	} 
		
	@Override 
	public void onDestroy() {
		if(playerReceiver!=null)
			{unregisterReceiver(playerReceiver);} 
		if(posterList!=null){
			for (int i = 0; i < posterList.size(); i++) {
				if(bitmap!=null)
					{bitmap.recycle();}
			}
		}	
		super.onDestroy(); 
	}
		
	private void showPlayMode(int mode){

		switch(mode){
		case 1:
			mPlayMode.setBackgroundResource(R.drawable.mode_one_xunhuan_selector);
			Toast.makeText(MusicPlayActivity.this, getString(R.string.danqu), Toast.LENGTH_SHORT).show(); 
		    break;
		case 2:
			mPlayMode.setBackgroundResource(R.drawable.mode_xunhuan_selector);
			Toast.makeText(MusicPlayActivity.this, getString(R.string.liebiao), Toast.LENGTH_SHORT).show(); 
			break;
		case 3:
			mPlayMode.setBackgroundResource(R.drawable.mode_shunxu_selector);
			Toast.makeText(MusicPlayActivity.this, getString(R.string.shunxu), Toast.LENGTH_SHORT).show(); 
			break;	
		case 4:
			mPlayMode.setBackgroundResource(R.drawable.mode_suiji_selector);
			Toast.makeText(MusicPlayActivity.this, getString(R.string.suiji), Toast.LENGTH_SHORT).show(); 
			break;
		}	
		saveMusicPlayMode.saveMusicPlayMode(mode);
	}
	  
	/**获取保存的音乐播放模式*/
	public static int getSavePlayMode(){
		int mode = saveMusicPlayMode.getMusicPlayMode();
		return mode;
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if (keyCode == KeyEvent.KEYCODE_BACK  
				&& event.getAction() == KeyEvent.ACTION_DOWN) {  
				finish();
		}  
		return super.onKeyDown(keyCode, event);  
	}  
	
	private void exitDialog(){
		new AlertDialog.Builder(this)  
			//.setIcon(R.drawable.ic_launcher)  
			.setTitle(getString(R.string.quit))  
			.setMessage(getString(R.string.exit_music_play))  
			.setNegativeButton(getString(R.string.cancel), null)  
			.setPositiveButton(getString(R.string.ensure),  
		new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				Intent intent = new Intent();  
				intent.setAction("com.kt.media.MUSIC_SERVICE");  
				stopService(intent);
				finish();  
			}  
		}).show(); 
	}
	
	public class PlayerReceiver extends BroadcastReceiver {  
	
		@Override  
		public void onReceive(Context context, Intent intent) {  
			String action = intent.getAction();   
			if(action.equals(Constant.UPDATE_TITLE)){				
				String title = intent.getStringExtra("musictitle");				
				if(title!=null){
					if(!title.equals("")){
						mCurrentMusicTitle.setText(getString(R.string.playing)+title);
						mCurrentMusicTitle.setVisibility(View.VISIBLE);
						return;
					}else {
						mCurrentMusicTitle.setVisibility(View.GONE);
						return;
					}
				}else{
					return;
				}
			}else if(action.equals(Constant.UPDATE_INDEX)){
				mIndex = intent.getIntExtra("mIndex", 0);
				return;
			}else if(action.equals(Constant.STOP_ACTION)){
				Toast.makeText(MusicPlayActivity.this, getString(R.string.file_not_exist_stop_play), Toast.LENGTH_SHORT).show(); 				
			}else if(action.equals(Constant.PLAY_OVER)){
				Toast.makeText(MusicPlayActivity.this, getString(R.string.play_over), Toast.LENGTH_SHORT).show(); 	
			}else if(action.equals(Constant.PLAY_ERROR)){
				Toast.makeText(MusicPlayActivity.this, getString(R.string.error_stop_play), Toast.LENGTH_SHORT).show(); 
			}
			finish();
			Intent mTntent = new Intent();  
			mTntent.setAction("com.kt.media.MUSIC_SERVICE");  
			stopService(mTntent);
			
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
						if(mmprogress>0){	
							if(mmprogress==MusicService.mediaPlayer.getDuration()){
								//Log.i(TAG,"----->startMTimeThread break");
								MusicService.mediaPlayer.seekTo(mmprogress);
								currentTimeHandler.sendEmptyMessage(0);
								isThreadBreak = true;
								break;
							}else{
								MusicService.mediaPlayer.seekTo(mmprogress);
								currentTimeHandler.sendEmptyMessage(0);
							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(isseekbarChange==false){
						MusicService.mediaPosition = mmprogress;
						break;
					}
				}			
				super.run();
			}
			  
		  }.start();
	  }
}
