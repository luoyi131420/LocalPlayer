package com.kt.localmedia.video;


import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.localmedia.R;
import com.kt.localmedia.util.AutoSize;
import com.kt.localmedia.util.DBUtils;
import com.kt.localmedia.util.LogUtil;



public class VideoSetting extends FrameLayout implements View.OnClickListener{
	private static final String TAG = "KTPlayer";	
	private Context mContext;
	private ViewGroup mControllerView;
	private View mMenuView = null;
	ArrayList mItemList = new ArrayList<View>();

	private static VideoSettingHelper mSettingHelper=null;
	private static IndexStringPair mItemsPairRepeat=null;
	private static IndexStringPair mItemsPairSndTrack=null;
	private static IndexStringPair mItemsPairScale=null;
	private static IndexStringPair mItemsPair2d3d=null;
	private static IndexStringPair mItemsPairSutitle1ST=null;  /*primary    subtitle(1st)*/
	private static IndexStringPair mItemsPairSutitle2ND=null;  /*secondary subtitle(2nd)*/
	private static IndexStringPair mItemsPairTrack=null;
	private static final int AUTO_HIDE_SETTING=0x00029;


	public VideoSetting(Context context,VideoSettingHelper helper, ViewGroup parent){
		super(context);
		mContext = context;

		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				   LayoutParams.WRAP_CONTENT);				   
		setLayoutParams(layout);
		mControllerView = parent;

		mSettingHelper = helper;

		initMainMenu(context); 

