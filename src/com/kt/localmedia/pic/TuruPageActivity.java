package com.kt.localmedia.pic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.pic.anim.BlackSquareFadeAway;
import com.kt.localmedia.pic.anim.ScalMpic;
import com.kt.localmedia.pic.anim.ShutterDown2Up;
import com.kt.localmedia.pic.anim.ShutterLeft2Right;
import com.kt.localmedia.pic.anim.ShutterRight2Left;
import com.kt.localmedia.pic.anim.ShutterUp2Down;
import com.kt.localmedia.pic.anim.StretchOut;
import com.kt.localmedia.pic.anim.TranslateDown;
import com.kt.localmedia.pic.anim.TranslateLeft;
import com.kt.localmedia.pic.anim.TranslateRight;
import com.kt.localmedia.pic.anim.TranslateUp;
import com.kt.localmedia.pic.widget.TurnPageView;
import com.kt.localmedia.util.GLFiles;


public class TuruPageActivity extends Activity {

	private TurnPageView mTurnPageView=null;
	private String []mBitmaps;
	private List<String> listBitmaps = new ArrayList<String>();
	private ShutterLeft2Right shutterLeft2Right;
	private ShutterRight2Left shutterRight2Left;
	private ShutterDown2Up shutterDown2Up;
	private ShutterUp2Down shutterUp2Down;
	private TranslateRight translateRight;
	private TranslateLeft translateLeft;
	private TranslateDown translateDown;
	private TranslateUp translateUp;
	private BlackSquareFadeAway blackSquareFadeAway;
	private StretchOut stretchOut;
	private int curBitmapIndex=0;
	private int timeSpace=3000;//时间间隔
    private int currentMode;//播放模式
	private boolean playstate=false;
	private boolean isbreak=false;
	private boolean isstart=false;
	private Bitmap lastBitmap;
	private List<File> listFiles=null;
	private int FileNum;
	private String FIRST,LAST;
//	private boolean keyBiao=true;
	private boolean loop=true;
    private int currentRotate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	
		FIRST = getResources().getString(R.string.firstone);
		LAST  = getResources().getString(R.string.lastone);

		initThread();
		 
		initMPageThread();
		
		//checkKEYTime();
		
	}
	
	private void initMPageThread(){
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					try {
						Thread.sleep(50);
						if(isstart){
							playPageThread();
							break;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			}
			
		}.start();
		
		
	}
	Handler handler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			shutterLeft2Right = new ShutterLeft2Right();
			shutterRight2Left = new ShutterRight2Left();
			shutterDown2Up    = new ShutterDown2Up();
			shutterUp2Down    = new ShutterUp2Down();
			translateRight    = new TranslateRight();
			translateLeft     = new TranslateLeft();
			translateDown     = new TranslateDown();
			translateUp       = new TranslateUp();
			blackSquareFadeAway = new BlackSquareFadeAway();
			stretchOut        = new StretchOut();
			mTurnPageView=new TurnPageView(TuruPageActivity.this);
		//	mTurnPageView=(TurnPageView)findViewById(R.id.page_surfaceview);
			System.out.println("--------------this is bitmapFactory2------------"+playstate+"---------"+curBitmapIndex);
		
			mTurnPageView.setTurnPageStyle(translateLeft);
			Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
			mTurnPageView.setBitmaps(new Bitmap[]{bitmap});
			lastBitmap =bitmap;
			setContentView(mTurnPageView);
