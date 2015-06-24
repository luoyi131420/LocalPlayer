package com.kt.localmedia.pic.anim;

import com.kt.localmedia.pic.ITurnPage;
import com.kt.localmedia.util.KTBitmapsf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * 
 * @author yanglonghui
 *
 */
public class BlackSquareZoomIn implements ITurnPage {

	private int duration=100;//动画持续时间
	private final int leafNum=15;
	private PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	private Bitmap newBitmap;
	private int bitmapW,bitmapH;
	public BlackSquareZoomIn() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTurnPageDraw(SurfaceHolder holder, Bitmap[] bitmap,
			int maxWidth, int maxHeight) {
     KTBitmapsf ktsf = new KTBitmapsf();
		
		if(bitmap[0].getWidth()>maxWidth){
			newBitmap =ktsf.smallW(bitmap[0],maxWidth);
		}
		else if(bitmap[0].getHeight()>maxHeight){
			newBitmap =ktsf.smallH(bitmap[0],maxHeight);
		}
		else{
			newBitmap =ktsf.getMBitmap(bitmap[0]);
		}
		
		
		bitmapW =newBitmap.getWidth();
		bitmapH = newBitmap.getHeight();
		
		int dx=(maxWidth-bitmapW)/2;
		int dy=(maxHeight-bitmapH)/2;
		
		int perWidth=bitmapW/leafNum;
		int row=bitmapH/bitmapW;
		int perHeight=bitmapH/row;

		Rect[][] array=new Rect[row][leafNum];
		for(int i=0;i<array.length;i++)
		{
			for(int j=0;j<array[i].length;j++)
			{
				array[i][j]=new Rect();
				array[i][j].set(j*perWidth, i*perHeight, (j+1)*perWidth, (i+1)*perHeight);
				
				if(j==array[i].length-1)
				{
					array[i][j].set(bitmapH-perWidth, array[i][j].top, bitmapW, array[i][j].bottom);
				}
				
				if(i==array.length-1)
				{
					array[i][j].set(array[i][j].left, bitmapH-perHeight, array[i][j].right, bitmapH);
				}
			}
		}
		
		long start=System.currentTimeMillis();
		long runMills=0;
		
		
		Paint mRectPaint=new Paint();
		mRectPaint.setColor(Color.BLACK);
		Rect drawRect=new Rect();
		
		Canvas canvas=null;
		boolean isRunning=true;
		while(isRunning)
		{
			isRunning=((runMills=(System.currentTimeMillis()-start))<duration);
			if(!isRunning)
			{
				runMills=duration;
			}
			
			try {
					canvas=holder.lockCanvas(null);
					
					canvas.setDrawFilter(pdf);
					canvas.drawColor(Color.BLACK);// 清除画布
					canvas.drawBitmap(newBitmap, dx, dy, null);
					
					for(int i=0;i<array.length;i++)
					{
						for(int j=0;j<array[i].length;j++)
						{
							drawRect.set(array[i][j]);
							int _dx = (int)(((float)runMills/(float)duration)*drawRect.width()/2);
							int _dy = (int)(((float)runMills/(float)duration)*drawRect.height()/2);
							
							drawRect.inset(_dx, _dy);
							canvas.drawRect(drawRect, mRectPaint);
						}
					}
					
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(null!=canvas)
				{
					holder.unlockCanvasAndPost(canvas);
				}
				else
				{
					break;
				}
				
				if(!isRunning)
				{
					break;
				}
			}
		}
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub

	}

}
