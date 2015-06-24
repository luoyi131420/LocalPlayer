package com.kt.localmedia.music;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.PlayerUtil;

public class MusicService extends Service implements OnCompletionListener,
		OnPreparedListener, OnErrorListener {
	private static final String TAG = "KTMusicService";
	public static MediaPlayer mediaPlayer = null;
	private String bgPath;
	private static boolean isPause;
	private List<MusicInfo> musicList;
	private int MSG;
	private Timer mTimer = new Timer();
	private TimerTask mTimerTask;
	private static int type;
	private static int bgIndex;
	private ArrayList<String> mMusics;
	private int mIndex = 0;
	public static final String MUSIC_LIST = "android.intent.wmt_extra.music_list";
	public static final String MUSIC_CURRENT = "android.intent.wmt_extra.music_current";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "onCreate");
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelMTimer();
		isError = false;
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			//MSG = intent.getIntExtra("MSG", -1);
			mMusics = intent.getStringArrayListExtra(MUSIC_LIST);
			//if (MSG == MusicConstant.PlayerMag.PLAY_MAG) {
				type = intent.getIntExtra("type", -1);
				if (type == MusicConstant.PlayerMag.PLAY_NOT_BGMUSIC) {
					mIndex = intent.getIntExtra("listPosition", -1);
					playMusic();
				}	
				if (type == MusicConstant.PlayerMag.PLAY_BGMUSIC) {
					bgIndex = intent.getIntExtra("listPosition", -1);
					playBGMusic();
				}
			//}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void playBGMusic() {
		musicList = PlayerUtil.getBackgroundMusicList(MusicService.this);
		if (musicList.size() <= 0)
			return;
		MusicInfo music = musicList.get(bgIndex);
		bgPath = music.getMusicUrl();
		File file = new File(bgPath);
		if (!file.exists()) {
			Intent mTntent = new Intent();
			mTntent.setAction("com.kt.media.MUSIC_SERVICE");
			stopService(mTntent);
			return;
		}
		startBgPlay();
	}

	public void startBgPlay() {

		new Thread() {
			@Override
			public void run() {
				MusicInfo music = musicList.get(bgIndex);
				String path = music.getMusicUrl();;
				//Log.i(TAG,"---startBgPlay path="+path);
				bgPlay(path);
				super.run();
			}
		}.start();

	}
	//synchronized
	public void bgPlay(String path){
//		//Log.i(TAG,"---bgPlay path="+path);
//		MusicInfo music = musicList.get(bgIndex);
//		String path2 = music.getMusicUrl();;;
//		//Log.i(TAG,"---bgPlay path2="+path2);
//		if(!path.equals(path2)){
//			//Log.i(TAG,"---path is not equals path2");
//			return;
//		}
//		if (mediaPlayer != null) {
//			mediaPlayer.release();
//			mediaPlayer = null;
//			//Log.i(TAG,"--->mediaPlayer stop");
//		}
//	
//		if (mediaPlayer == null) {
//			//Log.i(TAG,"--->new MediaPlayer");
//			mediaPlayer = new MediaPlayer();
//			mediaPlayer.setOnPreparedListener(MusicService.this);
//			mediaPlayer.setOnCompletionListener(MusicService.this);
//			mediaPlayer.setOnErrorListener(MusicService.this);
//		}
		try {
			if(mediaPlayer!=null){
				//Log.i(TAG,"mediaPlayer reset");
				mediaPlayer.reset();
				//Log.i(TAG,"mediaPlayer setDataSource");
				mediaPlayer.setDataSource(path);
				//Log.i(TAG,"mediaPlayer prepare");
				mediaPlayer.prepare();
				//Log.i(TAG,"mediaPlayer start");
				mediaPlayer.start();
			}
		} catch (IllegalStateException e) {
			//Log.i(TAG, "---->catch 1");
			bgPlay(path);
			e.printStackTrace();
		} catch (IOException e) {
			//Log.i(TAG, "---->catch 2");
			bgPlay(path);
			e.printStackTrace();
		}
	}
	
	public void playMusic() {
		if (mMusics.size() <= 0)
			return;
		mediaPosition = 0;
		MusicPlayActivity.mmprogress = 0;

		String path = mMusics.get(mIndex);
		//Log.i(TAG,"playMusic path：" + path);
		File file = new File(path);
		if (!file.exists()) {
			Intent sendIntent = new Intent(Constant.STOP_ACTION);
			sendBroadcast(sendIntent);
			return;
		}
		startPlay();
		
		int n = path.length();
		int m = path.lastIndexOf("/");
		String mMusicTitle = path.substring(m + 1, n);
		Intent sendIntent = new Intent(Constant.UPDATE_TITLE);
		sendIntent.putExtra("musictitle", mMusicTitle);
		sendBroadcast(sendIntent);
		startMTimer();
	}

	public void startPlay() {

		new Thread() {
			@Override
			public void run() {
				String path = mMusics.get(mIndex);
				Log.i(TAG,"startPlay path="+path);
				play(path);
				super.run();
			}
		}.start();

	}
	//synchronized
	public void play(String path){
		//Log.i(TAG,"---play path="+path);
//		String path2 = mMusics.get(mIndex);
//		//Log.i(TAG,"---play path2="+path2);
//		if(!path.equals(path2)){
//			Log.i(TAG,"---path is not equals path2");
//			return;
//		}
//		if (mediaPlayer != null) {
//			//mediaPlayer.stop();
//			mediaPlayer.release();
//			mediaPlayer = null;
//			Log.i(TAG,"--->mediaPlayer release");
//		}
//	
//		if (mediaPlayer == null) {
//			Log.i(TAG,"--->new MediaPlayer");
//			mediaPlayer = new MediaPlayer();
//			mediaPlayer.setOnPreparedListener(this);
//			mediaPlayer.setOnCompletionListener(this);
//			mediaPlayer.setOnErrorListener(this);
//		}
		try {
			if(mediaPlayer!=null){
				Log.i(TAG,"mediaPlayer reset");
				mediaPlayer.reset();
				Log.i(TAG,"mediaPlayer setDataSource");
				mediaPlayer.setDataSource(path);
				Log.i(TAG,"mediaPlayer prepare");
				mediaPlayer.prepare();
				Log.i(TAG,"mediaPlayer start");
				mediaPlayer.start();
				if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
					duration = mediaPlayer.getDuration();
				}
			}
		} catch (IllegalStateException e) {
			Log.i(TAG, "---->catch 1");
			play(path);
			e.printStackTrace();			
		} catch (IOException e) {
			Log.i(TAG, "---->catch 2");
			play(path);
			e.printStackTrace();
		}catch (IllegalArgumentException e) {
			Log.i(TAG, "---->catch 3");
			play(path);
			e.printStackTrace();
		}
	}
	
	public static void pauseMusic() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPause = true;
		}
	}

	
	public static void restartMusic() {
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	}

	public void stopMusic() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			// try {
			// mediaPlayer.prepare();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
	}

	public int getDuration() {
		return mediaPlayer.getDuration();
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public void seekto(int progress) {
		mediaPlayer.seekTo(progress);
	}

	private void getPlayMode() {
		
		if (type == MusicConstant.PlayerMag.PLAY_BGMUSIC) {
			musicList = PlayerUtil.getBackgroundMusicList(MusicService.this);
			if (musicList.size() == 0)
				return;
			bgIndex++;
			if (bgIndex == musicList.size() && musicList.size() > 0) {
				bgIndex = 0;
			}
			playBGMusic();
			return;
		}

		int playmode = MusicPlayActivity.getSavePlayMode();
		Log.i(TAG,"getPlayMode =" + playmode);
		switch (playmode) {
		case 1:
			mediaPosition = 0;
			MusicPlayActivity.mmprogress = 0;
			mediaPlayer.start();
			startMTimer();
			return;
		case 2:
			mIndex++;
			if (mIndex == mMusics.size()) {
				mIndex = 0;
			}
			break;
		case 3:
			mIndex++;
			if (mIndex < mMusics.size()) {
				break;
			} else {
				mIndex = mMusics.size() - 1;
				Intent sendIntent = new Intent(Constant.PLAY_OVER);
				sendBroadcast(sendIntent);
				return;
			}
		case 4:
			mIndex = getRandomIndex(mMusics.size());
			if (mIndex <= mMusics.size()) {
				break;
			} else {
				return;
			}
		}
		Intent sendIntent = new Intent(Constant.UPDATE_INDEX);
		sendIntent.putExtra("mIndex", mIndex);
		sendBroadcast(sendIntent);
		playMusic();
	}

	public static void setPlayMusicPosition() {
		bgIndex = -1;
	}

	public static boolean BGMusicIsPlaying() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()
				&& type == MusicConstant.PlayerMag.PLAY_BGMUSIC) {
			return true;
		}
		return false;
	}

	protected static int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
	}

	public void startMTimer() {
		if (MusicPlayActivity.music_progressBar != null) {
			if(mTimer !=null){
				mTimer.cancel();
				mTimer = null;
			}
			if (mTimerTask != null) {
				mTimerTask.cancel();
				mTimerTask = null;
			}
			
			mTimer = new Timer();
			if (mTimerTask == null) {
				mTimerTask = new TimerTask() {
					@Override
					public void run() {
						try {
							if (mediaPlayer == null) {
								return;
							} else if (mediaPlayer.isPlaying()
									&& MusicPlayActivity.isseekbarChange == false) {
								handleProgress.sendEmptyMessage(0);
							}

						} catch (Exception e) {

						}
					}
				};
			}
			if (mTimer != null && mTimerTask != null) {
				mTimer.schedule(mTimerTask, 0, 1001);
			}
		}
	}

	public void cancelMTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	public static long mediaPosition;
	private long duration;
	Handler handleProgress = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mediaPosition += 1000;
			if (duration > 0) {
				if (mediaPosition >= duration) {
					mediaPosition = duration;
				}

				MusicPlayActivity.setMusicTime(mediaPosition, duration);
				long pos = MusicPlayActivity.music_progressBar.getMax()
							* mediaPosition / duration;
				MusicPlayActivity.music_progressBar.setProgress((int) pos);
			}
		};
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		//Log.i(TAG,"onPrepared");
		// mp.start();
	}

	private boolean isError = false;

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.i(TAG, "mediaPlayer onError,what=" + what + ",extra=" + extra);
		if (what == 100) {
			isError = true;
			playMusic();
			return false;
		}

		Intent intent = new Intent();
		intent.setAction("com.kt.media.MUSIC_SERVICE");
		stopService(intent);

		Intent sendIntent = new Intent(Constant.PLAY_ERROR);
		sendBroadcast(sendIntent);
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCompletion!");

		if (isError) {
			isError = false;
			Log.i(TAG, "onCompletion isError");
			return;
		}	
		Log.i(TAG, "onCompletion getPlayMode");
		getPlayMode();
	}
}