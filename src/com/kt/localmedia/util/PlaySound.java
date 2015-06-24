package com.kt.localmedia.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import com.kt.localmedia.R;

public class PlaySound {
	
	private SoundPool sp;
	private int soundid;
	Context mContext;
	private float volumnRatio ;
	private boolean isPlay;
	public PlaySound(Context context){
		this.mContext = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); 
		soundid = sp.load(mContext, R.raw.fy, 1); 
		
		sp.setOnLoadCompleteListener(new OnLoadCompleteListener(){
			@Override
			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
				isPlay = true ;
			}
		});
		AudioManager am = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumnRatio = audioCurrentVolumn/audioMaxVolumn;
	}
		
	public void playSounds(int number){   
		if(isPlay){
			sp.play(soundid, volumnRatio, volumnRatio, 1, number, 1);
		}	
        //sp.play(soundid, 1, 1, 0, 0, 1);
    }

}