		requestFocus(); 		   
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event){
		 if(mControllerView.getVisibility()!=View.VISIBLE){
			return false;
		 }
		 if(event.getAction() == event.ACTION_UP){
		 	updateArrowHighlight(false, false);
			updateArrowHighlight(true, false);
		 	return super.dispatchKeyEvent(event);
		 }

		 //cancel old auto hide-msg and send new auto hide-msg with delay
		 autoHideWithDelay();
		 
		 int keyCode = event.getKeyCode();
		 View focused;
		 boolean enable=false;
		 switch(keyCode){
		 	case KeyEvent.KEYCODE_DPAD_LEFT:
				updateArrowHighlight(true, true);
				onKeyLeftRight(true);
				//LogUtil.Log(TAG,"dispatchKeyEvent KEYCODE_DPAD_LEFT");
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				updateArrowHighlight(false, true);
				onKeyLeftRight(false);
				//LogUtil.Log(TAG,"dispatchKeyEvent KEYCODE_DPAD_RIGHT");
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				return requestFocusCC(View.FOCUS_DOWN);
			case KeyEvent.KEYCODE_DPAD_UP:
				return requestFocusCC(View.FOCUS_UP);
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_BACK:
				hideSetting();
				return true;
			default:
				break;
		 	
		 }
		 return mMenuView.dispatchKeyEvent(event);
	}

	private Handler mHandlerSetting = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AUTO_HIDE_SETTING:
				hideSetting();
			default:
				break;
			}
		}
	};

	private void autoHideWithDelay(){
		mHandlerSetting.removeMessages(AUTO_HIDE_SETTING);
		mHandlerSetting.sendEmptyMessageDelayed(AUTO_HIDE_SETTING, 10000);			
	}

	private boolean requestFocusCC(int direction){
		int index=mFucusIndex;
		if(direction==View.FOCUS_DOWN){
			index++;
		}
		if(direction==View.FOCUS_UP){
			index--;
		}
		if(index<0){index = mItemList.size()-1;}
		if(index>=mItemList.size()){index =0;}
		((View)mItemList.get(index)).requestFocus();
		//LogUtil.Log(TAG,"dispatchKeyEvent  CurView.id=" + ((View)mItemList.get(index)).getId());
		return true;
	}


	private void doLeftRight(IndexStringPair pair, boolean isLeft){
		if(isLeft){
			pair.switchLeft();
		}else{
			pair.switchRight();
		}
	}

	private boolean onKeyLeftRight(boolean isLeft){
		switch(mFucusIndex){
			case 0:
				doLeftRight(mItemsPairRepeat, isLeft);
				mSettingHelper.storeRepeatMode(mItemsPairRepeat.getCurIndex());
				LogUtil.Log(TAG,"mSettingHelper.storeRepeatMode index="+mItemsPairRepeat.getCurIndex());
				break;
			case 1:
				doLeftRight(mItemsPair2d3d, isLeft);
				//mSettingHelper.setCur2D3DMode(mItemsPair2d3d.getCurIndex());
				break;
			case 2:
				doLeftRight(mItemsPairScale, isLeft);
				mSettingHelper.setScreenScale(mItemsPairScale.getCurIndex());
				break;
			case 3:
				doLeftRight(mItemsPairSndTrack, isLeft);
				mSettingHelper.setSoundTrack(mItemsPairSndTrack.getCurIndex());
				break;
			case 4:
				doLeftRight(mItemsPairTrack, isLeft);
				mSettingHelper.setAudioTrack(mItemsPairTrack.getCurIndex());
				break;
			case 5:
				int index = mItemsPairSutitle1ST.getCurIndex();
				doLeftRight(mItemsPairSutitle1ST, isLeft);
				//setSubtitleThread(index);
				if(index!=mItemsPairSutitle1ST.getCurIndex()){
				    mSettingHelper.setPrimarySubtitle(mItemsPairSutitle1ST.getCurIndex());
				    mItemsPairSutitle2ND.updateStringPair(mSettingHelper.getSecondarySubtitles());
				}
				//return false;
				break;
			case 6:
				doLeftRight(mItemsPairSutitle2ND, isLeft);
				mSettingHelper.setSecondarySubtitle(mItemsPairSutitle2ND.getCurIndex());
				
				break;
			case 7:
				if(isLeft){
					mSettingHelper.adjustVolume(false);
				}else{
				    mSettingHelper.adjustVolume(true);
				}				
				break;
			default: 
		}
		updateMainMenu();
		return false;
	}

	public void setSubtitleThread(final int index){
		new Thread() {
			@Override
			public void run() {
//				int index = mItemsPairSutitle1ST.getCurIndex();
				if(index!=mItemsPairSutitle1ST.getCurIndex()){
				    mSettingHelper.setPrimarySubtitle(mItemsPairSutitle1ST.getCurIndex());
				    mItemsPairSutitle2ND.updateStringPair(mSettingHelper.getSecondarySubtitles());
				}
				super.run();
			}
		}.start();
	}
	
	public void showSetting(){
		mControllerView.setVisibility(View.VISIBLE);
		LinearLayout temp = (LinearLayout)mMenuView.findViewById(R.id.linear_item1);
		temp.requestFocus();
		updateMainMenu();
		
		float width = AutoSize.getInstance().getWidth();
		float height = AutoSize.getInstance().getHeight();
		
		Animation scaleAnimation = new ScaleAnimation(0.5f, 1.0f,0.5f,1.0f);
		Animation translate = new TranslateAnimation(width/2, 0, 0, 0);	
		Animation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);  

		AnimationSet set = new AnimationSet(true);  
		set.addAnimation(translate);  
		set.addAnimation(alphaAnimation); 
		set.addAnimation(scaleAnimation);
		set.setDuration(500);  
		mControllerView.startAnimation(set); 
	}

	public void hideSetting(){
		mHandlerSetting.removeMessages(AUTO_HIDE_SETTING);
		if(mControllerView!=null){
			mControllerView.setVisibility(View.GONE);
		}
	}

	public void onClick(View v){
	}

	private void setItemLayoutParams(View view, int param1, int param2){
		int grid = AutoSize.getInstance().getScaleHeight(0.1f);

		if(param1!=0){
			TextView text1 = (TextView)view.findViewById(param1);
			text1.setGravity(Gravity.LEFT);
			text1.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)(grid*0.4f));
		}

		if(param2!=0){
			TextView text2 = (TextView)view.findViewById(param2);
			text2.setGravity(Gravity.CENTER);
			text2.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)(grid*0.4f));
		}
	}


	public static void initAlterableStringPair(){
		//LogUtil.Log(TAG,"getAlterableStringPair");
		mSettingHelper.initEmbeddedSubtitleAndTrack();

		//mItemsPair2d3d.updateStringPair(mSettingHelper.getSupported2D3DMode());
		mItemsPairSutitle1ST.updateStringPair(mSettingHelper.getPrimarySubtitles());
		mItemsPairSutitle2ND.updateStringPair(mSettingHelper.getSecondarySubtitles());
		mItemsPairTrack.updateStringPair(mSettingHelper.getSupportedTracks());
	}

	private void initMainMenu(Context context){
		//LayoutInflater factory = LayoutInflater.from(mContext);
		//factory.inflate(R.layout.menu_setting, mControllerView);
		mMenuView = (View)mControllerView.findViewById(R.id.menu_setting);
		int cellWidth = AutoSize.getInstance().getScaleWidth(0.51f);
		int cellHeight = AutoSize.getInstance().getScaleHeight(0.72f);		

		String[] menu_items1 = context.getResources().getStringArray(R.array.menu_play_mode);
		mItemsPairRepeat = new IndexStringPair(menu_items1);
			
		String[] menu_items2 = context.getResources().getStringArray(R.array.menu_screen_scale);
		mItemsPairScale = new IndexStringPair(menu_items2);

		String[] menu_items3 = context.getResources().getStringArray(R.array.AudioChannel);
		mItemsPairSndTrack = new IndexStringPair(menu_items3);

		mItemsPair2d3d = new IndexStringPair(null);
		mItemsPairSutitle1ST = new IndexStringPair(null);
		mItemsPairSutitle2ND = new IndexStringPair(null);
		mItemsPairTrack   = new IndexStringPair(null);
	
		setItemLayoutParams(mMenuView, R.id.linear_text1_tile,R.id.linear_text10_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text2_tile,R.id.linear_text20_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text3_tile,R.id.linear_text30_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text4_tile,R.id.linear_text40_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text5_tile,R.id.linear_text50_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text6_tile,R.id.linear_text60_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text7_tile,R.id.linear_text70_tile);
		setItemLayoutParams(mMenuView, R.id.linear_text8_tile,0);

		mItemList.clear();
		mItemList.add(initMenuItem(R.id.linear_item1));
		mItemList.add(initMenuItem(R.id.linear_item2));
		mItemList.add(initMenuItem(R.id.linear_item3));
		mItemList.add(initMenuItem(R.id.linear_item4));
		mItemList.add(initMenuItem(R.id.linear_item5));
		mItemList.add(initMenuItem(R.id.linear_item6));
		mItemList.add(initMenuItem(R.id.linear_item7));
		mItemList.add(initMenuItem(R.id.linear_item8));

		ProgressBar volume = (ProgressBar)mMenuView.findViewById(R.id.progress_volume);
		volume.setMax(DBUtils.getMaxMusicVolume(mContext));
		volume.setProgress(mSettingHelper.getVolume());
	}

	private LinearLayout initMenuItem(final int id){
		LinearLayout temp = (LinearLayout)mMenuView.findViewById(id);
		int cell_w = AutoSize.getInstance().getScaleWidth(0.50f);
		int cell_h = AutoSize.getInstance().getScaleHeight(0.1f);		

		temp.setOnFocusChangeListener(mFocusListener);
		temp.getLayoutParams().width = cell_w;
		temp.getLayoutParams().height = cell_h;
		temp.setFocusable(true);
		return temp;
	}

	public void setSubtitleHandler(Handler handler){
		mSettingHelper.setSubtitleHandler(handler);
	}


	private void updateArrowHighlight(boolean isLeft, boolean isHightlight){
		mMenuView = (View)mControllerView.findViewById(R.id.menu_setting);
		ImageView image=null;
		int imageLeft  = 0;
		int imageRight = 0;
		switch(mFucusIndex){
			case 0:
				imageLeft  = R.id.linear_image_left1;
				imageRight = R.id.linear_image_right1;
				break;
			case 1:
				imageLeft  = R.id.linear_image_left2;
				imageRight = R.id.linear_image_right2;
				break;
			case 2:
				imageLeft  = R.id.linear_image_left3;
				imageRight = R.id.linear_image_right3;
				break;
			case 3:
				imageLeft  = R.id.linear_image_left4;
				imageRight = R.id.linear_image_right4;
				break;
			case 4:
				imageLeft  = R.id.linear_image_left5;
				imageRight = R.id.linear_image_right5;
				break;
			case 5:
				imageLeft  = R.id.linear_image_left6;
				imageRight = R.id.linear_image_right6;
				break;
			case 6:
				imageLeft  = R.id.linear_image_left7;
				imageRight = R.id.linear_image_right7;
				break;
			case 7:
				if(isLeft){
					image = (ImageView)mMenuView.findViewById(R.id.linear_image_left8);
					if(isHightlight){
						image.setImageResource(R.drawable.voice_no_hl);
					}
				}else{
					image = (ImageView)mMenuView.findViewById(R.id.linear_image_right8);
					if(isHightlight){
						image.setImageResource(R.drawable.voice_ok_hl);
					}
				}
			default:
				break;
		}
		if(0!=imageLeft){
		    updateItemHightlight(isLeft, isHightlight, imageLeft,imageRight);
		}
	}

    private void updateItemHightlight(boolean isLeft, boolean isHighlight, 
		                            final int imageLeft, final int imageRight){                   
        if(isLeft){
            ImageView image = (ImageView)mMenuView.findViewById(imageLeft);
			if(null != image){
                setLeftImage(image,isHighlight);
			}
        }else{
            ImageView image = (ImageView)mMenuView.findViewById(imageRight);
			if(null != image){
                setRightImage(image,isHighlight);
			}
        }	
	}

	private void setLeftImage(ImageView view, boolean isHeightlight){
		if(isHeightlight){
			view.setImageResource(R.drawable.arrow_left_h);
		}else{
			view.setImageResource(R.drawable.arrow_left);
		}
	}

	private void setRightImage(ImageView view, boolean isHeightlight){
		if(isHeightlight){
			view.setImageResource(R.drawable.arrow_right_h);
		}else{
			view.setImageResource(R.drawable.arrow_right);
		}

	}

	private void updateMainMenu(){
		mMenuView = (View)mControllerView.findViewById(R.id.menu_setting);
		//play mode
		TextView func1 = (TextView)mMenuView.findViewById(R.id.linear_text10_tile);
		mItemsPairRepeat.setCurIndex(mSettingHelper.getRepeatMode());
		func1.setText(mItemsPairRepeat.getCurText());

		//2d&3d mode
		TextView func2 = (TextView)mMenuView.findViewById(R.id.linear_text20_tile);
		//mItemsPair2d3d.setCurIndex(mSettingHelper.getCur2D3DModeIndex());
		func2.setText(mItemsPair2d3d.getCurText());

		//screen scale
		TextView func3 = (TextView)mMenuView.findViewById(R.id.linear_text30_tile);
		mItemsPairScale.setCurIndex(mSettingHelper.getScreenModeIndex());
		func3.setText(mItemsPairScale.getCurText());

		//sound track
		TextView func4 = (TextView)mMenuView.findViewById(R.id.linear_text40_tile);
		mItemsPairSndTrack.setCurIndex(mSettingHelper.getAudioChannelMode());
		func4.setText(mItemsPairSndTrack.getCurText());	

		//audio track
		TextView func5 = (TextView)mMenuView.findViewById(R.id.linear_text50_tile);
		mItemsPairRepeat.setCurIndex(mSettingHelper.getRepeatMode());
		func5.setText(mItemsPairTrack.getCurText());	

		//Primary Subtitle
		TextView func6 = (TextView)mMenuView.findViewById(R.id.linear_text60_tile);
		LogUtil.LogPlayer(TAG, "subtitle text="+mItemsPairSutitle1ST.getCurText());
		func6.setText(mItemsPairSutitle1ST.getCurText());	

		//Secondary Subtitle
		TextView func7 = (TextView)mMenuView.findViewById(R.id.linear_text70_tile);
		func7.setText(mItemsPairSutitle2ND.getCurText());	

		ProgressBar volume = (ProgressBar)mMenuView.findViewById(R.id.progress_volume);
		volume.setMax(DBUtils.getMaxMusicVolume(mContext));
		volume.setProgress(mSettingHelper.getVolume());

		ImageView btn_L1 = (ImageView)mMenuView.findViewById(R.id.linear_image_left1);
		ImageView btn_R1 = (ImageView)mMenuView.findViewById(R.id.linear_image_right1);
		btn_L1.setOnTouchListener(mBtnTouchListener);
		btn_R1.setOnTouchListener(mBtnTouchListener);
		
		ImageView btn_L2 = (ImageView)mMenuView.findViewById(R.id.linear_image_left2);
		ImageView btn_R2 = (ImageView)mMenuView.findViewById(R.id.linear_image_right2);
		btn_L2.setOnTouchListener(mBtnTouchListener);
		btn_R2.setOnTouchListener(mBtnTouchListener);

		ImageView btn_L3 = (ImageView)mMenuView.findViewById(R.id.linear_image_left3);
		ImageView btn_R3 = (ImageView)mMenuView.findViewById(R.id.linear_image_right3);
		btn_L3.setOnTouchListener(mBtnTouchListener);
		btn_R3.setOnTouchListener(mBtnTouchListener);

		ImageView btn_L4 = (ImageView)mMenuView.findViewById(R.id.linear_image_left4);
		ImageView btn_R4 = (ImageView)mMenuView.findViewById(R.id.linear_image_right4);
		btn_L4.setOnTouchListener(mBtnTouchListener);
		btn_R4.setOnTouchListener(mBtnTouchListener);

		ImageView btn_L5 = (ImageView)mMenuView.findViewById(R.id.linear_image_left5);
		ImageView btn_R5 = (ImageView)mMenuView.findViewById(R.id.linear_image_right5);
		btn_L5.setOnTouchListener(mBtnTouchListener);
		btn_R5.setOnTouchListener(mBtnTouchListener);

		ImageView btn_L6 = (ImageView)mMenuView.findViewById(R.id.linear_image_left6);
		ImageView btn_R6 = (ImageView)mMenuView.findViewById(R.id.linear_image_right6);
		btn_L6.setOnTouchListener(mBtnTouchListener);
		btn_R6.setOnTouchListener(mBtnTouchListener);

		ImageView btn_L7 = (ImageView)mMenuView.findViewById(R.id.linear_image_left7);
		ImageView btn_R7 = (ImageView)mMenuView.findViewById(R.id.linear_image_right7);
		btn_L7.setOnTouchListener(mBtnTouchListener);
		btn_R7.setOnTouchListener(mBtnTouchListener);

		ImageView btn_L8 = (ImageView)mMenuView.findViewById(R.id.linear_image_left8);
		ImageView btn_R8 = (ImageView)mMenuView.findViewById(R.id.linear_image_right8);
		btn_L8.setOnTouchListener(mBtnTouchListener);
		btn_R8.setOnTouchListener(mBtnTouchListener);

		mMenuView = (View)mControllerView.findViewById(R.id.menu_setting);
		ImageView image=null;	
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left1);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right1);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left2);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right2);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left3);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right3);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left4);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right4);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left5);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right5);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left6);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right6);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left7);
		setLeftImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right7);
		setRightImage(image,false);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_left8);
		image.setImageResource(R.drawable.voice_no);
		image = (ImageView)mMenuView.findViewById(R.id.linear_image_right8);
		image.setImageResource(R.drawable.voice_ok);

	}


	public void onLeftClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
			case R.id.linear_image_left1:
				mFucusIndex = 0;
				updateArrowHighlight(true, true);
				doLeftRight(mItemsPairRepeat, true);
				mSettingHelper.storeRepeatMode(mItemsPairRepeat.getCurIndex());
				LogUtil.Log(TAG,"mSettingHelper.storeRepeatMode index="+mItemsPairRepeat.getCurIndex());
				break;
			case R.id.linear_image_left2:
				mFucusIndex = 1;
				updateArrowHighlight(true, true);
				doLeftRight(mItemsPair2d3d, true);
				//mSettingHelper.setCur2D3DMode(mItemsPair2d3d.getCurIndex());
				break;
			case R.id.linear_image_left3:
				mFucusIndex = 2;
				updateArrowHighlight(true, true);
				doLeftRight(mItemsPairScale, true);
				mSettingHelper.setScreenScale(mItemsPairScale.getCurIndex());
				break;
			case R.id.linear_image_left4:
				mFucusIndex = 3;
				updateArrowHighlight(true, true);
				doLeftRight(mItemsPairSndTrack, true);
				mSettingHelper.setSoundTrack(mItemsPairSndTrack.getCurIndex());
				break;
			case R.id.linear_image_left5:
				mFucusIndex = 4;
				updateArrowHighlight(true, true);
				doLeftRight(mItemsPairTrack, true);
				mSettingHelper.setAudioTrack(mItemsPairTrack.getCurIndex());
				break;
			case R.id.linear_image_left6:
				mFucusIndex = 5;
				updateArrowHighlight(true, true);
				int index = mItemsPairSutitle1ST.getCurIndex();
				doLeftRight(mItemsPairSutitle1ST, true);
				if(index != mItemsPairSutitle1ST.getCurIndex()){
				    mSettingHelper.setPrimarySubtitle(mItemsPairSutitle1ST.getCurIndex());
				    mItemsPairSutitle2ND.updateStringPair(mSettingHelper.getSecondarySubtitles());
				}
				break;
			case R.id.linear_image_left7:
				mFucusIndex = 6;
				updateArrowHighlight(true, true);
				doLeftRight(mItemsPairSutitle2ND, true);
				mSettingHelper.setSecondarySubtitle(mItemsPairSutitle2ND.getCurIndex());
				break;
			case R.id.linear_image_left8:
				mFucusIndex = 7;
				updateArrowHighlight(true, true);
				mSettingHelper.adjustVolume(false);
				break;
			default:
				break;
		}
		
	}

	View.OnTouchListener mBtnTouchListener = new View.OnTouchListener(){

		public boolean onTouch(View v, MotionEvent event) {
			//cancel old auto hide-msg and send new auto hide-msg with delay
			autoHideWithDelay();

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				onLeftClick(v);
				onRightClick(v);
			}else if (event.getAction() == MotionEvent.ACTION_UP){
				updateMainMenu();
				updateArrowHighlight(false, false);
				updateArrowHighlight(true, false);	
			}
			
			return true;
		}
	};


	public void onRightClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
			case R.id.linear_image_right1:
				mFucusIndex = 0;
				updateArrowHighlight(false, true);
				doLeftRight(mItemsPairRepeat, false);
				mSettingHelper.storeRepeatMode(mItemsPairRepeat.getCurIndex());
				LogUtil.Log(TAG,"mSettingHelper.storeRepeatMode index="+mItemsPairRepeat.getCurIndex());
				break;
			case R.id.linear_image_right2:
				mFucusIndex = 1;
				updateArrowHighlight(false, true);
				doLeftRight(mItemsPair2d3d, false);
				//mSettingHelper.setCur2D3DMode(mItemsPair2d3d.getCurIndex());
				break;
			case R.id.linear_image_right3:
				mFucusIndex = 2;
				updateArrowHighlight(false, true);
				doLeftRight(mItemsPairScale, false);
				mSettingHelper.setScreenScale(mItemsPairScale.getCurIndex());
				break;
			case R.id.linear_image_right4:
				mFucusIndex = 3;
				updateArrowHighlight(false, true);
				doLeftRight(mItemsPairTrack, false);
				mSettingHelper.setAudioTrack(mItemsPairTrack.getCurIndex());
				break;
			case R.id.linear_image_right5:
				mFucusIndex = 4;
				updateArrowHighlight(false, true);
				doLeftRight(mItemsPairSndTrack, false);
				mSettingHelper.setSoundTrack(mItemsPairSndTrack.getCurIndex());				
				break;
			case R.id.linear_image_right6:
				mFucusIndex = 5;
				updateArrowHighlight(false, true);
				int index = mItemsPairSutitle1ST.getCurIndex();
				doLeftRight(mItemsPairSutitle1ST, false);
				if(index != mItemsPairSutitle1ST.getCurIndex()){
				    mSettingHelper.setPrimarySubtitle(mItemsPairSutitle1ST.getCurIndex());
				    mItemsPairSutitle2ND.updateStringPair(mSettingHelper.getSecondarySubtitles());
				}
				break;
			case R.id.linear_image_right7:
				mFucusIndex = 6;
				updateArrowHighlight(false, true);
				doLeftRight(mItemsPairSutitle2ND, false);
				mSettingHelper.setSecondarySubtitle(mItemsPairSutitle2ND.getCurIndex());				
				break;
			case R.id.linear_image_right8:
				mFucusIndex = 7;
				updateArrowHighlight(false, true);
				mSettingHelper.adjustVolume(true);
				break;
			default:
				break;
		}
		
	}

	private int mFucusIndex = 0;
	private OnFocusChangeListener mFocusListener = new OnFocusChangeListener(){
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus==false){return ;}
			switch(v.getId()){
				case R.id.linear_item1:
					mFucusIndex=0;
					break;
				case R.id.linear_item2:
					mFucusIndex=1;
					break;
				case R.id.linear_item3:
					mFucusIndex=2;
					break;
				case R.id.linear_item4:
					mFucusIndex=3;
					break;
				case R.id.linear_item5:
					mFucusIndex=4;
					break;
				case R.id.linear_item6:
					mFucusIndex=5;
					break;
				case R.id.linear_item7:
					mFucusIndex=6;
					break;
				case R.id.linear_item8:
					mFucusIndex=7;
					break;			
			}

			//cancel old auto hide-msg and send new auto hide-msg with delay
			autoHideWithDelay();

			//LogUtil.Log(TAG,"Menu onFocusChange mFucusIndex=" + mFucusIndex);		
		}
	};


	private class IndexStringPair{
		private ArrayList<String> mList;
		private int mCurIndex=0;
		private final int DIR_LEFT = 100;
		private final int DIR_RIGHT = 200;
		public IndexStringPair(){
			mList = new ArrayList<String>();
			updateStringPair(null);
		};

		public IndexStringPair(String[] itemNames){
			mList = new ArrayList<String>();
			updateStringPair(itemNames);
		}
		
		public void updateStringPair(String[] itemNames){
			mList.clear();
			if(itemNames!=null){
				for(int index=0; index<itemNames.length;index++){
					addIndexString(index, itemNames[index]);
				}
			}

			if(mList.size()==0){
				addIndexString(0, "None");
			}

			setCurIndex(0);
		}
		
		public int addIndexString(int index, String text){
			if(index==mList.size()){
				mList.add(index, text);
				return 0;
			}
			return -1;
		}

		public String getCurText(){
//			if(mCurIndex<0){mCurIndex=0;}
//			if(mCurIndex>=mList.size()){mCurIndex=mList.size()-1;}
			if(mCurIndex<0){mCurIndex=mList.size()-1;}
			if(mCurIndex>=mList.size()){mCurIndex=0;}
			return mList.get(mCurIndex);
		}

		public void setCurIndex(int idx){
			mCurIndex=idx;
			if(mCurIndex<0){mCurIndex=0;}
			if(mCurIndex>=mList.size()){mCurIndex=mList.size()-1;}
		}

		public int getCurIndex(){
			return mCurIndex;
		}

		public String switchLeft(){
			mCurIndex--;
			return getCurText();
		}

		public String switchRight(){
			mCurIndex++;
			return getCurText();
		}
	}
}

