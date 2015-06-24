package com.kt.localmedia.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class BuilderUtil {
	private static AlertDialog.Builder builder;
	
	//������ʾ��Ϣ�Ի���
	public static void buildInfo(Activity activity,String title,String message){
		builder = new AlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}
	
	//��ȡbuilder
	public static AlertDialog.Builder getBuilder(Activity activity,String title,String message){
		builder = new AlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setMessage(message);
		return builder;
	}
	
	//����ȡ��ť
	public static void setNegativeButton(AlertDialog.Builder builder){
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}
}
