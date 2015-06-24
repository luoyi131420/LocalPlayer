package com.kt.localmedia.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.kt.localmedia.FileListActivity;
import com.kt.localmedia.R;
import com.kt.localmedia.database.MusicDatabaseControl;
import com.kt.localmedia.database.PosterDatabaseControl;
import com.kt.localmedia.util.BuilderUtil;
import com.kt.localmedia.util.CustomProgressDialog;


public class FilesManager {
	private static final String TAG = "FilesManager";
	private List<String> listpath = new ArrayList<String>();
	FileListActivity fileList;
	MusicDatabaseControl dbMusicHelper;
	PosterDatabaseControl dbPosterHelper;
	// 保存临时操作的文件，用于复制、剪切
	private File tempFile = null;
	// 当前操作代号
	private int currentOpt = 0;
	private CustomProgressDialog dialog;
	
	public FilesManager(FileListActivity f,MusicDatabaseControl m,PosterDatabaseControl p){
		this.fileList = f;
		this.dbMusicHelper = m;
		this.dbPosterHelper = p;
		dialog =  CustomProgressDialog.createDialog(fileList);
		dialog.setTitile(fileList.getString(R.string.removing));
		dialog.setMessage(fileList.getString(R.string.removing));
	}

	public FilesManager(){
	}

