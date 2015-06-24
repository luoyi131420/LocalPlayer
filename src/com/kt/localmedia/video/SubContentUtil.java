package com.kt.localmedia.video;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.kt.localmedia.util.DebugAnno;
import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.video.subtitle.AliasUtil;
import com.kt.localmedia.video.subtitle.AssDecoder;
import com.kt.localmedia.video.subtitle.MicroSubDecoder;
import com.kt.localmedia.video.subtitle.SmiDecoder;
import com.kt.localmedia.video.subtitle.SrtDecoder;
import com.kt.localmedia.video.subtitle.SubDecoder;
import com.kt.localmedia.video.subtitle.SubtitleContent;
import com.kt.localmedia.video.subtitle.SubtitleDecoder;
import com.kt.localmedia.video.subtitle.autodetect.AutoDetectUtil;

/*".srt", ".smi", ".ass", ".ssa", ".sub"*/
@DebugAnno(isEnableDebug=false)
public class SubContentUtil {
	private static final String TAG = "KTPlayer";
	//private static VideoDisplayView mActivity = null;

	public static int mOldSubtitleIndex = 0;
	public static boolean stopDecodeFlag = false;
	
	public static String removeHtmlTag(String input){
		String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
		str = str.replaceAll("[(/>)<]", "");
		return str;
	}
	
	public static ArrayList<String> getSubtitleFile(Context context, String videoPath){
//		String scheme = uri.getScheme();
//		
//		String videoFullPath = null;
//		if("content".equals(scheme)){
////			Cursor cur = DBUtils.getCurrentCursor(context, uri);
////            if (cur == null){return null;}
////            videoFullPath = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
////            cur.close();
//		}else if("file".equals(scheme)){
//			videoFullPath = uri.getPath();
//		}else if("http".equals(scheme)){
//			return null;
//		}
		
//		String videoPath = videoFullPath;
		if(videoPath == null){
			return null;
		}
		
		int end = videoPath.lastIndexOf("/", videoPath.length());
		String path = videoPath.substring(0, end + 1);
		end = videoPath.lastIndexOf(".", videoPath.length());
		if(-1   == end ){ return null;}
		if(null == path){ return null;}

		String subffix = videoPath.substring(0, end);
		LogUtil.LogPlayer(TAG, "subffix="+subffix);
		File files = new File(path);
		if ((files != null) && (files.exists())&& (files.isDirectory())) {
		    File[] filesInDir = files.listFiles();
		    long count = filesInDir.length;
		    ArrayList<String> SubtitlePath  = null;
		    SubtitlePath = new ArrayList<String>();
		    for (int num = 0; num < count; num++) {
		        String filePath = filesInDir[num].getPath();
		        File subTitleFile = new File(filePath);
		        if ((subTitleFile != null) && (subTitleFile.isFile())&&(subTitleFile.canRead())) {
		            int pos = filePath.lastIndexOf(".",filePath.length());
		            String sub = filePath.substring(pos + 1,filePath.length());
		            if ((filePath.startsWith(subffix))&& (sub != null) && ((sub.equalsIgnoreCase("srt"))
		                     || (sub.equalsIgnoreCase("ass")) || (sub.equalsIgnoreCase("smi"))
		                     || (sub.equalsIgnoreCase("ssa")) || (sub.equalsIgnoreCase("sub")))) {
		            	LogUtil.LogPlayer(TAG, "filePath="+filePath);
		                SubtitlePath.add(filePath);
		            }
		        }
		    }
		    if (SubtitlePath.size() != 0){
		    	//LogUtil.LogPlayer(TAG, "SubtitlePath="+SubtitlePath);
		        return SubtitlePath;
		    }
		}
		return null;
	}
	
    // Add by fxw
	static class SubtitleDecoderResult {
		public boolean isSuccess=true;
		public boolean isPictureSub;
		public boolean isTextSub;
		public Map<String, List<SubtitleContent>> subtitleContentMap;
		public SubtitleDecoder subtitleDecoder;
		
		public void setResult(SubtitleDecoderResult result){
			isSuccess = result.isSuccess;
			isPictureSub = result.isPictureSub;
			isTextSub = result.isTextSub;
			subtitleContentMap = result.subtitleContentMap;
			subtitleDecoder = result.subtitleDecoder;
		}
		
	}
	
	public static void decodePictureSubtitle(String subTitlePath, SubtitleContent content, SubtitleDecoderResult decoderResult, int screenWidth){
		if(decoderResult.isSuccess&&decoderResult.isPictureSub){
			SubDecoder subdecoder = (SubDecoder)decoderResult.subtitleDecoder;//Idx+Sub
			subdecoder.decodePictureSubTitle(subTitlePath, content, screenWidth);
			SubtitleContent subtitleContent = getCurrentSubtitleContent(content, decoderResult.subtitleContentMap);
			subtitleContent.setSubtitleEndTime(content.getSubtitleEndTime());//update endtime in subtitle list.
		}
	}
	
