package com.kt.localmedia.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class KTBitmapsf {

	     private  Bitmap big(Bitmap bitmap) {
		  Matrix matrix = new Matrix(); 
		  matrix.postScale(1.5f,1.5f); //长和宽放大缩小的比例
		  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		  return resizeBmp;
		 }
	     
	     public Bitmap smallWH(Bitmap bitmap,int maxW,int maxH){
	    	  Matrix matrix = new Matrix(); 
			  float picBlW = 0;
			  float picBlH = 0;
			  
			  if(bitmap.getWidth()>bitmap.getHeight()){
				  picBlW = (float)maxW/bitmap.getWidth();
				 //picBlH = (float)maxH/bitmap.getHeight();
				 matrix.postScale(picBlW,picBlW); 
			  }else if(bitmap.getHeight()>bitmap.getWidth()){				  
				 picBlW = (float)bitmap.getWidth()/(bitmap.getHeight()/maxH)/maxW;
				 picBlH =(float)maxH/bitmap.getHeight();
				 matrix.postScale(picBlH,picBlH); 
			  }else{
				 picBlW = (float)maxW/bitmap.getWidth();
				 picBlH = (float)maxH/bitmap.getHeight();
				 matrix.postScale(picBlW,picBlH); 
			  }
			  //matrix.postScale(picBlH,picBlH); //长和宽放大缩小的比例
			  System.out.println("----------------this is1 ------"+picBlW+"--------"+bitmap.getWidth()+"----"+bitmap.getHeight());
			  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		
			  return resizeBmp;
	     }

	     public Bitmap scalWH(Bitmap bitmap,float maxW,float maxH){
	    	  Matrix matrix = new Matrix(); 
			  float picBlW = (float)maxW/bitmap.getWidth();
			  float picBlH = (float)maxH/bitmap.getHeight();
			  matrix.postScale(picBlW,picBlH); //长和宽放大缩小的比例
			  System.out.println("----------------this is1 ------"+picBlW+"--------"+bitmap.getWidth()+"----"+bitmap.getHeight());
			  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		
			  return resizeBmp;
	     }

		 public  Bitmap smallW(Bitmap bitmap,int max) {
		  Matrix matrix = new Matrix(); 
		  float picBl = (float)max/bitmap.getWidth();
		  matrix.postScale(picBl,picBl); //长和宽放大缩小的比例
		  System.out.println("----------------this is1 ------"+picBl+"--------"+bitmap.getWidth()+"----"+bitmap.getHeight());
		  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
	
		  return resizeBmp;
		 }
		 public  Bitmap smallH(Bitmap bitmap,int max) {
			  Matrix matrix = new Matrix(); 
			  float picBl = (float)max/bitmap.getHeight();
			  matrix.postScale(picBl,picBl); //长和宽放大缩小的比例
			  System.out.println("----------------this is2 ------"+picBl+"--------"+bitmap.getWidth()+"----"+bitmap.getHeight());
			  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
			
			  return resizeBmp;
			 }
		 
		 public Bitmap getMBitmap(Bitmap bitmap){
			 return bitmap;
		 }
}
