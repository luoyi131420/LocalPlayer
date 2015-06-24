package com.kt.localmedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kt.localmedia.database.PlayTimeDatabaseControl;


public class MainActivity extends Activity {
	
	List<String> listpath = new ArrayList<String>();
    private FrameLayout framlayout = null;
    private String playType="playerType";
    PlayTimeDatabaseControl dbHelper;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			for(int i=0;i<listpath.size();i++){
				File file = new File(listpath.get(i));
				if(file.isFile()||file.isDirectory()){
					 System.out.println("--------this is liujun---------------"+file.getAbsolutePath());
				}
			
			}
			super.handleMessage(msg);
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	
		setContentView(R.layout.mainui);
		Button yinyue_btn = (Button)findViewById(R.id.yinyue_btn);
		Button shipin_btn = (Button)findViewById(R.id.shipin_btn);
		Button tupian_btn = (Button)findViewById(R.id.tupian_btn);
		framlayout =(FrameLayout)findViewById(R.id.main_framlayout);
		yinyue_btn.setOnClickListener(listener);
		tupian_btn.setOnClickListener(listener);
		shipin_btn.setOnClickListener(listener);
		
		
		yinyue_btn.setOnFocusChangeListener(focusListener);
		tupian_btn.setOnFocusChangeListener(focusListener);
		shipin_btn.setOnFocusChangeListener(focusListener);
		
		dbHelper = new PlayTimeDatabaseControl(MainActivity.this);
	}

	private View.OnClickListener listener=new View.OnClickListener() {
		
		
		@Override
		public void onClick(View v) {

			if(v.getId()==R.id.yinyue_btn){
				Intent mIntent=new Intent(getBaseContext(), DeviceListActivity.class);
				mIntent.putExtra("playerType", "music");
				startActivity(mIntent);
			}
			else if(v.getId()==R.id.shipin_btn){
				Intent mIntent=new Intent(getBaseContext(), DeviceListActivity.class);
				mIntent.putExtra("playerType", "video");
				startActivity(mIntent);
			}
            else if(v.getId()==R.id.tupian_btn){
            	Intent mIntent=new Intent(getBaseContext(), DeviceListActivity.class);
            	mIntent.putExtra("playerType", "pic");
				startActivity(mIntent);
			}
          
			
		}
	};
	
	private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus){
				
				Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.animation_jump);
			//	animation.setAnimationListener(new animation_jumpListener(view));
				v.setAnimation(animation);
				framlayout.invalidate();
			}
			else{
				v.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.animation_down));
				//layout.invalidate();
				framlayout.invalidate();
			}
          
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dbHelper.clearPlayTime();
		super.onDestroy();
	}
	
	/**
	 * 获取扩展存储路径，TF卡、U盘
	 */
	public  void getExternalStorageDirectory(){
	    String dir = new String();
	    try {
	        Runtime runtime = Runtime.getRuntime();
	        Process proc = runtime.exec("mount");
	        InputStream is = proc.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        String line;
	        BufferedReader br = new BufferedReader(isr);
	        while ((line = br.readLine()) != null) {
	            if (line.contains("secure")) continue;
	            if (line.contains("asec")) continue;
	            
	            if (line.contains("fat")) {
	                String columns[] = line.split(" ");
	                if (columns != null && columns.length > 1) {
	                	for(int i=0;i<columns.length;i++){
	                	       System.out.print("----------------this is dir fat------------"+columns[i]+"\n");
	                	       listpath.add(columns[i]);
	                	}
	                    dir = dir.concat(columns[1] + "\n");
	             
	                }
	            } 
	            else if (line.contains("fuse")) {
	                String columns[] = line.split(" ");
	                if (columns != null && columns.length > 1) {
	                    dir = dir.concat(columns[1] + "\n");
	                    for(int i=0;i<columns.length;i++){
	                	       System.out.print("----------------this is dir fuse------------"+columns[i]+"\n");
	                	       listpath.add(columns[i]);
	                	}
	                }
	            }
	        }
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    handler.sendEmptyMessage(0);
	}
}