	public static SubtitleDecoderResult decodeSubtitle2(String subTitlePath){
		mOldSubtitleIndex =0;
		SubtitleDecoderResult result = new SubtitleDecoderResult();
		result.isPictureSub = false;
		result.isTextSub = true;
		if(subTitlePath.toLowerCase().endsWith(".srt")){
			try {
				result.subtitleDecoder = new SrtDecoder();
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subTitlePath,null);
				result.isSuccess = true;
			} catch (Exception e) {
				try {
					result.subtitleDecoder = new SrtDecoder();
					result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subTitlePath,"ISO-8859-1");
					result.isSuccess = true;
				} catch (Exception e1) {
					result.isSuccess = false;
					result.subtitleContentMap = null;
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}else if(subTitlePath.toLowerCase().endsWith(".ass") || subTitlePath.toLowerCase().endsWith(".ssa")){
			try {
				result.subtitleDecoder = new AssDecoder();
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subTitlePath,null);
				result.isSuccess = true;
			} catch (Exception e) {
				result.isSuccess = false;
				result.subtitleContentMap = null;
				e.printStackTrace();
			}
		}else if(subTitlePath.toLowerCase().endsWith(".smi")){
			try {
				result.subtitleDecoder = new SmiDecoder();
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subTitlePath,null);
				result.isSuccess = true;
			} catch (Exception e) {
				result.isSuccess = false;
				result.subtitleContentMap = null;
				e.printStackTrace();
			}
		}else if(subTitlePath.toLowerCase().endsWith(".sub")){
			try {
				String subtitleIdxPath = subTitlePath.substring(0, subTitlePath.lastIndexOf("."))+".idx";
				if(new File(subtitleIdxPath).exists()){
					result.subtitleDecoder = new SubDecoder();
					result.isPictureSub = true;
					result.isTextSub = false;
				}else{
					result.subtitleDecoder = new MicroSubDecoder();
				}
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subTitlePath,null);
				result.isSuccess = true;
			} catch (Exception e) {
				result.isSuccess = false;
				result.subtitleContentMap = null;
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public static void updateMicroDVDSubtitle(Map<String, List<SubtitleContent>> subtitleList, double framerate){
		Iterator<String> keyIt = subtitleList.keySet().iterator();
		String keyStr = null;
		List<SubtitleContent> contentList = null;
		while(keyIt.hasNext()){
			keyStr = keyIt.next();
			contentList = subtitleList.get(keyStr);
			if(contentList == null || contentList.size() == 0){
				continue;
			}
			for(SubtitleContent content : contentList){
				content.setSubtitleStartTime((int)(content.getSubtitleStartTime()*1000/framerate));
				content.setSubtitleEndTime((int)(content.getSubtitleEndTime()*1000/framerate));
			}
		}
	}
	
	//return simple copy object
	public static SubtitleContent getNextSubtitleContent(SubtitleContent currContent/*SimpleCopy*/, final Map<String, List<SubtitleContent>> subtitleList){
		Iterator<String> keyIt = subtitleList.keySet().iterator();
		String keyStr = null;
		List<SubtitleContent> contentList = null;
		while(keyIt.hasNext()){
			keyStr = keyIt.next();
			contentList = subtitleList.get(keyStr);
			if(contentList == null || contentList.size() == 0){
				continue;
			}
			//guess next
			SubtitleContent tmpcontent = null;
			int subIndex = currContent.getSubtitleIndex()-contentList.get(0).getSubtitleIndex()+1;
			if(subIndex>=0&&subIndex<contentList.size()){
				tmpcontent = contentList.get(subIndex);
				return tmpcontent.getSimpleCopy();
			}
		}
		return null;
	}
	
	//return object in the subtitlelist
	private static SubtitleContent getCurrentSubtitleContent(SubtitleContent currContent, final Map<String, List<SubtitleContent>> subtitleList){
		Iterator<String> keyIt = subtitleList.keySet().iterator();
		String keyStr = null;
		List<SubtitleContent> contentList = null;
		while(keyIt.hasNext()){
			keyStr = keyIt.next();
			contentList = subtitleList.get(keyStr);
			if(contentList == null || contentList.size() == 0){
				continue;
			}
			SubtitleContent tmpcontent = null;
			int subIndex = currContent.getSubtitleIndex()-contentList.get(0).getSubtitleIndex();
			if(subIndex>=0&&subIndex<contentList.size()){
				tmpcontent = contentList.get(subIndex);
				return tmpcontent;
			}
		}
		return null;
	}
	
	public static Map<String, List<SubtitleContent>> decodeSubtitle(String subTitlePath){
		LogUtil.LogPlayer(TAG, "Map decodeSubtitle");
		if(subTitlePath.toLowerCase().endsWith(".srt")){
			try {
				return new SrtDecoder().decodeSubtitle(subTitlePath,null);
			} catch (Exception e) {
				try {
					return new SrtDecoder().decodeSubtitle(subTitlePath,"ISO-8859-1");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}else if(subTitlePath.toLowerCase().endsWith(".ass") || subTitlePath.toLowerCase().endsWith(".ssa")){
			try {
				return new AssDecoder().decodeSubtitle(subTitlePath,null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(subTitlePath.toLowerCase().endsWith(".smi")){
			try {
				return new SmiDecoder().decodeSubtitle(subTitlePath,null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(subTitlePath.toLowerCase().endsWith(".sub")){
			try {
				return new SubDecoder().decodeSubtitle(subTitlePath,null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public synchronized static SubtitleContent get2ndSubtitleContent(int pos, int childIndex,
		       final Map<String, List<SubtitleContent>> subtitleList){
        int keyIndex = 0;
        Iterator<String> keyIt = subtitleList.keySet().iterator();
        List<SubtitleContent> contentList = null;
        while(keyIt.hasNext()){
			String keyStr = keyIt.next();
			if(keyIndex==childIndex){
				contentList = subtitleList.get(keyStr);
				break;
			}else{
				keyIndex++;
			}
        }

        if((contentList==null)||(contentList.size()==0)){
			LogUtil.LogPlayer(TAG,"get2ndSubtitleContent contentList==null");
			return null;
        }

        int iTryCount = 0;
        SubtitleContent tmpcontent = null;
        SubtitleContent content = null;
		//scan forward
		while(true){
			try {
				if(mOldSubtitleIndex + iTryCount>=contentList.size()){break;}
				tmpcontent = contentList.get(mOldSubtitleIndex + iTryCount);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				break;
			}
			int iTimeStart = tmpcontent.getSubtitleStartTime();
			int iTimeEnd   = tmpcontent.getSubtitleEndTime();
			if(iTimeStart <= pos && iTimeEnd >= pos){
				content = tmpcontent.getSimpleCopy();
				mOldSubtitleIndex += iTryCount;
			    return content;
			}else if(pos < iTimeStart){
				break;
			}
			iTryCount++;
		}

        //scan backward
        iTryCount = 0;
		while(true){
			try {
				if(mOldSubtitleIndex - iTryCount<0){break;}
                tmpcontent = contentList.get(mOldSubtitleIndex - iTryCount);
			} catch (Exception e) {
                // TODO Auto-generated catch block
                break;
			}
			int iTimeStart = tmpcontent.getSubtitleStartTime();
			int iTimeEnd   = tmpcontent.getSubtitleEndTime();
			if(iTimeStart <= pos && iTimeEnd >= pos){
				content = tmpcontent.getSimpleCopy();
				mOldSubtitleIndex -= iTryCount;
			    return content;
			}

			iTryCount++;
		}
		
		return null;
	}
	
	public static String encodeType = null;
	/**
	 * detect encode type
	 * @param subTitlePath
	 * @return InputStreamReader with encode deal
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static BufferedReader getEncodeType(String subTitlePath) throws FileNotFoundException, UnsupportedEncodingException{
		InputStream encodeinput = new FileInputStream(subTitlePath);
		BufferedInputStream encodebin = new BufferedInputStream(encodeinput);
		encodeType = AutoDetectUtil.detectEncode(6, encodebin);
		
		try {
			encodebin.close();
			encodebin = null;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//It seems unecessary
		try {
			encodeinput.close();
			encodeinput = null;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		InputStream in = new FileInputStream(subTitlePath);
		BufferedInputStream bin = new BufferedInputStream(in);
		InputStreamReader isr = null;
		
		if(encodeType != null && !"ascii".equals(encodeType)){
			if(subTitlePath.endsWith("srt") && encodeType.equals("windows-1252")){
				encodeType = "Unicode";
			}if(subTitlePath.endsWith("smi") && encodeType.equals("windows-1252")){
				encodeType = "GB2312";
			}else{
				encodeType = AliasUtil.getAlias(encodeType);
			}
			if("nomatch".equals(encodeType) || "Shift_JIS".equals(encodeType)){
				encodeType = "windows-1253";
			}
			isr = new InputStreamReader(bin, encodeType);
		}else{
			isr = new InputStreamReader(bin);
		}
		BufferedReader br = new BufferedReader(isr);
		return br;
	}
	public static BufferedReader getEncodeType(String subTitlePath,String encodeType) throws FileNotFoundException, UnsupportedEncodingException{
		InputStream in = new FileInputStream(subTitlePath);
		BufferedInputStream bin = new BufferedInputStream(in);
		InputStreamReader isr = null;
		if(encodeType != null && !"ascii".equals(encodeType)){	
			isr = new InputStreamReader(bin, encodeType);
		}else{
			isr = new InputStreamReader(bin);
		}
		
		BufferedReader br = new BufferedReader(isr);
		return br;
	}
	
	public static void SortAllList(Map<String, List<SubtitleContent>> subTitleMap){
		Set<String> keySet = subTitleMap.keySet();
		Iterator<String> keyIt = keySet.iterator();
		
		String key = null;
		List<SubtitleContent> subTitleList = null;
		while(keyIt.hasNext()){
			key = keyIt.next();
			subTitleList = subTitleMap.get(key);
			
			Collections.sort(subTitleList);
		}
	}
}
