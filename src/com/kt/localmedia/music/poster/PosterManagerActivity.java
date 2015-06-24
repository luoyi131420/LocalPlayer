package com.kt.localmedia.music.poster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.database.PosterDatabaseControl;

public class PosterManagerActivity extends Activity implements OnClickListener{
  private static final String TAG = "PosterManager";
  PosterDatabaseControl dbHelper;
  private Button mAddBtn,mClearBtn,mDeleteBtn;
  private Intent mIntent;
  private static List<PosterInfo> posterList = new ArrayList<PosterInfo>();	
  private List<String> deleteList = new ArrayList<String>();
  private PosterAdapter posterAdapter;  
  private GridView posterGridView;
  private boolean isChice[];
  private TextView noPoster;
  private int count = 0 ;
  
  public static void clearPosterList() {
	    if (posterList != null) {
	    	posterList.clear();
	    	posterList = null;
	    }
  }
  
  private Handler handler = new Handler() {            
  	@Override
	public void handleMessage(Message msg) {                  
  		setupViews();  
  		//updateViews();
  		super.handleMessage(msg);  
  	}        
  };
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE); 
    setContentView(R.layout.music_poster_manager);
    setProgressBarIndeterminateVisibility(true); 
    findViewById();
    setViewOnclickListener();  
    dbHelper = new PosterDatabaseControl(this);
  }
    
  @Override 
  protected void onResume(){
	  setupViews();	
	  super.onResume();
  }
   
  @Override 
  public void onDestroy() {  
	 if(!photos.isEmpty()){
		for(int i=0;i<photos.size();i++){
			photos.get(i).getBitmap().recycle();
		}
	}
	handler.removeCallbacks(r);
	super.onDestroy(); 
  }
		
  @Override  
  protected void onStop() {  
	super.onStop();  
  }
  
  private void findViewById(){	  
	  //mAddBtn = (Button) findViewById(R.id.poster_add_btn);
	  mDeleteBtn = (Button) findViewById(R.id.poster_delete_btn);
	  mClearBtn = (Button) findViewById(R.id.poster_clear_btn);
	  posterGridView = (GridView) findViewById(R.id.file_list_view);
	  noPoster = (TextView)findViewById(R.id.no_poster);
  }
  
  private void setViewOnclickListener() {		  	  	  
	  mClearBtn.setOnClickListener(this);
	  //mAddBtn.setOnClickListener(this);
	  mDeleteBtn.setOnClickListener(this);
	  posterGridView.setOnItemClickListener(new ItemClickListener());
  }
 
  private void updateViews(){
	  if(!photos.isEmpty()){
		  photos.clear();
			System.gc();
	  }
	  dbHelper = new PosterDatabaseControl(this);
	  posterList=dbHelper.queryPosterList();  
	  posterAdapter = new PosterAdapter(getApplicationContext(),posterList);  
	  posterGridView.setAdapter(posterAdapter);
	  posterAdapter.notifyDataSetChanged();
	  //handler.removeCallbacks(r);
  }
  
 
  private void waitforAsyncTaskPic(){}
  
  Handler setupViewHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(0==count){
				noPoster.setVisibility(View.VISIBLE);
				noPoster.setText(R.string.no_poster);
		  	}else{
		  		noPoster.setVisibility(View.GONE);
	        }
			posterAdapter = new PosterAdapter(getApplicationContext(),posterList);  
			posterGridView.setAdapter(posterAdapter); 
		  	new AsyncLoadedImage().executeOnExecutor(Executors.newCachedThreadPool());
		  	
			super.handleMessage(msg);
		}
  };
  
  private void setupViews() {
	  //clearPosterList();
	  posterList = dbHelper.queryPosterList();
	  new Thread(){
		  @Override
			public void run() {
			  if(!photos.isEmpty()){
				  photos.clear();
					System.gc();
			  }
			  count = posterList.size();
			  for(int i=0;i<posterList.size();i++){
				  photos.add(new LoadedImage(getRes("pic_default"),i,posterList));
			  }
			  setupViewHandler.sendEmptyMessage(0);
			  super.run();
		  }
	  }.start();
	  waitforAsyncTaskPic();//异步加载缩略图片
  }
  
  public Bitmap getRes(String name) {
	  	ApplicationInfo appInfo = getApplicationInfo();
	  	int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
	  	return BitmapFactory.decodeResource(getResources(), resID);
  }
  
  class AsyncLoadedImage extends AsyncTask<Object, LoadedImage, Object>{
	  	public AsyncLoadedImage(){}
	    @Override  
	    protected Object doInBackground(Object... params) {  
	      	  
	              Bitmap bitmap;  
	              Bitmap newBitmap;

	              for (int i = 0; i < posterList.size(); i++) {  
	                  	 try {  
	                           BitmapFactory.Options options = new BitmapFactory.Options();  
	                           options.inSampleSize = 15;  
	                           
		                       bitmap = BitmapFactory.decodeFile(posterList.get(i).getPosterPath(), options);  
		                       if (bitmap != null){   
		                           newBitmap = ThumbnailUtils.extractThumbnail(bitmap,115, 80);  
		                           bitmap.recycle();  
		                           if (newBitmap != null) {  
		                        	  publishProgress(new LoadedImage(newBitmap,i,posterList));  
		                              Thread.sleep(100);
		                           }  
	                           }
	                           
	                       } catch (Exception e) {  
	                           e.printStackTrace();  
	                       }                  
	              }   
	          return null;  
	      }  

	      //实时更新UI  onProgressUpdate()方法的参数对应于doInBackground中publishProgress方法的参数，同时也对应于  
	      //doInBackground的第二个参数  
	      @Override  
	      public void onProgressUpdate(LoadedImage... value) {  
	          addImage(value);  
	      }  

	      //更新UI结束后的处理  
	      @Override  
	      protected void onPostExecute(Object result) {  
	          setProgressBarIndeterminateVisibility(false); 
	      }  
	      
	      @Override
	      protected void onCancelled() { 
	         super.onCancelled(); 
	      } 	  
  }
  
  /* 
   * 刷新Adapter 
   */  
  private void addImage(LoadedImage... value) {  
      for (LoadedImage image : value) {  
    	  posterAdapter.addPhoto(image);  
          posterAdapter.notifyDataSetChanged();
      }  
  } 
  
  /* 
   * Adapter 
   */  
  private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();  
  class PosterAdapter extends BaseAdapter {  
	  public Context mContext;  
	  
      public PosterAdapter(Context context,List<PosterInfo> items) {  
          this.mContext = context;  
          isChice = new boolean[items.size()];
	  	  for (int i = 0; i < items.size(); i++) {
	  		 isChice[i]=false;
	  	  }
      }  

      public void addPhoto(LoadedImage photo) {  
          int i =photo.getIndex();
          if(photos.size()>0&& i<photos.size()){
        	  photos.set(i,photo);
          }
      }  
      
      @Override
	public int getCount() {  
          return photos.size();  
      }  

      @Override
	public Object getItem(int position) {  
          return photos.get(position);  
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
				convertView = LayoutInflater.from(PosterManagerActivity.this).inflate(R.layout.music_image_list_item, null);
				viewHolder.dvIcon = (ImageView)convertView.findViewById(R.id.device_list_item_iv);
		        viewHolder.dvName = (TextView)convertView.findViewById(R.id.device_list_item_tv);
		        viewHolder.select = (ImageView) convertView.findViewById(R.id.selected_id);
		        convertView.setTag(viewHolder);
			}
			else{
				viewHolder=(ViewHolder)convertView.getTag();
			}
			  
			if(photos.size()>0){
				viewHolder.dvIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);  
				viewHolder.dvIcon.setPadding(0, 1, 1, 1);  
				viewHolder.dvIcon.setImageBitmap(photos.get(position).getBitmap());  
				int n = posterList.get(position).getPosterPath().length();
				int m = posterList.get(position).getPosterPath().lastIndexOf("/");
				String title = posterList.get(position).getPosterPath().substring(m+1, n);
				viewHolder.dvName.setText(title);
			}
			if (isChice[position]== true){  
				 Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.selected);
				 viewHolder.select.setImageBitmap(bitmap);
				 viewHolder.select.setVisibility(View.VISIBLE);
			 }else{
				 viewHolder.select.setVisibility(View.GONE);
			 }
          return convertView;  
      }  
      
      class ViewHolder{
		  public ImageView dvIcon;
		  public TextView dvName;
		  public ImageView select;
	  }
  }
  private  class LoadedImage {  
      
      Bitmap mBitmap;  
      int index;
      List<PosterInfo> file;
      /*
       * @param 
       * bitmap 缩略图片
       * path   图片路径
       * index  图片的LIST索引
       * type   图片或文件夹类型判断
       * name   图片或文件夹名字 
       * */
      public LoadedImage(Bitmap bitmap,int i,List<PosterInfo> file) {  
          this.mBitmap = bitmap;  
          this.file=file;  
          this.index =i;
      }  

      public Bitmap getBitmap() {  
          return mBitmap;  
      }  
       
      public int getIndex() {  
          return index;  
      }  
      public List<PosterInfo> getmFile(){
      	return file;
      }     
  }
   
  private class ItemClickListener implements AdapterView.OnItemClickListener {
      @Override
	public void onItemClick(AdapterView parent, View v, int position, long id) {
    	  String path = posterList.get(position).getPosterPath();
    	  chiceState(position);
          if (!path.equals("")) { 
	          if(deleteList.contains(path)){  
	        	  deleteList.remove(path);	        
	          }else{
	        	  deleteList.add(path);
	          }
          }else{
        	  Toast.makeText(PosterManagerActivity.this, "操作异常，请重新选择", Toast.LENGTH_LONG).show();
          }
      }
  }
  
  public void chiceState(int post)
  {
		isChice[post]=isChice[post]==true?false:true;
		posterAdapter.notifyDataSetChanged();
  }
   
  @Override
  public void onClick(View v) {
	  posterList = dbHelper.queryPosterList();
	  switch(v.getId()){
	  case R.id.poster_clear_btn:			  
		  if(posterList.size()>0){
			  clearDialog();
		  }else{
			  Toast.makeText(PosterManagerActivity.this, "没有要清除的图片", Toast.LENGTH_LONG).show(); 
		  }
		  break;
//	  case R.id.poster_add_btn:
//		  Intent snedIntent = new Intent();
//		  snedIntent.setClass(PosterManagerActivity.this, PosterAddActivity.class);  
//		  startActivity(snedIntent); 
//		  break;
	  case R.id.poster_delete_btn:
		  if(posterList.size()>0){
			  if(deleteList.size()>0){
				  deleteDialog(deleteList.size());
			  }
			  else{
				 Toast.makeText(PosterManagerActivity.this, "请选择要删除的图片", Toast.LENGTH_LONG).show(); 
			  }
		  }else{
			  Toast.makeText(PosterManagerActivity.this, "没有要删除的图片", Toast.LENGTH_LONG).show(); 
		  }
		  break;
	  }
  }
 
  Runnable r =new Runnable() {             
	  @Override
	public void run() {                 
		  	  Message message = handler.obtainMessage();                       
			  try {                           
				   Thread.sleep(100);                        
			  } 
			  catch (InterruptedException e) {                            
					 Thread.currentThread().interrupt();                       
			  }  			                                           
			  handler.sendMessage(message);             
	  }        
  };
  
  public void clearDialog(){
		new AlertDialog.Builder(PosterManagerActivity.this)  
			.setTitle("操作")  
			.setMessage("是否清空海报列表？")  
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {   
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();    
				}   
			})
			.setPositiveButton("确定",  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				dbHelper.clearPosterList();              
				handler.post(r);
				dialog.dismiss();
			}  
		}).show(); 
	}
  
  public void deleteDialog(final int num){
		new AlertDialog.Builder(PosterManagerActivity.this)  
			.setTitle("操作")  
			.setMessage("是否删除选中的"+num+"张海报？")  
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {   
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();    
				}   
			})
			.setPositiveButton("确定",  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				dbHelper.deletePosterPath(deleteList);
				handler.post(r);
				deleteList.clear();
				dialog.dismiss();
			}  
		}).show(); 
	}

}