package com.kt.localmedia.music.poster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.localmedia.R;
import com.kt.localmedia.database.PosterDatabaseControl;
import com.kt.localmedia.music.MusicInfo;
import com.kt.localmedia.util.CustomProgressDialog;
import com.kt.localmedia.util.PlayerUtil;

public class PosterAddActivity extends Activity {
  public static List<PosterInfo> imageList = null;
  public static List<String> selectImageList = new ArrayList<String>();
  public static List<String> selectTitleList = new ArrayList<String>();
  private static final String TAG = "PosterAddActivity";
  PosterDatabaseControl dbHelper;
  private ImageView selectImage;
  private PosterAdapter posterAdapter;  
  private GridView posterGridView;
  private boolean isChice[];
  private TextView mTitle;
  private int count=0;
  PlayerUtil image;
  
  public static void clearImageList() {
    if (imageList != null) {
    	imageList.clear();
    	imageList = null;
    }
  }

  private Intent mIntent;
  private List<PosterInfo> posterList = new ArrayList<PosterInfo>();	
  private List<PosterInfo> listPoster = new ArrayList<PosterInfo>();	
  private CustomProgressDialog dialog;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //requestWindowFeature(Window.FEATURE_NO_TITLE);
    mIntent = this.getIntent();  
    setContentView(R.layout.music_add_poster_gridview);
    mTitle = (TextView) findViewById(R.id.title);
    image =new PlayerUtil();
    
    dialog =  CustomProgressDialog.createDialog(PosterAddActivity.this);
	dialog.setTitile("加载中.......");
	dialog.setMessage("加载中......");
    dialog.show();
       
    selectImage =(ImageView) findViewById(R.id.selected_id);
    posterGridView = (GridView) findViewById(R.id.file_list_view); 
	posterGridView.setOnItemClickListener(new ItemClickListener()); 
    setupViews();  
  }
 
  private void waitforAsyncTaskPic(){}
  
  Handler setupViewHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			dbHelper = new PosterDatabaseControl(PosterAddActivity.this);
		    listPoster = dbHelper.queryPosterList();
		    count = 10-listPoster.size();
		    if(0 == count){
		    	mTitle.setText(R.string.poster_ten);
		    }
		    else{
		    	mTitle.setText("添加("+listPoster.size()+"/10)张海报");
		    }
			posterAdapter = new PosterAdapter(getApplicationContext(),imageList);  
			posterGridView.setAdapter(posterAdapter); 
		  	new AsyncLoadedImage().executeOnExecutor(Executors.newCachedThreadPool());
			super.handleMessage(msg);
		}
  };

  private void setupViews() {
	  clearImageList();
	  imageList = image.getImagesList(this);
	  new Thread(){
		  @Override
			public void run() {
			  if(!photos.isEmpty()){
				  photos.clear();
					System.gc();
			  }

			  for(int i=0;i<imageList.size();i++){
				  photos.add(new LoadedImage(getRes("pic_default"),i,imageList));
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

              for (int i = 0; i < imageList.size(); i++) {             	  
                  	 try {  
                           BitmapFactory.Options options = new BitmapFactory.Options();  
                           options.inSampleSize = 15;  
                           bitmap = BitmapFactory.decodeFile(imageList.get(i).getPosterPath(), options);  
                           newBitmap = ThumbnailUtils.extractThumbnail(bitmap,  
                                   122, 109);  
                           bitmap.recycle();  

                           if (newBitmap != null) {  
                        	  publishProgress(new LoadedImage(newBitmap,i,imageList));  
                              Thread.sleep(100);
                           }  
                           
                       } catch (Exception e) {  
                           e.printStackTrace();  
                       }                  
              }  
//          }  
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
      	  photos.set(i,photo);
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
				convertView = LayoutInflater.from(PosterAddActivity.this).inflate(R.layout.music_image_list_item, null);
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
			viewHolder.dvIcon.setImageBitmap(photos.get(position).getBitmap());  
			viewHolder.dvName.setText(photos.get(position).getmFile().get(position).getPosterTitle());
			
			if (isChice[position]== true){  
				Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
					 R.drawable.selected);
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
  
  @Override
  public void onDestroy() { 
	  if(!photos.isEmpty()){
			for(int i=0;i<photos.size();i++){
				photos.get(i).getBitmap().recycle();
			}
	  }
	  super.onDestroy(); 
  }
  
  private List<MusicInfo> posterDBList = new ArrayList<MusicInfo>();	
  private class ItemClickListener implements AdapterView.OnItemClickListener {
      @Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
          String path = PosterAddActivity.imageList.get(position).getPosterPath();
          String title = PosterAddActivity.imageList.get(position).getPosterTitle();
          dbHelper = new PosterDatabaseControl(PosterAddActivity.this);
    	  boolean flag = dbHelper.queryPosterPath(path); 
          if (!path.equals("")) { 
            if(!flag){
            	if(selectImageList.contains(path)){
            		count++;
            		if(count<10)
        	        {
            			chiceState(position);
        	        	selectImageList.remove(path);	 
            	        selectTitleList.remove(title);
        	        	mTitle.setText("添加("+(10-count)+"/10)张海报");
        	        }
        	        else{       	        		  
        	        	mTitle.setText(R.string.poster_max);
        	        }         			       	        	  
        	   }else{    
        	        count--;
        	        if(count<0){
        	        	count++;
        	        	mTitle.setText(R.string.poster_max);
        	        }
        	        else{  
        	        	chiceState(position);
        	        	selectImageList.add(path);
            	        selectTitleList.add(title);
            	        mTitle.setText("添加("+(10-count)+"/10)张海报");
        	     }       	        	  
        	  }
            }	
            else{
            	mTitle.setText(R.string.poster_added);
            }
          }else{
            Toast.makeText(PosterAddActivity.this, "操作异常，请重新选择", Toast.LENGTH_LONG).show();
          }
      }
  }
   
  public void chiceState(int post)
	{
		isChice[post]=isChice[post]==true?false:true;
		posterAdapter.notifyDataSetChanged();
	}
   
  private void selectListToPosterList() {	  
	  for(int i=0;i<selectImageList.size();i++){
		  PosterInfo posterInfo = new PosterInfo();
          posterInfo.setPosterPath(selectImageList.get(i));
          posterInfo.setPosterTitle(selectTitleList.get(i));
          //posterInfo.setMusicPosterId(i);
          posterList.add(posterInfo);
	  }
  }
  
  @Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if (keyCode == KeyEvent.KEYCODE_BACK) {  
			dbHelper = new PosterDatabaseControl(PosterAddActivity.this);  
		  	selectListToPosterList();
		  	//dbHelper.addPosterList(posterList);
		  	selectImageList.clear();
		  	selectTitleList.clear();
		  	PosterAddActivity.this.setResult(RESULT_OK, mIntent);
		  	PosterAddActivity.this.finish();
		}  
		return super.onKeyDown(keyCode, event);  
	} 
}
