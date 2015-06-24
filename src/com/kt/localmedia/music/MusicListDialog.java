package com.kt.localmedia.music;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kt.localmedia.R;
import com.kt.localmedia.database.MusicDatabaseControl;
import com.kt.localmedia.util.Constant;

public class MusicListDialog extends Activity implements OnItemLongClickListener{
	private static final String TAG = "KTMusicListActivity";
	private TextView mCurrentMusicTitle,mNoBgMusicTitle;
	private List<MusicInfo> musicList = new ArrayList<MusicInfo>();	
	private MusicListAdapter mAdapter;
	private ListView musicListView;
	private int listPosition = 0;      
	private MusicReceiver musicReceiver;   
	MusicDatabaseControl dbHelper;
	private MusicService musicService; 
	private boolean flag;
	
//	private ServiceConnection mServiceConnection = new ServiceConnection() {                
//		@Override        
//		public void onServiceConnected(ComponentName name, IBinder service) {             
//			musicService = ((MusicService.LocalBinder)(service)).getService();
//			flag = true ;
//			System.out.println("----->onServiceConnected success");
//		}           
//      
//		@Override       
//		public void onServiceDisconnected(ComponentName name) {               
//		}       
//	};
//	
//	private void connectToPlayerService(){               
//		 Intent intent = new Intent(MusicListDialog.this, MusicService.class);                      
//		 bindService(intent, mServiceConnection, BIND_AUTO_CREATE);  
//	} 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bg_music_list_layout);
		
		mCurrentMusicTitle = (TextView) findViewById(R.id.current_music_title);  
		mNoBgMusicTitle  = (TextView) findViewById(R.id.no_bg_music_title);
		musicListView = (ListView) findViewById(R.id.all_music_list);  		
		musicListView.setOnItemClickListener(new MusicListItemClickListener());
		musicListView.setOnItemLongClickListener(MusicListDialog.this);
		
		dbHelper = new MusicDatabaseControl(MusicListDialog.this);
		if(musicList!=null){
			musicList.clear();
		}
		musicList = dbHelper.queryMusicList();
		Log.i(TAG,"===musicList.size()="+musicList.size());
		if(musicList.size()>0){
			//connectToPlayerService(); 
			mNoBgMusicTitle.setVisibility(View.GONE);
			//PlayerUtil.setKTMusicList(musicList);
			mAdapter = new MusicListAdapter(getApplicationContext(),musicList);
			musicListView.setAdapter(mAdapter);
		}else{
			mNoBgMusicTitle.setVisibility(View.VISIBLE);
		}
		musicReceiver = new MusicReceiver(); 
		IntentFilter filter = new IntentFilter();  
		filter.addAction(Constant.UPDATE_TITLE);  
		filter.addAction(Constant.STOP_ACTION);
		registerReceiver(musicReceiver, filter); 
	}
	
	private boolean itemFlag = false;
	private class MusicListItemClickListener implements OnItemClickListener {  
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
			if(itemFlag){
				itemFlag = false;
				return;
			}
			listPosition = position;  
//			if(flag){
//				musicService.playMusic(listPosition); 
//			}
			playMusic(listPosition);
		}

	}
	
	public static List<String> addList = new ArrayList<String>();
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		itemFlag = true;
		String path = musicList.get(position).getMusicUrl();
		if(addList!=null){
			addList.clear();
		}
		addList.add(path);
		deleteDialog();
		return false;
	}
	 
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if (keyCode == KeyEvent.KEYCODE_BACK  
				&& event.getAction() == KeyEvent.ACTION_DOWN) {  		
		}  
		return super.onKeyDown(keyCode, event);  
	}
	
	public void playMusic(int pos) { 
		if(musicList!=null){
			int n = musicList.get(pos).getMusicUrl().length();
			int m = musicList.get(pos).getMusicUrl().lastIndexOf("/");
			String title = musicList.get(pos).getMusicUrl().substring(m+1, n);
			mCurrentMusicTitle.setText("正在播放："+title);
			Intent intent = new Intent();  
			intent.setAction("com.kt.media.MUSIC_SERVICE"); 
			intent.putExtra("listPosition", listPosition);  
			intent.setClass(MusicListDialog.this, MusicService.class);  
			startService(intent); 
		}
	}
	
	private void deleteDialog(){
		new AlertDialog.Builder(this)  
			//.setIcon(R.drawable.ic_launcher) 
			.setTitle("操作提示")  
			.setMessage("是否从背景音乐列表中删除？")  
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {   
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();    
				}   
			})
			.setPositiveButton("确定",  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				dbHelper = new MusicDatabaseControl(MusicListDialog.this);
				boolean flag = dbHelper.queryMusicPath(addList.get(0));
				if(flag){
					dbHelper.deleteMusicPath(addList);
					musicList = dbHelper.queryMusicList();
					if(musicList.size()>0){
						//PlayerUtil.setKTMusicList(musicList);
						mAdapter = new MusicListAdapter(getApplicationContext(),musicList);
						musicListView.setAdapter(mAdapter);
					}else{
						mNoBgMusicTitle.setVisibility(View.VISIBLE);
					}
					mAdapter.notifyDataSetChanged();
				}
				dialog.dismiss();
			}  
		}).show(); 
	}
	
	@Override  
	protected void onDestroy() {
		if(musicReceiver!=null){
			unregisterReceiver(musicReceiver);
		}
		//flag = false;
		super.onDestroy();  
	}
	
	class MusicListAdapter extends BaseAdapter {  
		  public Context mContext;  
		  
	      public MusicListAdapter(Context context,List<MusicInfo> items) {  
	          this.mContext = context;  
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
					convertView = LayoutInflater.from(MusicListDialog.this).inflate(R.layout.bg_music_list_item, null);
					viewHolder.dvIcon = (ImageView)convertView.findViewById(R.id.icon);
			        viewHolder.dvTitle = (TextView)convertView.findViewById(R.id.music_title);
			        convertView.setTag(viewHolder);
				}
				else{
					viewHolder=(ViewHolder)convertView.getTag();
				}
				viewHolder.dvIcon.setBackgroundResource(R.drawable.music_icon);  
				int n = musicList.get(position).getMusicUrl().length();
				int m = musicList.get(position).getMusicUrl().lastIndexOf("/");
				String title = musicList.get(position).getMusicUrl().substring(m+1, n);
				viewHolder.dvTitle.setText(title);
	          return convertView;  
	      }  
	      
	      class ViewHolder{
			  public ImageView dvIcon,select,unselect;
			  public TextView dvTitle,dvArtist,dvDuration;
		  }
	  }
	
	public class MusicReceiver extends BroadcastReceiver { 
		@Override 
		public void onReceive(Context context, Intent intent) {  
			String action = intent.getAction(); 
			Log.i(TAG,"------>action="+action);
			if(action.equals(Constant.UPDATE_TITLE)){
				int playOverFlag = intent.getIntExtra("playover", -1); 
				listPosition = intent.getIntExtra("currentPoisition", -1);  
				if(listPosition >= 0) {  
					int n = musicList.get(listPosition).getMusicUrl().length();
					int m = musicList.get(listPosition).getMusicUrl().lastIndexOf("/");
					String title = musicList.get(listPosition).getMusicUrl().substring(m+1, n);
					mCurrentMusicTitle.setText("正在播放："+title);  
				} 
				if(1==playOverFlag){
					mCurrentMusicTitle.setText(R.string.play_over);  
				}
			}else if(action.equals(Constant.STOP_ACTION)){
				Intent mintent = new Intent(MusicListDialog.this, MusicService.class);  
				stopService(mintent);
			}
		}	
	}
}