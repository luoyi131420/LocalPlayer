package com.kt.localmedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kt.localmedia.database.MusicDatabaseControl;
import com.kt.localmedia.database.PosterDatabaseControl;
import com.kt.localmedia.files.FilesManager;
import com.kt.localmedia.music.MusicBackgroundActivity;
import com.kt.localmedia.music.MusicInfo;
import com.kt.localmedia.music.MusicPlayActivity;
import com.kt.localmedia.music.poster.PosterInfo;
import com.kt.localmedia.pic.TuruPageActivity;
import com.kt.localmedia.util.PlaySound;
import com.kt.localmedia.util.PlayerUtil;
import com.kt.localmedia.video.VideoInfo;
import com.kt.localmedia.video.VideoPlayActivity;

public class FileListActivity extends Activity implements OnItemClickListener,OnItemLongClickListener{
	private static final String TAG = "FileListActivity";
	private GridView fileGridView;
	private ListView fileListView;	
	private List<File> listFiles =new ArrayList<File>();
	private String currentPath;
	private String initPath;
	private String initType;
	private int listPosition = 0; 
	private TextView fileListTitile,pathTitle,noFileTitle;
	private ImageView mediaIcon;
	private boolean isGridview;
	public Spinner fileModeSpinner;
	MusicDatabaseControl dbMusicHelper;
	PosterDatabaseControl dbPosterHelper;
	public ProgressDialog pDialog; 
	PlaySound sp ;
	PlayerUtil playerUtil;
	public File currentDirectory = new File(File.separator);// 当前目录
	FilesManager filesManager; 
	AsyncLoadedImage asyncLI;
	public static int sCurrentDev = 0;
	public static final int STORAGE_DEVICE_SD = 0;
	public static final int STORAGE_DEVICE_USB = 1;
	public static final int STORAGE_DEVICE_USB2 = 2;
	public static int dev;
	//private static final String EXTERNAL_MOUNT_POINT = DataManager.EXTERNAL_MOUNT_POINT;
	BroadcastReceiver fReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			System.out.println("----->action="+action);
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
				
//				StorageVolume storage = (StorageVolume) intent
//						.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
//				String path = storage.getPath().toString();
//				System.out.println("----->path="+path);
//				if(path.contains("/mnt/usb_storage/USB_DISK1/udisk1")){
//					dev = STORAGE_DEVICE_USB2;
//				}else if(path.contains("/mnt/usb_storage/USB_DISK1")){
//					dev = STORAGE_DEVICE_USB;
//				}else if(path.contains("/storage/sdcard1")){
//					dev = STORAGE_DEVICE_SD;
//				}
//				showUnmountError(dev);
			}
		}
	};
	
	private void showUnmountError(int dev) {
		if (sCurrentDev == dev) {
			if(dev == STORAGE_DEVICE_USB2){
				Toast.makeText(this.getApplicationContext(), getString(R.string.usb_removed), Toast.LENGTH_LONG)
				.show();
				this.finish();
			}else if(dev == STORAGE_DEVICE_USB){
				Toast.makeText(this.getApplicationContext(), getString(R.string.usb_removed), Toast.LENGTH_LONG)
				.show();
				this.finish();
			}else if(dev == STORAGE_DEVICE_SD){
				Toast.makeText(this.getApplicationContext(), getString(R.string.sd_removed), Toast.LENGTH_LONG)
				.show();
				this.finish();
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.files_list_layout);
        setProgressBarIndeterminateVisibility(true);  
        initPath= getIntent().getStringExtra("file_path");
        initType= getIntent().getStringExtra("file_type");
        currentPath= initPath;
              
        findViewById();   
        setViewOnclickListener();
        registerReceiver();
     	initViews();
    	
     	dbMusicHelper = new MusicDatabaseControl(FileListActivity.this);
     	dbPosterHelper = new PosterDatabaseControl(FileListActivity.this);
    	sp = new PlaySound(FileListActivity.this);
    	filesManager = new FilesManager(FileListActivity.this,dbMusicHelper,dbPosterHelper);	
    	filesManager.initSpinner();
    	playerUtil = new PlayerUtil();
    	open(new File(initPath)); 
    	 
    	if(initPath.contains("sdcard")){
    		sCurrentDev = STORAGE_DEVICE_SD;
    	}else if(initPath.contains("/storage/udisk2")){
			sCurrentDev = STORAGE_DEVICE_USB2;
		}else if(initPath.contains("/mnt/usb_storage/USB_DISK1/udisk0")){
			sCurrentDev=STORAGE_DEVICE_USB;
		}
    	
        fileModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View view,
        			int position, long id) {
        		switch(position){
        			case 0:
        				isGridview=true;
        				setupViewHandler.sendEmptyMessage(0);
        				break;
        			case 1:
        				isGridview=false;
        				setupViewHandler.sendEmptyMessage(0);
        				break;
        			case 2:       				
        				String[] typeFolder = {getString(R.string.please_input_newfilename),getString(R.string.newfile),"folder"};
        				filesManager.menuCreateNew(typeFolder);
        				filesManager.initSpinner();
        				break;
        			case 3:
        				filesManager.menuDeleteFolder();
        				filesManager.initSpinner();
        				break;
        			case 4:
        				filesManager.menuPasteFile();
        				filesManager.initSpinner();
        				break;
        			case 5:
        				String path = filesManager.openRoot(initPath);
        				setIMGListFiles(path);
        				currentPath =path;
        				filesManager.initSpinner();
        				break;
        			case 6:
        				Intent intent = new Intent();
        				intent.setClass(FileListActivity.this, MusicBackgroundActivity.class);  
        				startActivity(intent); 
        				filesManager.initSpinner();
        				break;
        		} 
        	}
        	@Override
            public void onNothingSelected(AdapterView parent) {
                    // TODO Auto-generated method stub
            }
    	});
	}
	
	private void findViewById(){
		fileGridView = (GridView) findViewById(R.id.file_list_gridview);  
        fileListView = (ListView) findViewById(R.id.file_list_listview); 
        fileModeSpinner = (Spinner) findViewById(R.id.spinner);
        fileListTitile= (TextView)findViewById(R.id.file_path);
        pathTitle = (TextView)findViewById(R.id.path_title);
        noFileTitle = (TextView)findViewById(R.id.no_file_title);     
        mediaIcon = (ImageView) findViewById(R.id.media_icon);
	}
	
	private void setViewOnclickListener(){
		fileGridView.setOnItemClickListener(FileListActivity.this);
    	fileListView.setOnItemClickListener(FileListActivity.this);
    	fileGridView.setOnItemLongClickListener(FileListActivity.this);
    	fileListView.setOnItemLongClickListener(FileListActivity.this);
	}
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter(); 
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED); 
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED); 
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");              
        registerReceiver(fReceiver, filter); 
	}
	
	private void initViews(){
		if(initType.contains("video")){
    		mediaIcon.setBackgroundResource(R.drawable.shipin_icon);
    		pathTitle.setText(R.string.shipin_path);
    	}else if(initType.contains("music")){
    		mediaIcon.setBackgroundResource(R.drawable.yinyue_icon);
    		pathTitle.setText(R.string.yinyue_path);
    	}else if(initType.contains("pic")){
    		mediaIcon.setBackgroundResource(R.drawable.tupian_icon);
    		pathTitle.setText(R.string.tupian_path);
    	}
     	fileListTitile.setText(initPath);
	}
	
    private String mpath;
    private int count = 0 ;
    private void waitforAsyncTaskPic(String path){
    	mpath = path;
    }

    Handler setupViewHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			fileListTitile.setText(currentPath);
			if(0==count){
				noFileTitle.setVisibility(View.VISIBLE);
				if(initType.contains("video")){
					noFileTitle.setText(R.string.no_video);
				}
				if(initType.contains("music")){
					noFileTitle.setText(R.string.no_music);
				}
				if(initType.contains("pic")){
					noFileTitle.setText(R.string.no_pic);
				}        	
	        }else{
	        	noFileTitle.setVisibility(View.GONE);
	        }
			fileAdapter = new FileAdapter(getApplicationContext()); 
			
			if(isGridview){
				fileGridView.setAdapter(fileAdapter); 
				fileListView.setVisibility(View.GONE);
				fileGridView.setVisibility(View.VISIBLE);
				fileAdapter.notifyDataSetChanged();
			}else{
				fileListView.setAdapter(fileAdapter); 
				fileGridView.setVisibility(View.GONE);
				fileListView.setVisibility(View.VISIBLE);
				fileAdapter.notifyDataSetChanged();
			}
			isFastBack =false;
			itemLongFlag =false;
			asyncLI = new AsyncLoadedImage(mpath);			
			asyncLI.execute();
			super.handleMessage(msg);
		}
    	
    };
    
    private FileAdapter fileAdapter;  
	 /* 
     * 初始化文件浏览View 
     */  
    private File[] files;
    private String mpath1;
    List<PosterInfo> posterList = new ArrayList<PosterInfo>(); 
    List<MusicInfo> musicList = new ArrayList<MusicInfo>(); 
    List<VideoInfo> videoList = new ArrayList<VideoInfo>(); 

    private void setupViews(String m) {

    	mpath1= m;
    	
    	new Thread(){
   		
			@Override
			public void run() {
				// TODO Auto-generated method stub
					
				if(!medias.isEmpty()){				
					medias.clear();				
					System.gc();
				}
				
				count = 0;
				if(initType.contains("pic")){					
					File file = new File(mpath1);
			        files = file.listFiles();
			        if(files==null){
						finish();
						return;
					}
			        int pIndex = -1;
			        if(posterList!=null)
				 		{posterList.clear();}
			        
			        for(int i=0;i<files.length;i++){
				    	if(files[i].isFile()){
				    		if(files[i].getName().endsWith(".jpg")||files[i].getName().endsWith(".JPG")
				    			||files[i].getName().endsWith(".bmp")||files[i].getName().endsWith(".BMP")
				    			||files[i].getName().endsWith(".gif")||files[i].getName().endsWith(".GIF")
				    			||files[i].getName().endsWith(".png")||files[i].getName().endsWith(".PNG")
				    			||files[i].getName().endsWith(".jpeg")||files[i].getName().endsWith(".JPEG"))
				    		{	
				    			PosterInfo posterInfo = new PosterInfo(); 
				    			pIndex++;
				    			medias.add(new LoadedImage(getRes("pic_default"),i,files[i],pIndex,0));			    				
				    			posterInfo.setPosterPath(files[i].getPath());
				    			posterInfo.setPosterTitle(files[i].getName());
				    			posterList.add(posterInfo);	
								PlayerUtil.setKTPosterList(posterList);
				    			count++;
					    	}
				    	}				    	
				    	else if(files[i].isDirectory()){
				    	    	medias.add(new LoadedImage(getRes("wjj"),i,files[i],0,0));
				    	    	count = 1;
				    	}
				    }
				}
				
				if(initType.contains("music")){
					int mIndex = -1;					
					if(musicList!=null)
					 	{musicList.clear();}
					
					File file = new File(mpath1);
			        files = file.listFiles();
			        if(files==null){
						finish();
						return;
					}
			        if(mMusics!=null){
			        	mMusics.clear();
			        }
			        for(int i=0;i<files.length;i++){
				    	if(files[i].isFile()){
				    		if(files[i].getName().endsWith(".mp3")||files[i].getName().endsWith(".MP3")
				    			||files[i].getName().endsWith(".wav")||files[i].getName().endsWith(".WAV")
							    ||files[i].getName().endsWith(".wma")||files[i].getName().endsWith(".WMA")
							    ||files[i].getName().endsWith(".ogg")||files[i].getName().endsWith(".OGG")
							    ||files[i].getName().endsWith(".aac")||files[i].getName().endsWith(".AAC"))
				    		{	
								mIndex++;
				    			medias.add(new LoadedImage(getRes("music_default_h"),i,files[i],mIndex,0));	
								mMusics.add(files[i].getPath());
								count++;
					    	}
				    	}
				    	
				    	else if(files[i].isDirectory()){
				    		medias.add(new LoadedImage(getRes("wjj"),i,files[i],0,0));
				    		count = 1;
				    	}
				    }
			        
				}
				if(initType.contains("video")){
					int vIndex = -1;					
					if(videoList!=null)
						{videoList.clear();}
					
			        File file = new File(mpath1);
			        files = file.listFiles();
			        
			        if(files==null){
						finish();
						return;
					}
			        if(mVideos!=null){
			        	mVideos.clear();
			        }
			        for(int i=0;i<files.length;i++){		        	
				    	if(files[i].isFile()){
				    		if(files[i].getName().endsWith(".rmvb")||files[i].getName().endsWith(".RMVB")
				    				||files[i].getName().endsWith(".3gp")||files[i].getName().endsWith(".3GP")
					    			||files[i].getName().endsWith(".mpg")||files[i].getName().endsWith(".MPG")
					    			||files[i].getName().endsWith(".mpeg")||files[i].getName().endsWith(".MPEG")
					    			||files[i].getName().endsWith(".flv")||files[i].getName().endsWith(".FLV")
					    			||files[i].getName().endsWith(".wmv")||files[i].getName().endsWith(".WMV")
					    			||files[i].getName().endsWith(".mp4")||files[i].getName().endsWith(".MP4")
					    			||files[i].getName().endsWith(".rm")||files[i].getName().endsWith(".RM")
					    			||files[i].getName().endsWith(".mov")||files[i].getName().endsWith(".MOV")
					    			||files[i].getName().endsWith(".avi")||files[i].getName().endsWith(".AVI")
					    			||files[i].getName().endsWith(".f4v")||files[i].getName().endsWith(".F4V")
					    			||files[i].getName().endsWith(".mkv")||files[i].getName().endsWith(".MKV")
					    			||files[i].getName().endsWith(".vob")||files[i].getName().endsWith(".VOB")
					    			||files[i].getName().endsWith(".ts")||files[i].getName().endsWith(".TS")
					    			||files[i].getName().endsWith(".trp")||files[i].getName().endsWith(".TRP")
					    			||files[i].getName().endsWith(".asf")||files[i].getName().endsWith(".ASF")
					    			||files[i].getName().endsWith(".ra")||files[i].getName().endsWith(".RA")
					    			||files[i].getName().endsWith(".ram")||files[i].getName().endsWith(".RAM")
					    			||files[i].getName().endsWith(".mpe")||files[i].getName().endsWith(".MPE")
					    			||files[i].getName().endsWith(".m2v")||files[i].getName().endsWith(".M2V")
					    			||files[i].getName().endsWith(".mlv")||files[i].getName().endsWith(".MLV")
					    			||files[i].getName().endsWith(".dat")||files[i].getName().endsWith(".DAT")
					    			||files[i].getName().endsWith(".tp")||files[i].getName().endsWith(".TP")
					    			||files[i].getName().endsWith(".qt")||files[i].getName().endsWith(".QT")
					    			){
				    			vIndex++;
				    			medias.add(new LoadedImage(getRes("pic_default"),i,files[i],vIndex,0));
								mVideos.add(files[i].getPath());
								count++;
					    	}
				    		
				    		if(files[i].getName().endsWith(".SRT")||files[i].getName().endsWith(".srt")
				    				||files[i].getName().endsWith(".SUB")||files[i].getName().endsWith(".sub")){
				    			medias.add(new LoadedImage(getRes("wj"),i,files[i],vIndex,1));
				    			count++;
				    		}
				    	}				    	
				    	else if(files[i].isDirectory()){			    		
				    		medias.add(new LoadedImage(getRes("wjj"),i,files[i],0,0));
				    		count = 1;
				    	}
				    }
				}
				lastFilePath="";	
				lastFilePath1="";
		    	setupViewHandler.sendEmptyMessage(0);
				super.run();
			}
    		
    	}.start(); 
    	waitforAsyncTaskPic(mpath);//异步加载缩略图片
    }  

    public Bitmap getRes(String name) {
    	ApplicationInfo appInfo = getApplicationInfo();
    	int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
    	return BitmapFactory.decodeResource(getResources(), resID);
    	}
	Bitmap bitmap;
	Bitmap smallBitmap;
	
	 /* 
     * 异步加载缩略图到LoadedImage然后调用addImage方法更新Adapter 
     */  
    class AsyncLoadedImage extends AsyncTask<Object, LoadedImage, Object> {  
    	private String initpath;
    	
    	public AsyncLoadedImage(String path){   		
    		this.initpath =path;
    	}
    	
    	
        @Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
        	
			super.onCancelled();
		}


		@Override  
        protected Object doInBackground(Object... params) {  
        	  
                Bitmap bitmap;  
                Bitmap newBitmap;  
                int mIndex = -1;
                int vIndex = -1;
                int pIndex = -1;
                
                for (int i = 0; i < medias.size(); i++) {  
 
                	File file =new File(medias.get(i).getmFile().getPath());
                    if(file.isFile()){
                    	 try {  
                            	if(initType.contains("video")){                            		
                            		if(this.isCancelled()){
                                 		break;
                                 	}                           		
                            		bitmap = ThumbnailUtils.createVideoThumbnail(medias.get(i).getmFile().getPath(), Thumbnails.MINI_KIND); 
                               		if(bitmap!=null){
                               			newBitmap = ThumbnailUtils.extractThumbnail(bitmap, 115, 80, ThumbnailUtils.OPTIONS_RECYCLE_INPUT); 
                               			bitmap.recycle();
                               		 if (newBitmap != null) { 
                        		    	vIndex++;
	                            		publishProgress(new LoadedImage(newBitmap,i,file,vIndex,0)); 
	                            		Thread.sleep(100);
		                         		}
                               		}
                            	}
                            	if(initType.contains("pic")){
                            		if(this.isCancelled()){
                                 		break;
                                 	}                           		
                            		BitmapFactory.Options options = new BitmapFactory.Options();  
                                    options.inSampleSize = 15;   
                                    bitmap = BitmapFactory.decodeFile(file.getPath(), options); 
                                    if(bitmap!=null){
	                                    newBitmap = ThumbnailUtils.extractThumbnail(bitmap, 115, 80);  
	                                    bitmap.recycle();                                    
	                                    if (newBitmap != null) { 
	                                    	pIndex++;
	                                    	publishProgress(new LoadedImage(newBitmap,i,file,pIndex,0));
	                                    	Thread.sleep(100);
	                                    }
                                    }
                            	}   
                            	
                            	if(initType.contains("music")){  
                            		if(this.isCancelled()){
                                 		break;
                                 	}                           		
                                    bitmap = getRes("music_default_h");   
                                    if(bitmap!=null){
	                                    newBitmap = ThumbnailUtils.extractThumbnail(bitmap,115,80);  
	                                    bitmap.recycle();
	                                    if (newBitmap != null) { 
	                                    	mIndex++;                  	
	                                    	publishProgress(new LoadedImage(newBitmap,i,file,mIndex,0));
	                                    	Thread.sleep(100);
	                                    }
                                    }
                            	} 
                         } catch (Exception e) {  
                             e.printStackTrace();  
                         }  
                    }
                   
                }  
            return null;  
        }  
  
        //实时更新UI  onProgressUpdate()方法的参数对应于doInBackground中publishProgress方法的参数，同时也对应于  
        //doInBackground的第二个参数  
        @Override  
        public void onProgressUpdate(LoadedImage... value) {  
        	if(this.isCancelled()){
        		return;
        	}
        		addImage(value);
            
        }  
  
        //更新UI结束后的处理  
        @Override  
        protected void onPostExecute(Object result) {  
            setProgressBarIndeterminateVisibility(false);  
        }  
    }  
    
    /* 
     * 刷新Adapter 
     */  
    private void addImage(LoadedImage... value) {  
        for (LoadedImage image : value) {  
            fileAdapter.addPhoto(image);  
            fileAdapter.notifyDataSetChanged();  
        }  
    }  
    /* 
     * Adapter 
     */  
    private ArrayList<LoadedImage> medias = new ArrayList<LoadedImage>();
    class FileAdapter extends BaseAdapter {  
  
        public Context mContext;  
      
  
        public FileAdapter(Context context) {  
            this.mContext = context;  
        }  
  
        public void addPhoto(LoadedImage media) {  
            int i = media.getIndex();
           
            if(medias.size()>0&& i<medias.size()){
            	  medias.set(i,media);
            }
          
        }  
  
        @Override
		public int getCount() {  
            return medias.size();  
        }  
  
        @Override
		public Object getItem(int position) {  
            return medias.get(position);  
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
				if(isGridview){
					convertView = LayoutInflater.from(FileListActivity.this).inflate(R.layout.device_list_item, null);
					viewHolder.dvIcon = (ImageView)convertView.findViewById(R.id.device_list_item_iv);
			        viewHolder.dvName = (TextView)convertView.findViewById(R.id.device_list_item_tv);
			        convertView.setTag(viewHolder);
				}else{
					convertView = LayoutInflater.from(FileListActivity.this).inflate(R.layout.music_file_list_item, null);
					viewHolder.dvIcon = (ImageView)convertView.findViewById(R.id.file_list_item_icon);
			        viewHolder.dvName = (TextView)convertView.findViewById(R.id.file_list_item_name);
			        convertView.setTag(viewHolder);
				}		
			}
			else{
				viewHolder=(ViewHolder)convertView.getTag();
			}
			
			if(isGridview){
				viewHolder.dvIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);  
				viewHolder.dvIcon.setPadding(0, 1, 1, 1); 
			}		 
			if(medias.size()>0){
				viewHolder.dvIcon.setImageBitmap(medias.get(position).getBitmap());  
				viewHolder.dvName.setText(medias.get(position).getmFile().getName());
			}
			
            return convertView;  
        }  
        
        class ViewHolder{
  		  public ImageView dvIcon;
  		  public TextView dvName;
  	  }
    }  
    
    /* 
     * 这是个保存bitmap的类，加入Adapter的ArrayList中，随着addImage更新Adapter 
     */  
    private  class LoadedImage {  
       
        Bitmap mBitmap;  
        int index;
        File file;
        int position;
        int flag=0;
        /*
         * @param 
         * bitmap 缩略图片
         * path   图片路径
         * index  图片的LIST索引
         * type   图片或文件夹类型判断
         * name   图片或文件夹名字 
         * */
        public LoadedImage(Bitmap bitmap,int i,File f,int p,int g)  {  
            this.mBitmap = bitmap;  
            this.index =i;
            this.file=f;
            this.position = p;
            this.flag = g;
        }  
  
        public Bitmap getBitmap() {  
            return mBitmap;  
        }  

        public int getIndex() {  
            return index;  
        }  
        public File getmFile(){
        	return file;
        }
        
        public int getPosition() { 
            return position;  
        } 
        public int getFlag() { 
            return flag;  
        } 
    }  
    /*短按OK键打开目录或文件*/
    private boolean itemFlag = false;
    private boolean itemLongFlag = false;
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		isFastBack = false;
		if(itemFlag){
			itemFlag = false;
			return;
		}
		itemLongFlag = true;
		LoadedImage loadedImage = medias.get(position);
		 if(loadedImage.getmFile().isFile()){
			 itemLongFlag =false;
			 openFile(position);
		 }
		 else if(loadedImage.getmFile().isDirectory()){
			
			 currentPath =  loadedImage.getmFile().getAbsolutePath();	
			 File tempcurrentFile = new File(currentPath+File.separator);
			 this.currentDirectory = tempcurrentFile;
			 //System.out.println("-->lastFilePath="+lastFilePath);
			 if(currentPath.equals(lastFilePath)){
					return;
			 }
			 lastFilePath = currentPath;
			 setIMGListFiles(currentPath);			 
		 }
	}

	private List<PosterInfo> posterInfos;
	public static List<String> addList = new ArrayList<String>();
	public static List<String> deleteMusicList = new ArrayList<String>();	
	public static List<String> deletePosterList = new ArrayList<String>();	
	private String mflag = "";
	/*长按OK键进行操作*/
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		isFastBack = false;
		itemFlag = true;	
		if(itemLongFlag){
			return false;
		}
		LoadedImage loadedImage = medias.get(position);		
		File tempcurrentFile = new File(this.currentDirectory.getAbsolutePath()+
				File.separator+loadedImage.getmFile().getName());
		if(loadedImage.getmFile().isFile()){
			if(initType.contains("music")){
				mflag = "music";
				filesManager.operateMenu(tempcurrentFile,position,mflag);			
			}else 
			if(initType.contains("pic")){
				mflag = "pic";
				filesManager.operateMenu(tempcurrentFile,position,mflag);
			}else{
				mflag = "";
				filesManager.operateMenu(tempcurrentFile,position,mflag);
			}
		}else{
			mflag = "";
			filesManager.operateMenu(tempcurrentFile,position,mflag);
		}
		return false;
	}

	boolean isFastBack = false;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(KeyEvent.KEYCODE_BACK==keyCode){
			//Log.i(TAG,"---->isFastBack="+isFastBack);
			if(isFastBack){
				return true;
			}
			isFastBack = true;
			System.out.println("--------------this is currentPath---------"+currentPath);										
			File file = new File(currentPath);			
			String prePath = file.getParentFile().getAbsolutePath();
			if(file.getAbsolutePath().equals(initPath)){
				isFastBack = false;
		    	this.finish();
			}
			else{
				File currentFile = new File(prePath);
				this.currentDirectory = currentFile;				
				setIMGListFiles(prePath);
				currentPath = prePath;	
				sp.playSounds(1);
				return true;
			}	
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		lastFilePath="";
		lastFilePath1="";
		if(!medias.isEmpty()){
			for(int i=0;i<medias.size();i++){
				medias.get(i).getBitmap().recycle();
			}
		}
		if(fReceiver!=null)
			{this.unregisterReceiver(fReceiver);}
		super.onDestroy();
	}
	private String filePath;
	private String lastFilePath="";
	private String lastFilePath1="";
	private void setIMGListFiles(String path){		
		filePath = path;
		
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
			while(true){
				
				try {
					
					Thread.sleep(100);
					
					if(asyncLI!=null&&asyncLI.getStatus()==AsyncTask.Status.RUNNING){
						asyncLI.cancel(true);
						
					}
					else if(asyncLI!=null&&asyncLI.getStatus()==AsyncTask.Status.FINISHED){						
						setupViews(filePath);	  						
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
	
	/* * 添加背景音乐*/
	public void addBackMusic(int position){
		int pos = medias.get(position).getPosition(); 
		String path = mMusics.get(pos);			
		boolean flag = dbMusicHelper.queryMusicPath(path);
		if(!flag){
			if(addList!=null){
				addList.clear();
			}
			addList.add(path);
			boolean addIsOk = dbMusicHelper.addMusicPath(addList);
			if(addIsOk){
				Toast.makeText(FileListActivity.this, getString(R.string.add_success), 1).show();
			}else{
				Toast.makeText(FileListActivity.this, getString(R.string.add_failed), 1).show();
			}
		}else{
			if(deleteMusicList!=null){
				deleteMusicList.clear();
			}
			deleteMusicList.add(path);
			filesManager.deleteDialog("music");
		}		
	}
	
	
	/* * 添加音乐海报*/
	public void addMusicPoster(int position){
		int pos = medias.get(position).getPosition(); 
		posterInfos = PlayerUtil.getKTPosterList();
		String path = posterInfos.get(pos).getPosterPath(); 

		boolean flag = dbPosterHelper.queryPosterPath(path);
		if(!flag){
			if(dbPosterHelper.queryPosterList().size()>=10){
				filesManager.Dialog(R.string.poster_max);
			}else{
				boolean addIsOk = dbPosterHelper.addPosterPath(path);
				if(addIsOk){
					Toast.makeText(FileListActivity.this, getString(R.string.add_success), 1).show();
				}else{
					Toast.makeText(FileListActivity.this, getString(R.string.add_failed), 1).show();
				}
			}
		}else {
			if(deletePosterList!=null){
				deletePosterList.clear();
			}
			deletePosterList.add(path);
			filesManager.deleteDialog("poster");
		}
	}
	
	/** 打开目录，并判断是否为文件 */
	public void open(File file) {
		if (file.isDirectory()) {
			isFastBack = false;
			this.currentDirectory = file;
			String path = this.currentDirectory.getAbsolutePath()+File.separator;
			currentPath =path;
			if(currentPath.equals(lastFilePath1)){
				return;
			}
			lastFilePath1 = currentPath;
			setIMGListFiles(path);			
		}else{
			//openFile(file);
		}
	}
	private ArrayList<String> mMusics = new ArrayList<String>();
	public  String MUSIC_LIST = "android.intent.wmt_extra.music_list";
	public static final String MUSIC_CURRENT = "android.intent.wmt_extra.music_current";
	private ArrayList<String> mVideos = new ArrayList<String>();
	public  String VIDEO_LIST = "android.intent.rk.video_list";
	public static final String VIDEO_CURRENT = "android.intent.rk.video_current";
	/*打开视频、音乐、图片文件*/
	public void openFile(int position) {
		
		LoadedImage loadedImage = medias.get(position);
		if(initType.contains("pic")){
			currentPath =loadedImage.getmFile().getParentFile().getAbsolutePath();
			Intent intent =new Intent(FileListActivity.this,TuruPageActivity.class);
			intent.putExtra("pic_index", position);
			intent.putExtra("pic_path", currentPath);
			startActivity(intent); 
        }
		if(initType.contains("video")){
			if(0==loadedImage.getFlag()){						 	
				listPosition = medias.get(position).getPosition(); 
//				String path=mVideos.get(listPosition);
//				int n = path.length();
//				int m = path.lastIndexOf("/");
//				String videoName=path.substring(m+1, n);			
//				System.out.println("=====videoName="+videoName);
//				if(videoName.endsWith(".rm")||(videoName.endsWith(".RM"))
//					||videoName.endsWith(".rmvb")||videoName.endsWith(".RMVB")){
//					filesManager.Dialog(R.string.no_this_video_tpye);
//					return;
//				}
				Intent mIntent = new Intent();  
				mIntent.setAction("com.kt.media.MUSIC_SERVICE");  
				stopService(mIntent);
				
				Intent intent = new Intent();
				intent.setClass(FileListActivity.this, VideoPlayActivity.class);  
				Bundle bundle = new Bundle();
				bundle.putStringArrayList(VIDEO_LIST, mVideos);
				bundle.putInt(VIDEO_CURRENT, listPosition);
				intent.putExtras(bundle);				
				startActivity(intent);
			}else if(1==loadedImage.getFlag()){
				filesManager.Dialog(R.string.open_invalid);
				return;
			}
		}
        if(initType.contains("music")){			
        	listPosition = medias.get(position).getPosition();  
			Intent intent = new Intent();
			intent.setClass(FileListActivity.this, MusicPlayActivity.class); 
			Bundle bundle = new Bundle();
			bundle.putStringArrayList(MUSIC_LIST, mMusics);
			bundle.putInt(MUSIC_CURRENT, listPosition);
			intent.putExtras(bundle);				
			startActivity(intent);			
//			intent.putExtra("listPosition", listPosition); 
//			intent.setClass(FileListActivity.this, MusicPlayActivity.class);  
//			startActivity(intent); 
		}
	}
	
	/*得到当前目录路径*/
	public String getCurrentDirectory(){
		//System.out.println("--->this.currentDirectory.getAbsolutePath()="+this.currentDirectory.getAbsolutePath());
		return this.currentDirectory.getAbsolutePath();
	}
	
	/*得到设备路径*/ 
	public String getCurrentDevicePath(){
		return initPath;
	}
	
	public void sendProgressMessage(int w,int p){
		
		Message m = new Message();  
		m.arg1 = w;  
		m.arg2 = p;  
		handler.sendMessage(m); 
	}
	//复制过程
	Handler handler = new Handler() {  
		  @Override  
		  public void handleMessage(Message msg) {  
			  switch(msg.arg1){
			  	case 1:
			  		pDialog.setProgress(msg.arg2);
			  		break;
			  	case 2:
			  		pDialog.dismiss(); 
			  		Toast.makeText(FileListActivity.this, getString(R.string.copy_over), 1).show();
			  		break;
			  } 
			  super.handleMessage(msg);
		  }  
	};
}
