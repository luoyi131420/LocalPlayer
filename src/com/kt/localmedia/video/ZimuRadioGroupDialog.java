package com.kt.localmedia.video;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.SaveInfo;

public class ZimuRadioGroupDialog extends Activity {
	/**
	 * 创建TextView对象
	 * 创建RadioGroup对象
	 * 创建4个RadioButton对象
	 */
	private TextView		mTextView;
	private RadioGroup		mRadioGroup;
	private RadioButton		mRadio;
	private int count;
	private int radioId[];	

	SaveInfo zimuSave,zimuTypeSave;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zimu_radio_layout);

		mTextView = (TextView) findViewById(R.id.text_title);
		mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
		
		mTextView.setText("字幕选择");
		
		zimuSave = new SaveInfo(ZimuRadioGroupDialog.this,"zimu");
		zimuTypeSave = new SaveInfo(ZimuRadioGroupDialog.this,"zimuType");
		
		Intent mintent = getIntent(); 
		Bundle bundle = mintent.getExtras();  
		count = bundle.getInt("zimuNum");

		if(count>0){
			radioId = new int[count];	
			//int type = zimuTypeSave.getZimuType();
			int type = 0;
			for(int i=0;i<count;i++){
				RadioButton mRadio = new RadioButton(this); 			
				mRadio.setId(i);
				radioId[i] = mRadio.getId();
				
				mRadio.setButtonDrawable(android.R.color.transparent);
				Resources res = this.getResources();
				Drawable myImage = res.getDrawable(android.R.drawable.btn_radio);
				mRadio.setCompoundDrawablesWithIntrinsicBounds(null, null, myImage,null);	
				mRadio.setText("" + (i+1));
				mRadioGroup.addView(mRadio);				
				if(i==type){
					mRadio.setChecked(true);
					mRadio.requestFocus();
				}
			}
			
		}else{
			Toast.makeText(ZimuRadioGroupDialog.this, "没有字幕", Toast.LENGTH_LONG).show();
			finish();
		}
			
		/* 设置事件监听  */
		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				// TODO Auto-generated method stub
				int pos=0;
				for(pos=0;pos<count;pos++){
					if (checkedId == radioId[pos]){
						break;
					}
				}	

				//zimuTypeSave.saveZimuType(pos);
				//myZimu.palyOtherZimu(pos);			
			}
		});
	}
	
	private boolean isBackFlag;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK) {		
			isBackFlag = true;
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override  
	protected void onPause() {
		if(isBackFlag){
			isBackFlag = false;
		}else{
			this.finish();
			Intent sendIntent = new Intent(Constant.FINISH_ACTION); 
			sendBroadcast(sendIntent);	
		}
		super.onPause(); 
	}
	
}