//			playstate=true;
			isbreak=true;
		//	isstart=true;
		}
		//dumpsys meminfo
		
	};
	
	private void initThread(){
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
				String picPath=	getIntent().getStringExtra("pic_path");
				int picIndex = getIntent().getIntExtra("pic_index", 0);
			
				GLFiles glF = new GLFiles();
				listFiles =glF.getIMGList(picPath);
				if(listFiles!=null){
			    if(!listBitmaps.isEmpty()){
			    	
			    	listBitmaps.clear();
			    }
				
			    System.out.println("--------------this is bitmapFactory4------------"+curBitmapIndex+"---------");
				for(int i=0;i<listFiles.size();i++)
				{
					if(listFiles.get(i).isFile()){
						System.out.println("--------------this is test1------------"+listBitmaps.size()+"---------"+picIndex);
						
						listBitmaps.add(listFiles.get(i).getAbsolutePath());
                     
					}
					else{
						FileNum++;
					}
					
					if(picIndex==i){
                     	System.out.println("--------------this is test2------------"+listBitmaps.size()+"------"+picIndex+"------"+FileNum);
                     	curBitmapIndex=picIndex-FileNum;
                     	System.out.println("--------------this is test3------------"+curBitmapIndex);
					}
					
				}
				mBitmaps = new String[listBitmaps.size()];
				for(int j=0;j<listBitmaps.size();j++){
					mBitmaps[j]=listBitmaps.get(j);
				}
			
				handler.sendEmptyMessage(0);
				super.run();
			}
			}	
		}.start();
		
	};
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
			  System.out.println("--------------this is onKeyDown------------"+keyCode);
		//	pageDialogs.showSystemDialog(this, "title", "content");
			if(keyCode==23){
				  MDialog   dialog =new MDialog(this, R.style.dialog);
					//获得当前窗体
					  Window window = dialog.getWindow();

					  //重新设置
					  WindowManager.LayoutParams lp = window.getAttributes();
					  window .setGravity(Gravity.CENTER | Gravity.BOTTOM);
					  lp.x = 0; // 新位置X坐标
					  lp.y = 50; // 新位置Y坐标
					  lp.alpha = 1.0f; // 透明度
					  dialog.show();
			}
			
			if(mTurnPageView==null){
				return false;
			}
			if(!playstate&&mTurnPageView.isok){
				
				mTurnPageView.isok=false;
			
				 if(lastBitmap!=null&&!lastBitmap.isRecycled()){
					 lastBitmap.recycle();
					 System.gc();
				 }
				if(keyCode==21){
			
					if(curBitmapIndex>=1){
						currentRotate=0;
						    curBitmapIndex--;
						    mTurnPageView.setTurnPageStyle(translateRight);
						    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
							mTurnPageView.setBitmaps(new Bitmap[]{bitmap});
							lastBitmap=bitmap;
						    mTurnPageView.gonextView();
						   
					}
					else{
						Toast.makeText(this, FIRST, Toast.LENGTH_SHORT).show();
						mTurnPageView.isok=true;
					}
					
				}
				else if(keyCode==22){
				
					if(curBitmapIndex<mBitmaps.length-1){
						currentRotate=0;
						    curBitmapIndex++;
						    mTurnPageView.setTurnPageStyle(translateLeft);
						    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
							mTurnPageView.setBitmaps(new Bitmap[]{bitmap});
							lastBitmap=bitmap;
						    mTurnPageView.gonextView();
						 
						  
					}
					else{
						Toast.makeText(this, LAST, Toast.LENGTH_SHORT).show();
						mTurnPageView.isok=true;
					}
				
				}
				else if(keyCode==19){
					    currentRotate=currentRotate+90;
					    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
					    Matrix matrix = new Matrix();
				        matrix.postRotate(currentRotate);        /*翻转180度*/
				        int width = bitmap.getWidth();
				        int height = bitmap.getHeight();
				        Bitmap img_a = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				        mTurnPageView.setTurnPageStyle(translateLeft);
				        mTurnPageView.setBitmaps(new Bitmap[]{img_a});
				        mTurnPageView.gonextView();
				        
				}
				else if(keyCode==20){
					    currentRotate=currentRotate-90;
					    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
					    Matrix matrix = new Matrix();
				        matrix.postRotate(currentRotate);        /*翻转180度*/
				        int width = bitmap.getWidth();
				        int height = bitmap.getHeight();
				        Bitmap img_a = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				        mTurnPageView.setTurnPageStyle(translateRight);
				        mTurnPageView.setBitmaps(new Bitmap[]{img_a});
				        mTurnPageView.gonextView();
				}
			
			}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public Bitmap loadResBitmap(String path,int scalSize){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds=false;
		options.inSampleSize=scalSize;
		Bitmap bmp =BitmapFactory.decodeFile(path, options);
		return bmp;
	}
	
	public  Bitmap fitSizeImg(String path) {
      
         Bitmap resizeBmp = null;
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inPreferredConfig = Bitmap.Config.RGB_565;   
         options.inPurgeable=true;
         options.inInputShareable=true;
		try {
			FileInputStream is;
			File file = new File(path);
			if(file!=null&&file.exists()){
				System.out.println("--------------------this is liujun file not null-------------");
				is = new FileInputStream(file);
				resizeBmp = BitmapFactory.decodeStream(is,null,options);
				if(resizeBmp==null){
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					is = new FileInputStream(file);
					resizeBmp = BitmapFactory.decodeStream(is,null,options);
				}
			}
			else{
				System.out.println("--------------------this is liujun file  null----------------");
				isbreak=false;
				this.finish();
			}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
        return resizeBmp;
     }
	
	

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		curBitmapIndex = data.getIntExtra("curBitmapIndex", 0);
//		super.onActivityResult(requestCode, resultCode, data);
//	}


	/*
	 * 启动图片播放线程
	 * */
	private void playPageThread(){
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			while(isbreak){
				try {
					  System.out.println("--------------this is bitmapFactory5------------"+timeSpace);
				Thread.sleep(timeSpace);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(playstate)
				{
					setPlayMode();//播放模式
//						curBitmapIndex++;
//					if(curBitmapIndex>=mBitmaps.length)
//					{
//						curBitmapIndex=0;
//					}
					
					
						Random rand = new Random();
						 int turnNum  = rand.nextInt(11);
						 System.out.println("------------this is turnum---------------"+turnNum);
						 if(turnNum==0){
								mTurnPageView.setTurnPageStyle(shutterLeft2Right);
						 }
						 else if(turnNum==1){
							 mTurnPageView.setTurnPageStyle(shutterRight2Left);
							  
						 }
						 else if(turnNum==2){
							 mTurnPageView.setTurnPageStyle(translateRight);
							
						 }
						 else if(turnNum==3){
							 mTurnPageView.setTurnPageStyle(translateLeft);
						 }
						 else if(turnNum==4){
							 mTurnPageView.setTurnPageStyle(translateDown);
						 }
						 else if(turnNum==5){
							 mTurnPageView.setTurnPageStyle(translateUp);
						 }
						 else if(turnNum==6){
							 mTurnPageView.setTurnPageStyle(blackSquareFadeAway);
						 }
						 else if(turnNum==7){
							 mTurnPageView.setTurnPageStyle(blackSquareFadeAway);
						 }
						 else if(turnNum==8){
							 mTurnPageView.setTurnPageStyle(stretchOut);
						 }
						 else if(turnNum==9){
							 mTurnPageView.setTurnPageStyle(shutterDown2Up);
						 }
						 else if(turnNum==10){
							 mTurnPageView.setTurnPageStyle(shutterUp2Down);
						 }
						
						 if(lastBitmap!=null&&!lastBitmap.isRecycled()){
							 lastBitmap.recycle();
							 System.gc();
						 }
						 System.out.println("----------------this is system--------------"+mBitmaps[curBitmapIndex]);
						    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
							mTurnPageView.setBitmaps(new Bitmap[]{bitmap});
							lastBitmap=bitmap;
						    mTurnPageView.gonextView();
					   }
					
					
				   }
				
			}
			
		}.start();
	}
	@Override
	protected void onResume() {
		 System.out.println("----------------this is system-----------------------"+curBitmapIndex);
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
	    isbreak=false;
		loop=false;		
		super.onDestroy();
	}

	class MDialog extends Dialog implements android.view.View.OnClickListener,OnItemSelectedListener{

		private Button playBtn;
	    private Spinner timeSpinner;
	    private Button modeBtn;
	    private Button musicBtn;
	    private Button addBtn,jianBtn;
	    private int currentNum=5;
		public MDialog(Context context, int theme) {
			super(context, theme);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			 setContentView(R.layout.pic_dialog_main);
			 
			 playBtn = (Button)findViewById(R.id.play_pause_button);
			 timeSpinner = (Spinner)findViewById(R.id.time_spinner);
			 modeBtn  = (Button)findViewById(R.id.play_mode);
			 //musicBtn = (Button)findViewById(R.id.bg_music_button);
			 addBtn   = (Button)findViewById(R.id.pic_scalBtn_add);
			 jianBtn  = (Button)findViewById(R.id.pic_scalBtn_jian);
//		     modeBtn.setBackground(getResources().getDrawable(R.drawable.mode_shunxu_selector));
		     modeBtn.setBackgroundResource(R.drawable.mode_shunxu_selector);
			// 建立数据源
			String[] mItems = getResources().getStringArray(R.array.spinnername);
			// 建立Adapter并且绑定数据源
			ArrayAdapter<String> _Adapter=new ArrayAdapter<String>(TuruPageActivity.this,R.layout.spinner_checked_text, mItems);
			//绑定 Adapter到控件
			_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			timeSpinner.setAdapter(_Adapter);
			timeSpinner.setOnItemSelectedListener(this);
			playBtn.setOnClickListener(this);
			modeBtn.setOnClickListener(this);
			//musicBtn.setOnClickListener(this);
			addBtn.setOnClickListener(this);
			jianBtn.setOnClickListener(this);
			initPlaystate();
			
		}

		public void initPlaystate(){ 
			 if(playstate){
					
					playBtn.setBackgroundResource(R.drawable.pause_btn_selector);
				}
				else {
				
					playBtn.setBackgroundResource(R.drawable.play_btn_selector);
				}
	
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId()==R.id.play_pause_button){
				System.out.println("--------------this is onKeyDown------------"+playstate);
				if(playstate){
					playstate=false;
					isstart=false;
					playBtn.setBackgroundResource(R.drawable.play_btn_selector);
				}
				else{
					isstart=true;
					playstate=true;
					playBtn.setBackgroundResource(R.drawable.pause_btn_selector);
				}
			}
			else if(v.getId()==R.id.play_mode){
				currentMode++;
				if(currentMode>=3){
					currentMode=0;
				}
				switch(currentMode){
				case 0:
					  modeBtn.setBackgroundResource(R.drawable.mode_shunxu_selector);
					  Toast.makeText(TuruPageActivity.this, "顺序模式", Toast.LENGTH_SHORT).show();
				    break;
				case 1:
					  modeBtn.setBackgroundResource(R.drawable.mode_suiji_selector);
					  Toast.makeText(TuruPageActivity.this, "随机模式", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					  modeBtn.setBackgroundResource(R.drawable.mode_xunhuan_selector);
					  Toast.makeText(TuruPageActivity.this, "循环模式", Toast.LENGTH_SHORT).show();
					break;
				
				}
			}
//			else if(v.getId()==R.id.bg_music_button){
//				
//				Intent intent = new Intent(TuruPageActivity.this,MusicListDialog.class);
//				intent.putExtra("curBitmapIndex",curBitmapIndex);
//				TuruPageActivity.this.startActivity(intent);
//			
//			}
			else if(v.getId() == R.id.pic_scalBtn_add&&!playstate){
				
				 currentNum++;
				 if(currentNum>=10){
						currentNum=10;
					}
					     	ScalMpic mpic = new ScalMpic(0.2f*currentNum);
					
						    mTurnPageView.setTurnPageStyle(mpic);
						    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
							mTurnPageView.setBitmaps(new Bitmap[]{bitmap});
							lastBitmap=bitmap;
						    mTurnPageView.gonextView();
			
			}
			else if(v.getId()==R.id.pic_scalBtn_jian&&!playstate){
				 currentNum--;
				 if(currentNum<=0){
	                	currentNum=1;
	                }
			
		     	ScalMpic mpic = new ScalMpic(0.2f*currentNum);
		
			    mTurnPageView.setTurnPageStyle(mpic);
			    Bitmap bitmap =fitSizeImg(mBitmaps[curBitmapIndex]);
				mTurnPageView.setBitmaps(new Bitmap[]{bitmap});
				lastBitmap=bitmap;
			    mTurnPageView.gonextView();
			}
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			   String str=parent.getItemAtPosition(position).toString();
			   int timenum = Integer.parseInt(str);
			   timeSpace =timenum*1000;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		this.finish();
		super.onStop();
	}

	private void setPlayMode(){
		switch(currentMode){
		case 0:
			shunxuPlay();
		    break;
		case 1:
			suijiPlay();
			break;
		case 2:
			xunhuanPlay();
			break;
		
		}
	}
	
	private void suijiPlay(){
		int randomNum = new Random().nextInt(mBitmaps.length);
		curBitmapIndex=randomNum;
	}
	
	private void shunxuPlay(){
		if(curBitmapIndex<mBitmaps.length-1)
		{
			curBitmapIndex++;
			
		}
		 if(curBitmapIndex>=mBitmaps.length-1){
			isbreak=false;
		}
	}
	
	private void xunhuanPlay(){
		curBitmapIndex++;
		if(curBitmapIndex>=mBitmaps.length)
		{
			curBitmapIndex=0;
		}
	}
	
}
