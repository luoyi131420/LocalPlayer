package com.kt.localmedia;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.localmedia.files.FilesManager;
import com.kt.localmedia.util.CustomProgressDialog;



public class DeviceListActivity extends Activity implements OnItemClickListener{
	private static final String TAG = "DeviceListActivity";
	private GridView deviceGv = null;
	private List<String> dvList;
	private String currentPath="";
	private FilesManager fm;
	private CustomProgressDialog dialog;
	private TextView mNoDevice;
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
		   Log.i(TAG,"---------------------this is receiver-------------"+intent.getAction());
		   if(intent.getAction()==Intent.ACTION_MEDIA_REMOVED
				   ||intent.getAction()==Intent.ACTION_MEDIA_CHECKING
				   ){
			    dialog.show();
			    try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				initDevicePath();				
		   }
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list_layout);
		currentPath = getIntent().getStringExtra("playerType");
		deviceGv = (GridView)findViewById(R.id.device_list_gridview);
		mNoDevice = (TextView)findViewById(R.id.no_device_title);
		deviceGv.setOnItemClickListener(this);
		fm = new FilesManager();
		dialog =  CustomProgressDialog.createDialog(DeviceListActivity.this);
		dialog.setTitile(getString(R.string.loading));
		dialog.setMessage(getString(R.string.loading));
	    dialog.show();
		initDevicePath();
		IntentFilter filter = new IntentFilter(); 
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED); 
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED); 
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");              
        registerReceiver(mReceiver, filter); 
	}

	Handler deviceHandler= new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			deviceGv.setAdapter(new DeviceAdapter());
			if(dvList.size()==0){
				mNoDevice.setVisibility(View.VISIBLE);
			}else{
				mNoDevice.setVisibility(View.GONE);
			}
			super.handleMessage(msg);
		} 
		
	};
	private void initDevicePath(){
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(dvList!=null){
					dvList.clear();
				}
				
				//System.out.println("===================this is dvlist1----------------"+dvList);	
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dvList = fm.getExternalStorageDirectory();
			    //System.out.println("===================this is dvlist2----------------"+dvList.size());	
				while(true){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(dvList!=null&&!"".equals(dvList)){
					
						deviceHandler.sendEmptyMessage(0);
						break;
					}
				}
			}
			
		}.start();
	}
	
	
	class DeviceAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return dvList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder = null;
			if(viewHolder==null){
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(DeviceListActivity.this).inflate(R.layout.device_list_item, null);
				viewHolder.dvIcon = (ImageView)convertView.findViewById(R.id.device_list_item_iv);
		        viewHolder.dvName = (TextView)convertView.findViewById(R.id.device_list_item_tv);
		        convertView.setTag(viewHolder);
			}
			else{
				viewHolder=(ViewHolder)convertView.getTag();
			}
		
			if(dvList.get(position).equals("/mnt/usb_storage/USB_DISK1/udisk0")){
				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_u_icon);
				viewHolder.dvName.setText(R.string.usb);
			}
			else if(dvList.get(position).equals("/storage/udisk2")){
				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_u_icon);
				viewHolder.dvName.setText(R.string.usb2);
			}
			else if(dvList.get(position).contains("sdcard")){
				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_sd_icon);
				viewHolder.dvName.setText(R.string.tf);
			}
//			else if(dvList.get(position).equals("/storage/udisk3")){
//				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_u_icon);
//				viewHolder.dvName.setText(R.string.usb3);
//			}else if(dvList.get(position).equals("/storage/udisk4")){
//				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_u_icon);
//				viewHolder.dvName.setText(R.string.usb4);
//			}else if(dvList.get(position).equals("/storage/udisk5")){
//				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_u_icon);
//				viewHolder.dvName.setText(R.string.usb5);
//			}
//			else{
//				viewHolder.dvIcon.setBackgroundResource(R.drawable.device_icon);
//				viewHolder.dvName.setText("分区"+(position+1));
//			}
			
		
			return convertView;
		}
		
	  class ViewHolder{
		  public ImageView dvIcon;
		  public TextView dvName;
	  }
	
	}

	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent =new Intent(DeviceListActivity.this,FileListActivity.class);
		if("music".equals(currentPath)){
			intent.putExtra("file_type", "music");
		}
		else if("pic".equals(currentPath)){
			intent.putExtra("file_type", "pic");
		}
		else if("video".equals(currentPath)){
			intent.putExtra("file_type", "video");
		}	
		intent.putExtra("file_path", dvList.get(position));
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mReceiver!=null)
			{this.unregisterReceiver(mReceiver);}
		super.onDestroy();
	}
}