	/**
	 * 获取扩展存储路径，TF卡、U盘
	 */
	public   List<String> getExternalStorageDirectory(){
		
		if(listpath!=null){
			listpath.clear();
		}
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
	                		File file = new File(columns[i]);
	                		if(file.isFile()||file.isDirectory()){
	                			if(columns[i].contains("udisk")||columns[i].contains("sdcard")){
	                				listpath.add(columns[i]);
	                			}	                			 
	                			 Log.i(TAG,"----------------this is dir fat------------"+columns[i]+"\n");
	                		}
	                	      
	                	      
	                	}
	                    dir = dir.concat(columns[1] + "\n");
	             
	                }
	            } 
	            else if (line.contains("fuse")) {
	                String columns[] = line.split(" ");
	                if (columns != null && columns.length > 1) {
	                    dir = dir.concat(columns[1] + "\n");
	                    for(int i=0;i<columns.length;i++){
	                    	File file = new File(columns[i]);
	                    	if(file.isFile()||file.isDirectory()){
	                    		if(columns[i].contains("udisk")||columns[i].contains("sdcard")){
	                				listpath.add(columns[i]);
	                			}
	                    		Log.i(TAG,"----------------this is dir fuse------------"+columns[i]+"\n");
	                		}
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
	  return listpath;
	}
	
	public void initSpinner(){
		String gridMode = fileList.getString(R.string.display_horizontally);
		String listMode = fileList.getString(R.string.list_show);	
		String creatFolder = fileList.getString(R.string.create_directory);
		String deleteFolder = fileList.getString(R.string.delete_directory);
		String paste = fileList.getString(R.string.paste);
		String root = fileList.getString(R.string.root_directory);
		String backgroundMusic = fileList.getString(R.string.background_music);

		String[] applicationNames = new String[] {gridMode,listMode,creatFolder,deleteFolder,paste,root,backgroundMusic};  
		ArrayAdapter<String> aaAdapter = new ArrayAdapter<String>(fileList, android.R.layout.simple_spinner_item, applicationNames);  
		aaAdapter.setDropDownViewResource(R.layout.drop_down_item);
		fileList.fileModeSpinner.setAdapter(aaAdapter);
	}
	
	public void operateMenu(final File file,final int position,final String mflag){

		OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
					//打开
					case 0:
						if(file.isDirectory()){
							fileList.open(file);		
						}else{
							fileList.openFile(position);
						}
						break;
					//重命名
					case 1:
						fileRenameItem(file);
						break;
					// 删除
					case 2:
						fileDeleteItem(file);
						break;
					//复制
					case 3:
						tempFile = file;
						currentOpt = 3;
						Toast.makeText(fileList, tempFile.getName()+fileList.getString(R.string.copied_paste_operations), Toast.LENGTH_LONG).show();
						break;
					//剪切
					case 4:
						tempFile = file;
						currentOpt = 4;
						Toast.makeText(fileList, tempFile.getName()+fileList.getString(R.string.cut_paste_operations), Toast.LENGTH_LONG).show();
						break;
					//添加背景音乐、添加音乐海报
					case 5:
						if(mflag.contains("music")){
							fileList.addBackMusic(position);
						}
						if(mflag.contains("pic")){
							fileList.addMusicPoster(position);
						}
						break;
				
				}
			}
		};
		//显示操作菜单
		if(mflag.contains("music")){
			String[] menu = {fileList.getString(R.string.open),
					fileList.getString(R.string.rename),
					fileList.getString(R.string.delete),
					fileList.getString(R.string.copy),
					fileList.getString(R.string.cut),
					fileList.getString(R.string.add_bg_music)};
			new AlertDialog.Builder(fileList).setTitle(fileList.getString(R.string.What_do_you_want_to_do))
			.setItems(menu, onClickListener).show();
		}else if(mflag.contains("pic")){
			String[] menu = {fileList.getString(R.string.open),
					fileList.getString(R.string.rename),
					fileList.getString(R.string.delete),
					fileList.getString(R.string.copy),
					fileList.getString(R.string.cut),
					fileList.getString(R.string.add_bg_poster)};
			new AlertDialog.Builder(fileList).setTitle(fileList.getString(R.string.What_do_you_want_to_do))
			.setItems(menu, onClickListener).show();
		}
		else{
			String[] menu = {fileList.getString(R.string.open),
					fileList.getString(R.string.rename),
					fileList.getString(R.string.delete),
					fileList.getString(R.string.copy),
					fileList.getString(R.string.cut)};
			new AlertDialog.Builder(fileList).setTitle(R.string.choic_settings)
			.setItems(menu, onClickListener).show();
		}
		
			
	}
	
	/** 打开根目录 */
	public String openRoot(String initPath) {
		File f = new File(initPath);
		fileList.currentDirectory = f;
		String path = fileList.currentDirectory.getAbsolutePath()+File.separator;
		return path;
	}
	
	/** 删除文件 */
	public boolean deleteFile(File file){
		boolean result = false;
		if(file != null){
			try {
				File file2 = file;
				file2.delete();
				result = true;
			} catch (Exception e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}
	
	/** 删除文件夹及其子文件 */
	public boolean deleteFolder(File folder){
		boolean result = false;
		try {
			String[] children = folder.list();
			if(children == null || children.length <= 0){
				if(folder.delete()){
					result = true;
				}
			}else{
				for(int i=0;i<children.length;i++){
					String childName = children[i];
					String childPath = folder.getPath() + File.separator + childName;
					File filePath = new File(childPath);
					if(filePath.exists() && filePath.isFile()){
						if(filePath.delete()){
							result = true;
						}else{
							result = false;
							break;
						}
					}else if(filePath.exists() && filePath.isDirectory()){
						if(deleteFolder(filePath)){
							result = true;
						}else{
							result = false;
							break;
						}
					}
				}
				folder.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	private int progress = 0;
	private int len = 0;
	
	public void copy(File src,File target){

		BufferedInputStream bufferedInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		try {			
			if (src.isDirectory()) {
				if (!target.exists()) {
					target.mkdirs();
				}

				File[] files = src.listFiles();
				for (File f : files) {

					if (f.isFile()) {
						bufferedInputStream = new BufferedInputStream(
								new FileInputStream(f));
						bufferedOutputStream = new BufferedOutputStream(
								new FileOutputStream(new File(target.getAbsolutePath()
										+ File.separator+ f.getName())));
						long total_length  = bufferedInputStream.available();
						byte[] buffer = FormetFileSize(total_length);
						int current_len = 0;
						int len = 0;	
						int progress = 0;//当前下载进度
						while ((len = bufferedInputStream.read(buffer)) != -1) {
							current_len += len;
							bufferedOutputStream.write(buffer, 0, len);
							progress = (int) ((current_len/(float)total_length)*100);
							fileList.sendProgressMessage(1,progress);
						}
						bufferedOutputStream.flush();
					} else {
						
						copy(new File(src.getAbsolutePath() + File.separator+ f.getName()),
								new File(target.getAbsolutePath()+ File.separator + f.getName()));
					}
				}
			}else if(src.isFile()){
				bufferedInputStream = new BufferedInputStream(new FileInputStream(src));
				bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(
						fileList.getCurrentDirectory()+File.separator+src.getName()));
				long total_length  = bufferedInputStream.available();
				byte[] buffer = FormetFileSize(total_length);
				int current_len = 0;
				int len = 0;	
				int progress = 0;//当前下载进度
				while ((len = bufferedInputStream.read(buffer)) != -1) {
					current_len += len;
					bufferedOutputStream.write(buffer, 0, len);
					progress = (int) ((current_len/(float)total_length)*100);
					fileList.sendProgressMessage(1,progress);
				}
				bufferedOutputStream.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(bufferedOutputStream != null){
					bufferedOutputStream.close();
				}
				if(bufferedInputStream != null){
					bufferedInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	/** 复制文件 */
	public void copyFile(final File src,final File target){
		new Thread(){
			@Override
			public void run() {

				copy(src,target);	
				
				fileList.open(new File(fileList.getCurrentDirectory()));
				fileList.sendProgressMessage(2,0);	
							
			   super.run();
			}				
		}.start();
	}
	
	/** 移动文件 */
	public void moveFile(String from,String to){
		new File(from).renameTo(new File(to));
	}
	
	/** 粘贴文件 */
	public void menuPasteFile(){
		//如果没有源文件
		if(tempFile == null){
			BuilderUtil.buildInfo(fileList, fileList.getString(R.string.paste_file), fileList.getString(R.string.please_copy_or_cut_operations));
		}else{
			//复制
			if(currentOpt == 3){
				if(new File(fileList.getCurrentDirectory()+File.separator+tempFile.getName()).exists()){
					Builder builder = BuilderUtil.getBuilder(fileList, fileList.getString(R.string.paste_file),fileList.getString(R.string.the_same_name_exists_overwrite));
					//覆盖同名文件
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(tempFile.isDirectory()){
								try {
									if(getFileSizes(tempFile)>getAvaliableSize()){
										Dialog(R.string.no_enough_space);
									}else{
										showCopyDialog();
										copyFile(tempFile,new File(fileList.getCurrentDirectory()+File.separator+tempFile.getName()));	
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}else{
								try {
									if(getFileSize(tempFile)>getAvaliableSize()){
										Dialog(R.string.no_enough_space);
									}else{
										showCopyDialog();
										copyFile(tempFile,new File(fileList.getCurrentDirectory()+File.separator+tempFile.getName()));	
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
					});
					BuilderUtil.setNegativeButton(builder);
				}else{
					if(tempFile.isDirectory()){
						try {
							if(getFileSizes(tempFile)>getAvaliableSize()){
								Dialog(R.string.no_enough_space);
							}else{
								showCopyDialog();
								copyFile(tempFile,new File(fileList.getCurrentDirectory()+File.separator+tempFile.getName()));	
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						try {
							if(getFileSize(tempFile)>getAvaliableSize()){
								Dialog(R.string.no_enough_space);
							}else{
								showCopyDialog();
								copyFile(tempFile,new File(fileList.getCurrentDirectory()+File.separator+tempFile.getName()));	
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			//剪切
			}else if(currentOpt == 4){
				if(new File(fileList.getCurrentDirectory()+File.separator+tempFile.getName()).exists()){
					Builder builder = BuilderUtil.getBuilder(fileList,fileList.getString(R.string.paste_file),fileList.getString(R.string.the_same_name_exists_overwrite));
					//覆盖同名文件
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							moveFile(tempFile.getAbsolutePath(), fileList.getCurrentDirectory()+File.separator+tempFile.getName());
							fileList.open(new File(fileList.getCurrentDirectory()));
						}
					});
					BuilderUtil.setNegativeButton(builder);
				}else{
					moveFile(tempFile.getAbsolutePath(), fileList.getCurrentDirectory()+File.separator+tempFile.getName());
					fileList.open(new File(fileList.getCurrentDirectory()));
				}
			}
		}
	}
	
	
	public byte[] FormetFileSize(long fileS){
		byte[] buffer;
		if(fileS<1024){
			 buffer = new byte[1024];
		}else if (fileS < 1024*1024){
			 buffer = new byte[1024*5];
		}else if (fileS < 1024*1024*1024){
			 buffer = new byte[1024*60];
		}else{
			 buffer = new byte[1024*100];
		}
		
		return buffer;
	}
	
	public void showCopyDialog(){
		fileList.pDialog = new ProgressDialog(fileList);  
		fileList.pDialog.setMax(100); 
		fileList.pDialog.setTitle(R.string.paste);  
		fileList.pDialog.setMessage(fileList.getString(R.string.copying));
		fileList.pDialog.setCancelable(false);  
		fileList.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); 
		fileList.pDialog.setIndeterminate(false); 
		fileList.pDialog.incrementProgressBy(-fileList.pDialog.getProgress());  
		fileList.pDialog.show();		
	}
	
	//重命名选项
	public void fileRenameItem(final File file){
		final LayoutInflater factory = LayoutInflater.from(fileList);
		final View dialogView = factory.inflate(R.layout.samename, null);
		((EditText)dialogView.findViewById(R.id.edittext2)).setText(file.getName());
		
		Builder builder = BuilderUtil.getBuilder(fileList, "", fileList.getString(R.string.please_input_filename));
		builder.setView(dialogView);
		//确定重命名
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = fileList.getCurrentDirectory() + File.separator +
				((EditText) dialogView.findViewById(R.id.edittext2)).getText().toString();
				//如果文件存在同名文件
				if(new File(value).exists()){
					Builder builder2 = BuilderUtil.getBuilder(fileList, fileList.getString(R.string.rename), fileList.getString(R.string.the_same_name_exists_overwrite));
					//确定覆盖
					builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							String str2 = fileList.getCurrentDirectory()+ File.separator
								+((EditText) dialogView.findViewById(R.id.edittext2)).getText().toString();
							file.renameTo(new File(str2));
							fileList.open(new File(fileList.getCurrentDirectory()));
						}
						
					});
					//取消覆盖
					BuilderUtil.setNegativeButton(builder2);
				}else{
					//不存在同名文件，直接重命名
					file.renameTo(new File(value));
					fileList.open(new File(fileList.getCurrentDirectory()));
				}
			}
		});
		//取消重命名
		BuilderUtil.setNegativeButton(builder);
	}
	
	//删除选项
	public void fileDeleteItem(final File file){
		Builder builder = BuilderUtil.getBuilder(fileList,fileList.getString(R.string.delete), fileList.getString(R.string.sure_to_delete)+file.getName()+fileList.getString(R.string.ma));
		//确定删除
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteFileThread(file);
			}
		});
		//取消删除
		BuilderUtil.setNegativeButton(builder);
	}
	
	/**删除文件夹及文件的线程*/
	private void deleteFileThread(final File file){
		dialog.show();
		new Thread(){
			@Override
			public void run() {		
				if(file.isDirectory()){
					if(deleteFolder(file)){						
						sendDeleteMessage(1,0);
					}else{
						//int i = Integer.parseInt(file.getName());
						sendDeleteMessage(2,0);
					}
				}else{
					if(deleteFile(file)){
						sendDeleteMessage(1,0);
					}else{
						sendDeleteMessage(2,0);
					}
				}
				fileList.open(new File(fileList.getCurrentDirectory()));							
			   super.run();
			}				
		}.start();
	}
	
	/** 菜单项：新建文件夹*/
	public void menuCreateNew(final String[] type){
		final LayoutInflater factory = LayoutInflater.from(fileList);
		final View dialogView = factory.inflate(R.layout.creat_new_files,null);
		((EditText)dialogView.findViewById(R.id.edittext1)).setText(type[1]);
		
		Builder builder = BuilderUtil.getBuilder(fileList, "", type[0]);
		builder.setView(dialogView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final String value = ((EditText)dialogView.findViewById(R.id.edittext1)).getText().toString();
				final String value2 = fileList.getCurrentDirectory()+File.separator+value;
				if(new File(value2).exists()){
					Builder builder2 = BuilderUtil.getBuilder(fileList, "", fileList.getString(R.string.the_same_name_exists_overwrite));
					//确定覆盖
					builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							if(type[2].equals("folder")){
								deleteFolder(new File(value2));
							}else if(type[2].equals("file")){
								deleteFile(new File(value2));
							}
							createNew(value,type[2]);
						}
					});
					//取消覆盖
					BuilderUtil.setNegativeButton(builder2);
				}else{
					createNew(value,type[2]);
				}
			}
		});
		BuilderUtil.setNegativeButton(builder);
	}
	
	/** 新建文件夹 */
	public boolean createNew(String folderName,String type){
		File newFile = new File(fileList.currentDirectory.getAbsolutePath()+File.separator+folderName);
		boolean create = false;
		try {
			if((!newFile.exists())){
				if(type.equals("folder")){
					create = newFile.mkdirs();
				}else if(type.equals("file")){
					create = newFile.createNewFile();
				}
				if(create){
					fileList.open(fileList.currentDirectory);
					create =  true;
				}else{
					create =  false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			create =  false;
		}
		return create;
	}
	
	/** 菜单项：删除文件夹 */
	public void menuDeleteFolder(){
		//获取当前目录
		final File temp = new File(fileList.currentDirectory.getAbsolutePath());
		Builder builder = BuilderUtil.getBuilder(fileList, fileList.getString(R.string.delete_folder), 
				fileList.getString(R.string.this_operation_will_delete)+temp.getName()+
				fileList.getString(R.string.all_files_in_the_folder_and_the_determination_delete_this_folder));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteFolder(temp);
				Toast.makeText(fileList, temp.getName()+fileList.getString(R.string.has_been_deleted), Toast.LENGTH_LONG).show();
				//跳至上一级目录
				upLevel();
			}
		});
		BuilderUtil.setNegativeButton(builder);
		fileList.open(fileList.currentDirectory);
	}
	
	/** 返回上级目录 */
	private void upLevel(){
		if(fileList.currentDirectory.getParent()!=null){
			fileList.open(fileList.currentDirectory.getParentFile());
		}
	}
	
	boolean deleteIsOk;
	public void deleteDialog(final String type){
		new AlertDialog.Builder(fileList)  
			.setTitle(fileList.getString(R.string.operation))  
			.setMessage(fileList.getString(R.string.have_to_add_delete))  
			.setNegativeButton(fileList.getString(R.string.cancel), new DialogInterface.OnClickListener() {   
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();    
				}   
			})
			.setPositiveButton(fileList.getString(R.string.ensure),  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				if(type.equals("music")){
					 deleteIsOk = dbMusicHelper.deleteMusicPath(FileListActivity.deleteMusicList);
				}else if(type.equals("poster")){
					deleteIsOk = dbPosterHelper.deletePosterPath(FileListActivity.deletePosterList);
				}				
				if(deleteIsOk){
					Toast.makeText(fileList, fileList.getString(R.string.successfully_deleted), 1).show();
				}else{
					Toast.makeText(fileList, fileList.getString(R.string.failed_to_delete), 1).show();
				}
				dialog.dismiss();
			}  
		}).show(); 
	}
		
	public void Dialog(int hint){
		new AlertDialog.Builder(fileList)  
			.setTitle(fileList.getString(R.string.hint))  
			.setMessage(hint)  
			.setPositiveButton(fileList.getString(R.string.ensure),  new DialogInterface.OnClickListener() {  
			@Override  
			public void onClick(DialogInterface dialog, int which) {  
				dialog.dismiss();
			}  
		}).show(); 
	}

	/*获取文件夹大小*/
	public long getFileSizes(File f) throws Exception
	{
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++)
		{
			if (flist[i].isDirectory())
			{
				size = size + getFileSizes(flist[i]);
			}
			else
			{
				size = size + getFileSize(flist[i]);
			}
	  }
	  return size;
	}
	/*获取文件大小*/
	private static long getFileSize(File file) throws Exception
	{
		long size = 0;
		if (file.exists()){
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		}
		return size;
	}
	/*获取空间剩余大小*/
	public long getAvaliableSize(){	
		System.out.println(" fileList.getCurrentDevicePath() = "+fileList.getCurrentDevicePath());
		StatFs statFs=new StatFs(fileList.getCurrentDevicePath());
		long blockSize = statFs.getBlockSize(); 
		long avaliableBlocks = statFs.getAvailableBlocks(); 
		System.out.println(" avaliableBlocks*blockSize = "+avaliableBlocks*blockSize);		
		return avaliableBlocks*blockSize;
	}	
	
	public void sendDeleteMessage(int w,int name){
		
		Message m = new Message();  
		m.arg1 = w;  
		m.arg2 = name; 
		mHandler.sendMessage(m); 
	}

	Handler mHandler = new Handler() {  
		@Override  
		public void handleMessage(Message msg) {  
			switch(msg.arg1){
		  		case 1:
		  			Toast.makeText(fileList, fileList.getString(R.string.has_been_deleted), Toast.LENGTH_LONG).show();
		  			break;
		  		case 2:
		  			Toast.makeText(fileList, fileList.getString(R.string.failed_to_delete), Toast.LENGTH_LONG).show();
		  		break;		  		
			} 
			dialog.dismiss();
			super.handleMessage(msg);
		}  
	};
}
