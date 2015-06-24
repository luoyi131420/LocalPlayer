package com.kt.localmedia.video;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kt.localmedia.R;
import com.kt.localmedia.util.Constant;
import com.kt.localmedia.util.SaveInfo;

public class ZimuSettingActivity extends Activity implements OnCheckedChangeListener,OnKeyListener{
	private static final String TAG = "KTPlayer";   
	//private TextView mTitle;
	private CheckBox color_yellow,color_white,color_blue,
					 small_size,middle_size,large_size,
					 code_gb,code_utf,code_gbk;
	private TextView lDelayTime,rDelayTime;
	private String zCode;
	private int zColor,zSize;
	private int zDelayTime;
	private SeekBar seekBar;
	private int pos=0;
	float d_time;
	private SaveInfo zimuSave;
	private final static int SIZE_24 =24;
	private final static int SIZE_36 =36;
	private final static int SIZE_48 =48;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zimu_setting_layout);
		findViewById(); 
		zimuSave = new SaveInfo(ZimuSettingActivity.this,"zimu");
		pos=seekBar.getMax()/2;
		showSaveInfo();	
			 	
		color_yellow.setOnCheckedChangeListener(this);		
		color_white.setOnCheckedChangeListener(this);			
		color_blue.setOnCheckedChangeListener(this);
		small_size.setOnCheckedChangeListener(this);		
		middle_size.setOnCheckedChangeListener(this);			
		large_size.setOnCheckedChangeListener(this);	
		code_gb.setOnCheckedChangeListener(this);		
		code_utf.setOnCheckedChangeListener(this);			
		code_gbk.setOnCheckedChangeListener(this);
		seekBar.setOnKeyListener(this);
	}
	
	private void findViewById(){
		//mTitle = (TextView) findViewById(R.id.title);  
		color_yellow = (CheckBox) findViewById(R.id.yellow);
		color_white = (CheckBox) findViewById(R.id.white);
		color_blue = (CheckBox) findViewById(R.id.blue);
		small_size = (CheckBox) findViewById(R.id.s);
		middle_size = (CheckBox) findViewById(R.id.m);
		large_size = (CheckBox) findViewById(R.id.l);
		code_gb = (CheckBox) findViewById(R.id.gb);
		code_utf = (CheckBox) findViewById(R.id.utf);
		code_gbk = (CheckBox) findViewById(R.id.gbk);
		seekBar = (SeekBar) findViewById(R.id.seekbar);
		lDelayTime = (TextView) findViewById(R.id.l_time); 
		rDelayTime = (TextView) findViewById(R.id.r_time); 
	}
	
	private boolean isBackFlag;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK) {		
			isBackFlag = true;
			zimuSave.saveZimuSettings(zColor,zSize,zCode,zDelayTime);
			Intent intent=new Intent();  
			setResult(1, intent); 
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void showSaveInfo(){
		if(zimuSave.getZimuColor()==Color.WHITE){
			color_white.setChecked(true);
			color_white.requestFocus();
			zColor = Color.WHITE;
		}else if(zimuSave.getZimuColor()==Color.BLACK){
			color_yellow.setChecked(true);
			color_yellow.requestFocus();
			zColor = Color.YELLOW;
		}else if(zimuSave.getZimuColor()==Color.BLUE){
			color_blue.setChecked(true);
			color_blue.requestFocus();
			zColor = Color.BLUE;
		}else{
			color_white.setChecked(true);
			color_white.requestFocus();
			zColor = Color.WHITE;
		}
		
		if(zimuSave.getZimuSize()==SIZE_24){
			small_size.setChecked(true);
			zSize = SIZE_24;
		}else if(zimuSave.getZimuSize()==SIZE_36){
			middle_size.setChecked(true);
			zSize = SIZE_36;
		}else if(zimuSave.getZimuSize()==SIZE_48){
			large_size.setChecked(true);
			zSize = SIZE_48;
		}else{
			middle_size.setChecked(true);
			zSize = SIZE_24;
		}

		if(zimuSave.getZimuCode().contains("gb2312")){
			code_gb.setChecked(true);
			zCode = "gb2312";
		}else if(zimuSave.getZimuCode().contains("utf8")){
			code_utf.setChecked(true);
			zCode = "utf8";
		}else if(zimuSave.getZimuCode().contains("gbk")){
			code_gbk.setChecked(true);
			zCode = "gbk";
		}else{
			code_gb.setChecked(true);
			zCode = "gb2312";
		}

		if(zimuSave.getZimuDelay()<0){
			showSaveLeftTime((float)zimuSave.getZimuDelay()/1000);
		}else if(zimuSave.getZimuDelay()>0){
			showSaveRightTime((float)zimuSave.getZimuDelay()/1000);
		}else{
			seekBar.setProgress(pos);
		}

	}
	
	private void showSaveLeftTime(float daley){
		if(daley==-0.5){seekBar.setProgress(38);pos=38;}
		else if(daley==-1.0){seekBar.setProgress(36);pos=36;}
		else if(daley==-1.5){seekBar.setProgress(34);pos=34;}
		else if(daley==-2.0){seekBar.setProgress(32);pos=32;}
		else if(daley==-2.5){seekBar.setProgress(30);pos=30;}
		else if(daley==-3.0){seekBar.setProgress(28);pos=28;}
		else if(daley==-3.5){seekBar.setProgress(26);pos=26;}
		else if(daley==-4.0){seekBar.setProgress(24);pos=24;}
		else if(daley==-4.5){seekBar.setProgress(22);pos=22;}
		else if(daley==-5.0){seekBar.setProgress(20);pos=20;}
		else if(daley==-5.5){seekBar.setProgress(18);pos=18;}
		else if(daley==-6.0){seekBar.setProgress(16);pos=16;}
		else if(daley==-6.5){seekBar.setProgress(14);pos=14;}
		else if(daley==-7.0){seekBar.setProgress(12);pos=12;}
		else if(daley==-7.5){seekBar.setProgress(10);pos=10;}
		else if(daley==-8.0){seekBar.setProgress(8);pos=8;}
		else if(daley==-8.5){seekBar.setProgress(6);pos=6;}
		else if(daley==-9.0){seekBar.setProgress(4);pos=4;}
		else if(daley==-9.5){seekBar.setProgress(2);pos=2;}
		else if(daley==-10.0){seekBar.setProgress(0);pos=0;}
		showDelayTime(pos);
	}
	
	private void showSaveRightTime(float daley){

		if(daley==0.5){seekBar.setProgress(42);pos=42;}
		else if(daley==1.0){seekBar.setProgress(44);pos=44;}
		else if(daley==1.5){seekBar.setProgress(46);pos=46;}
		else if(daley==2.0){seekBar.setProgress(48);pos=48;}
		else if(daley==2.5){seekBar.setProgress(50);pos=50;}
		else if(daley==3.0){seekBar.setProgress(52);pos=52;}
		else if(daley==3.5){seekBar.setProgress(54);pos=54;}
		else if(daley==4.0){seekBar.setProgress(56);pos=56;}
		else if(daley==4.5){seekBar.setProgress(58);pos=58;}
		else if(daley==5.0){seekBar.setProgress(60);pos=60;}
		else if(daley==5.5){seekBar.setProgress(62);pos=62;}
		else if(daley==6.0){seekBar.setProgress(64);pos=64;}
		else if(daley==6.5){seekBar.setProgress(66);pos=66;}
		else if(daley==7.0){seekBar.setProgress(68);pos=68;}
		else if(daley==7.5){seekBar.setProgress(70);pos=70;}
		else if(daley==8.0){seekBar.setProgress(72);pos=72;}
		else if(daley==8.5){seekBar.setProgress(74);pos=74;}
		else if(daley==9.0){seekBar.setProgress(76);pos=76;}
		else if(daley==9.5){seekBar.setProgress(78);pos=78;}
		else if(daley==10.0){seekBar.setProgress(80);pos=80;}
		showDelayTime(pos);
	}
	
	private void showDelayTime(int p){
		
		switch(p){
			case 38:
			case 42:
				d_time = (float) 0.5;
				break;
			case 36:
			case 44:
				d_time = (float) 1.0;
				break;
			case 34:
			case 46:
				d_time = (float) 1.5;
				break;
			case 32:
			case 48:
				d_time = (float) 2.0;
				break;
			case 30:
			case 50:
				d_time = (float) 2.5;
				break;
			case 28:
			case 52:
				d_time = 3;
				break;
			case 26:
			case 54:
				d_time = (float) 3.5;
				break;
			case 24:
			case 56:
				d_time = (float) 4.0;
				break;
			case 22:
			case 58:
				d_time = (float) 4.5;
				break;
			case 20:
			case 60:
				d_time = (float) 5.0;
				break;
			case 18:
			case 62:
				d_time = (float) 5.5;
				break;
			case 16:
			case 64:
				d_time = (float) 6.0;
				break;
			case 14:
			case 66:
				d_time = (float) 6.5;
				break;
			case 12:
			case 68:
				d_time = (float) 7.0;
				break;
			case 10:
			case 70:
				d_time = (float) 7.5;
				break;
			case 8:
			case 72:
				d_time = (float) 8.0;
				break;
			case 6:
			case 74:
				d_time = (float) 8.5;
				break;
			case 4:
			case 76:
				d_time = (float) 9.0;
				break;
			case 2:
			case 78:
				d_time = (float) 9.5;
				break;
			case 0:
			case 80:
				d_time =(float) 10.0;
				break;
		}
		
		if(pos<40){
			rDelayTime.setVisibility(View.GONE);
			lDelayTime.setVisibility(View.VISIBLE);	

			lDelayTime.setText("-"+d_time+"s");
			zDelayTime = (int)(-d_time*1000);
		}else if(pos>40){
			lDelayTime.setVisibility(View.GONE);
			rDelayTime.setVisibility(View.VISIBLE);			

			rDelayTime.setText("+"+d_time+"s");
			zDelayTime = (int)(d_time*1000);
		}else{
			lDelayTime.setVisibility(View.GONE);
			rDelayTime.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked){

		switch(button.getId()){
			case R.id.yellow:
				if(isChecked){
					color_white.setChecked(false);
					color_blue.setChecked(false);
					zColor = Color.YELLOW;
				}
			break;
			case R.id.white:
				if(isChecked){
					color_yellow.setChecked(false);
					color_blue.setChecked(false);
					zColor = Color.WHITE;
				}
			break;
			case R.id.blue:
				if(isChecked){
					color_white.setChecked(false);
					color_yellow.setChecked(false);
					zColor = Color.BLUE;
				}
			break;
			
			case R.id.s:
				if(isChecked){
					middle_size.setChecked(false);
					large_size.setChecked(false);
					zSize = SIZE_24;
				}
			break;
			case R.id.m:
				if(isChecked){
					small_size.setChecked(false);
					large_size.setChecked(false);
					zSize = SIZE_36;
				}
			break;
			case R.id.l:
				if(isChecked){
					small_size.setChecked(false);
					middle_size.setChecked(false);
					zSize = SIZE_48;
				}
			break;
			
			case R.id.gb:
				if(isChecked){
					code_utf.setChecked(false);
					code_gbk.setChecked(false);
					zCode="gb2312";
				}
			break;
			case R.id.utf:
				if(isChecked){
					code_gb.setChecked(false);
					code_gbk.setChecked(false);
					zCode="utf8";
				}
			break;
			case R.id.gbk:
				if(isChecked){
					code_gb.setChecked(false);
					code_utf.setChecked(false);
					zCode="gbk";
				}
			break;
		}
		zimuSave.saveZimuSettings(zColor,zSize,zCode,zDelayTime);
	}
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(event.getAction()==KeyEvent.ACTION_DOWN){
			if(keyCode==22){
				pos= pos+(seekBar.getMax()/40);
				if(pos>=seekBar.getMax()){
					pos=seekBar.getMax();
				}
				seekBar.setProgress(pos);
			}
			else if(keyCode==21){
				pos= pos-(seekBar.getMax()/40);
				if(pos<=0){
					pos=0;
				}
				seekBar.setProgress(pos);
			}
			showDelayTime(pos);
		}				
		return false;
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
	
	@Override  
	protected void onDestroy() {
		this.finish();
		super.onDestroy();  
	}

}