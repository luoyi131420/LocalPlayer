package com.kt.localmedia.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GLFiles {

	public List<File> listFiles = new ArrayList<File>();
	private File [] files=null;
	
	/*
	 * 过滤图片以为的文件
	 * 
	 * */
	public List<File> getIMGList(String path){
		
		File file =new File(path);
		if(file.exists()){
		    files = file.listFiles();
			if(!listFiles.isEmpty()){
				listFiles.clear();
			}
			for(int i=0;i<files.length;i++){
		    	if(files[i].isFile()){
		    		if(files[i].getName().endsWith(".jpg")||files[i].getName().endsWith(".bmp")||files[i].getName().endsWith(".gif")||
			    			files[i].getName().endsWith(".png")||files[i].getName().endsWith(".jpeg")){
			    		listFiles.add(files[i]);
			    	}
		    	}
		    	
		    	else if(files[i].isDirectory()){
		    		    listFiles.add(files[i]);
		    	}
		    }
		}

		return listFiles;
	}
}
