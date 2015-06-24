package com.kt.localmedia.music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.database.MusicDatabaseControl;
import com.kt.localmedia.util.PlayerUtil;

public class MusicBackgroundActivity extends Activity implements OnItemLongClickListener,
OnClickListener{
	private Button mClearBtn,mDeleteBtn,mExitBtn;
	private GridView musicGridView;
	private TextView noMusic;
	private boolean isChice[];
	private int count = 0 ;
	private List<String> deleteList = new ArrayList<String>();
	private List<MusicInfo> musicList = new ArrayList<MusicInfo>();	
	MusicDatabaseControl dbHelper;
	private MusicAdapter musicAdapter; 
	private boolean isDeleteFlag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE); 
	    setContentView(R.layout.background_music_layout);
	    setProgressBarIndeterminateVisibility(true); 
	    findViewById();
	    setViewOnclickListener();  
	    dbHelper = new MusicDatabaseControl(this);
	    setupViews();
	}
	
	private void findViewById(){	  
		  mExitBtn = (Button) findViewById(R.id.music_exit_btn);
		  mDeleteBtn = (Button) findViewById(R.id.music_delete_btn);
		  mClearBtn = (Button) findViewById(R.id.music_clear_btn);
		  musicGridView = (GridView) findViewById(R.id.file_list_view);
		  noMusic = (TextView)findViewById(R.id.no_music);
	}
	
	private void setViewOnclickListener() {		  	  	  
		  mClearBtn.setOnClickListener(this);
		  mExitBtn.setOnClickListener(this);
		  mDeleteBtn.setOnClickListener(this);
		  musicGridView.setOnItemClickListener(new ItemClickListener());
		  musicGridView.setOnItemLongClickListener(this);
	}
	
	private void setupViews() {
		
		new Thread(){
			  @Override
				public void run() {
				  if(musicList!=null){
					  musicList.clear();
				  }
				  musicList = dbHelper.queryMusicList();
				  count = musicList.size();
				  setupViewHandler.sendEmptyMessage(0);
				  super.run();
			  }
		  }.start();
	  }
	
	Handler setupViewHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(0==count){
				noMusic.setVisibility(View.VISIBLE);
				noMusic.setText(R.string.no_bg_music);
				Intent mTntent = new Intent();  
				mTntent.setAction("com.kt.media.MUSIC_SERVICE");  
				stopService(mTntent);
		  	}else{
		  		noMusic.setVisibility(View.GONE);		  		
	        }
			PlayerUtil.setBackgroundMusicList(musicList);
			musicAdapter = new MusicAdapter(getApplicationContext(),musicList);  
			musicGridView.setAdapter(musicAdapter); 
			musicAdapter.notifyDataSetChanged();
			super.handleMessage(msg);
		}
	};
  
	private boolean itemFlag = false;
	private class ItemClickListener implements AdapterView.OnItemClickListener {
	      @Override
		public void onItemClick(AdapterView parent, View v, int position, long id) {	    	  
	    	  if(itemFlag){
	  			itemFlag = false;
	  			return;
	  		  }
	    	  
	    	  if(isDeleteFlag){
		    	  String path = musicList.get(position).getMusicUrl();
		    	  chiceState(position);
		          if (!path.equals("")) { 
			          if(deleteList.contains(path)){  
			        	  deleteList.remove(path);	        
			          }else{
			        	  deleteList.add(path);
			          }
		          }else{
		        	  Toast.makeText(MusicBackgroundActivity.this, getString(R.string.abnormal_operation_please_re_select), Toast.LENGTH_LONG).show();
		          }
	    	  }else{
	    		  String path = musicList.get(position).getMusicUrl();
	    		  File file = new File(path);
	    		  if(!file.exists()){
	    			  Toast.makeText(MusicBackgroundActivity.this, 
	    					  path+getString(R.string.file_not_exist_not_play), Toast.LENGTH_LONG).show();
	    			  return;
	    		  }
	    		  Intent intent = new Intent();  
	    		  intent.setAction("com.kt.media.MUSIC_SERVICE"); 
	    		  intent.putExtra("listPosition", position); 
	    		  intent.putExtra("MSG", MusicConstant.PlayerMag.PLAY_MAG);  
	    		  intent.putExtra("type", MusicConstant.PlayerMag.PLAY_BGMUSIC); 
	    		  intent.setClass(MusicBackgroundActivity.this, MusicService.class);  
	    		  startService(intent);
	    	  }
	      }
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		itemFlag = true;
		if(!isDeleteFlag){
			isDeleteFlag = true;
			setupViews();
		}
		return false;
	}
	
	public void chiceState(int post)
	{
		isChice[post]=isChice[post]==true?false:true;
		musicAdapter.notifyDataSetChanged();
	}
	
	
	public void clearDialog(){
		new AlertDialog.Builder(this)  
			.setTitle(getString(R.string.operation))  
			.setMessage(getString(R.string.empty_the_bgmusic_list))  
			.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {   
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();    
				}   
			})
			.setPositiveButton(getString(R.string.ensure),  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				dbHelper.clearMusicList();  
				setupViews(); 
				dialog.dismiss();
			}  
		}).show(); 
	}
  
	public void deleteDialog(final int num){
		new AlertDialog.Builder(this)  
			.setTitle(getString(R.string.empty_the_bgmusic_list))  
			.setMessage(getString(R.string.remove_the_selected)+num+getString(R.string.remove_the_selected))  
			.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {   
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					isDeleteFlag = false;
					setupViews(); 
					deleteList.clear();
					dialog.dismiss();    
				}   
			})
			.setPositiveButton(getString(R.string.ensure),  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				dbHelper.deleteMusicPath(deleteList);
				isDeleteFlag = false;
				setupViews(); 
				deleteList.clear();
				dialog.dismiss();
				MusicService.setPlayMusicPosition();
			}  
		}).show(); 
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){			  
			case R.id.music_delete_btn:
				if(count>0){
					if(deleteList.size()>0){
						  deleteDialog(deleteList.size());					  
					}
					else{
						  //isDeleteFlag = true;
						  //setupViews();
						  if(isDeleteFlag){
							  Toast.makeText(this, getString(R.string.please_tick_to_remove_music), Toast.LENGTH_LONG).show();
						  }else{
							  Toast.makeText(this, getString(R.string.Long_press_the_OK_button_to_remove_the_selected_music), Toast.LENGTH_LONG).show();
						  }
						  
					}
					
				}else{
					Toast.makeText(this, getString(R.string.not_to_remove_the_music), Toast.LENGTH_LONG).show(); 
				}
			break;
			case R.id.music_clear_btn:			  
				  if(count>0){
					  clearDialog();
				  }else{
					  Toast.makeText(MusicBackgroundActivity.this, getString(R.string.not_to_clear_the_music), Toast.LENGTH_LONG).show(); 
				  }
				  break;
			case R.id.music_exit_btn:
				
				exitDialog();
				break;
		}
	}
	
	private void exitDialog(){
		new AlertDialog.Builder(this)  
			//.setIcon(R.drawable.ic_launcher)  
			.setTitle(getString(R.string.quit))  
			.setMessage(getString(R.string.exit_bg_music))  
			.setNegativeButton(getString(R.string.cancel), null)  
			.setPositiveButton(getString(R.string.ensure),  
		new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				boolean isBgMusicPlaying=MusicService.BGMusicIsPlaying();
				if(isBgMusicPlaying){
					Intent intent = new Intent();  
					intent.setAction("com.kt.media.MUSIC_SERVICE");  
					stopService(intent);					  
				}
				finish();
			}  
		}).show(); 
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(KeyEvent.KEYCODE_BACK==keyCode){
			if(isDeleteFlag){
				isDeleteFlag = false;
				if(deleteList!=null){
					deleteList.clear();
				}
				setupViews();
				return true;
			}		
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/* 
	 * Adapter 
	 */  
	class MusicAdapter extends BaseAdapter {  
		  public Context mContext;  
		  
	      public MusicAdapter(Context context,List<MusicInfo> items) {  
	          this.mContext = context;  
	          isChice = new boolean[items.size()];
		  	  for (int i = 0; i < items.size(); i++) {
		  		 isChice[i]=false;
		  	  }
	      }  
      
	      @Override
	      public int getCount() {  
	          return musicList.size();  
	      }  

	      @Override
		  public Object getItem(int position) {  
	          return musicList.get(position);  
	      }  

	      @Override
	      public long getItemId(int position) {  
	          return position;  
	      }  

	      @Override
	      public View getView(int position, View convertView, ViewGroup parent) {  
	      	ViewHolder viewHolder = null;
				if(viewHolder==null){
					viewHolder = new ViewHolder();
					convertView = LayoutInflater.from(MusicBackgroundActivity.this).inflate(R.layout.music_image_list_item, null);
					viewHolder.dvIcon = (ImageView)convertView.findViewById(R.id.device_list_item_iv);
			        viewHolder.dvName = (TextView)convertView.findViewById(R.id.device_list_item_tv);
			        viewHolder.select = (ImageView) convertView.findViewById(R.id.selected_id);
			        convertView.setTag(viewHolder);
				}
				else{
					viewHolder=(ViewHolder)convertView.getTag();
				}
				  
				viewHolder.dvIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);  
				viewHolder.dvIcon.setPadding(0, 1, 1, 1);  
				viewHolder.dvIcon.setBackgroundResource(R.drawable.music_default_h);  
				int n = musicList.get(position).getMusicUrl().length();
				int m = musicList.get(position).getMusicUrl().lastIndexOf("/");
				String title = musicList.get(position).getMusicUrl().substring(m+1, n);
				viewHolder.dvName.setText(title);
				
				if(isDeleteFlag){
					if (isChice[position]== true){  
						 Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.select);
						 viewHolder.select.setImageBitmap(bitmap);
						 viewHolder.select.setVisibility(View.VISIBLE);
					 }else{
						 Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.unselect);
						 viewHolder.select.setImageBitmap(bitmap);
						 viewHolder.select.setVisibility(View.VISIBLE);
					 }
				}
	          return convertView;  
	      }  
	      
	      class ViewHolder{
			  public ImageView dvIcon;
			  public TextView dvName;
			  public ImageView select;
		  }
	  }
	
	@Override 
	  public void onDestroy() {  
		super.onDestroy(); 
	  }
}