/**
 * 
 */
package com.kt.localmedia.video.subtitle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.kt.localmedia.util.DebugAnno;
import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.video.SubContentUtil;
import com.kt.localmedia.video.subtitle.autodetect.AutoDetectUtil;

//import org.apache.http.util.EncodingUtils;


@DebugAnno(isEnableDebug=false)
public class SrtDecoder extends SubtitleDecoder {
	private final String mTag = "KTPlayer";
	private static int count = 0;
	
	private int getSrtTime(String str){
		int targetTime = 0;
	    int hour, minute, second, micosecond;
	    String strHour, strMinute, strSecond, strMicosecond;

	    strHour = str.substring(0, 2);
	    strMinute = str.substring(3, 5);
	    strSecond = str.substring(6, 8);
	    strMicosecond = str.substring(9, 12);

	    hour = Integer.parseInt(strHour);
	    minute = Integer.parseInt(strMinute);
	    second = Integer.parseInt(strSecond);
	    micosecond = Integer.parseInt(strMicosecond);

	    targetTime = hour * 3600000 + minute * 60000 + second * 1000 + micosecond;

	    return targetTime;
	}
	
	private boolean isNumberic(String str){
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.rk.RockVideoPlayer.subtitle.SubtitleDecoder#decodeSubtitle()
	 */
	@Override
	public Map<String, List<SubtitleContent>> decodeSubtitle(String subTitlePath,String encode) throws Exception{
		// TODO Auto-generated method stub
		LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type srt> path="+subTitlePath);

		//Auto-Detect Encode of SRT Subtitile
		InputStream encodeinput = new FileInputStream(subTitlePath);
		BufferedInputStream encodebin = new BufferedInputStream(encodeinput);
		String encodeType = AutoDetectUtil.detectEncode(6, encodebin).trim();
		LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type sub-text> EncodeType="+encodeType);
       
		
		try {
			encodebin.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			encodeinput.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		//Create InputStream of SRT Subtitle(Encode must Right)
		BufferedReader srtReader = null;
		if(encodeType == null){
			srtReader = SubContentUtil.getEncodeType(subTitlePath);
		}else{
			srtReader = SubContentUtil.getEncodeType(subTitlePath,encodeType);
		}
		String subTitleLine = null;
		String regex="\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d --> \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d";
		SubtitleContent content = null;
		Map<String, List<SubtitleContent>> subTitleMap = new TreeMap<String, List<SubtitleContent>>();
		
		List<SubtitleContent> subTitleList = new ArrayList<SubtitleContent>();
		StringBuffer subTitleBuf = new StringBuffer("");
		String preLine = null;
		String subTitleStr = null;
		int languageIndex = 1;
		int preLineNum = 0;
		String subtitle = null;
		int starttime = 0;
		int endtime = 0;
		int prestarttime = 0;
		
		Map<Integer, SubtitleContent> subTimeMap = new TreeMap<Integer, SubtitleContent>();
		
		while((subTitleLine = srtReader.readLine()) != null && SubContentUtil.stopDecodeFlag == false){
			subTitleLine = subTitleLine.trim();
			if(count == 0 && "1".equals(subTitleLine)){
				preLine = subTitleLine;
				preLineNum = 1;
				continue;
			}
			
			if((null!=encodeType) && encodeType.equalsIgnoreCase("Big5")){
			    //subTitleLine = EncodingUtis.getString(subTitleLine.getBytes("Big5"),"GB2312");
			    subTitleLine = new String(subTitleLine.getBytes("Big5"),"GB2312");
			}
				
			if(Pattern.matches(regex, subTitleLine)){
				content = new SubtitleContent();
				content.setSubtitleIndex(count++);
				starttime = getSrtTime(subTitleLine.split("-->")[0].trim());
				content.setSubtitleStartTime(starttime);
				endtime = getSrtTime(subTitleLine.split("-->")[1].trim());
				content.setSubtitleEndTime(endtime);
				
				if(starttime == 0 && endtime == 0)
					continue;
				
				if(prestarttime >  starttime){
					subTitleMap.put(String.valueOf(languageIndex), subTitleList);
					languageIndex++;
					count = 0;
					subTitleList = new ArrayList<SubtitleContent>();
				}
				
				content.setmLanguageClass(String.valueOf(languageIndex));
				
				if(!subTimeMap.containsKey(starttime)){
					subTimeMap.put(starttime, content);
				}else if(content.getmLanguageClass().equals(subTimeMap.get(starttime).getmLanguageClass())){
					//multie subline with the same time and the same language class
					content = subTimeMap.get(starttime);
				}
				
				prestarttime = starttime;
			}else if(isNumberic(subTitleLine) && "".equals(preLine)){
				subTitleStr = subTitleBuf.toString().trim();
				if("".equals(subTitleStr)){
					continue;
				}
				subtitle = subTitleStr.substring(0, subTitleStr.length() - "<br>".length()).trim();
				
				if(content.getSubtitleLine() != null && content.getSubtitleLine().trim().length() > 0)
					content.setSubtitleLine(content.getSubtitleLine() + subtitle);
				else
					content.setSubtitleLine(subtitle);
				if(!subTitleList.contains(content))
					subTitleList.add(content);
				content = null;
				subTitleBuf.delete(0, subTitleBuf.length());
				
				if(Integer.parseInt(subTitleLine) < preLineNum){
					subTitleMap.put(String.valueOf(languageIndex), subTitleList);
					languageIndex++;
					count = 0;
					subTitleList = new ArrayList<SubtitleContent>();
				}
				
				preLineNum = Integer.parseInt(subTitleLine);
			}else{
				if(!"".equals(subTitleLine.trim()))
					subTitleBuf.append(subTitleLine + "<br>");
			}
			
			preLine = subTitleLine;
		}
				
		subTitleStr = subTitleBuf.toString().trim();
		if(!"".equals(subTitleStr)){
			subtitle = subTitleStr.substring(0, subTitleStr.length() - "<br>".length()).trim();
			if(encode == null){
			    subTitleList.add(content);
			    content = null;
			}else{
				try{
					content.setSubtitleLine(subtitle);
					subTitleList.add(content);
					content = null;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		subTitleMap.put(String.valueOf(languageIndex), subTitleList);

		try {
			srtReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SubContentUtil.SortAllList(subTitleMap);
		LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type srt> SubtitleMap.size()="+subTitleMap.size());
		return subTitleMap;
	}

}
