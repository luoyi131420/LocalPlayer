/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年8月6日 下午10:20:23  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年8月6日      fxw         1.0         create
*******************************************************************/   

package com.kt.localmedia.video.subtitle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.video.subtitle.autodetect.AutoDetectUtil;

public class MicroSubDecoder extends SubtitleDecoder {
    private final String mTag = "MicroSubDecoder";

	@Override
	public Map<String, List<SubtitleContent>> decodeSubtitle(
			String subTitlePath, String encode) throws Exception {
		LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type sub-text> path="+subTitlePath);

		//Auto-Detect Encode of MicroSub Subtitile
		InputStream encodeinput = new FileInputStream(subTitlePath);
		BufferedInputStream encodebin = new BufferedInputStream(encodeinput);
		String encodeType = AutoDetectUtil.detectEncode(6, encodebin);
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

		//Create InputStream of MicroSub Subtitle(Encode must Right)
		InputStreamReader isr = null;
		InputStream in = new FileInputStream(subTitlePath);
		BufferedInputStream bin = new BufferedInputStream(in);
		if (encodeType != null && !"ascii".equals(encodeType)) {
			if ("windows-1252".equalsIgnoreCase(encodeType)) {
				encodeType = "Unicode";
			}
			isr = new InputStreamReader(bin, encodeType);
		} else {
			isr = new InputStreamReader(bin);
		}

		BufferedReader br = new BufferedReader(isr);
		Map<String, List<SubtitleContent>> subTitleMap = new TreeMap<String, List<SubtitleContent>>();
		SubtitleContent content = null;
		String subTitleLine = null;
		String langStr = null;
		int time = 0;
		int endTime = 0;
		int indexCount = 0;
		try{
			while ((subTitleLine = br.readLine()) != null) {
				subTitleLine = subTitleLine.trim();
	
				String str[] = subTitleLine.split("\\}");
				content = new SubtitleContent();
				content.setSubtitleIndex(indexCount++);
				if (str.length == 3) {
					String str0 = str[0].substring(str[0].indexOf("{") + 1);
					String str1 = str[1].substring(str[1].indexOf("{") + 1);
					String str2 = str[2];
					if(str2.contains("|")){
						String lines[] = str2.split("\\|");
						StringBuilder sb = new StringBuilder();
						for(int k=0; k<lines.length; k++){
							sb.append(lines[k]);
							if(k<lines.length-1){
								sb.append("<br>");
							}
						}
						str2 = sb.toString();
					}
					langStr = "SUB" + 0;
					if(!subTitleMap.containsKey(langStr)){
						subTitleMap.put(langStr, new ArrayList<SubtitleContent>());
					}
					time = (int) (Integer.valueOf(str0));
					endTime = (int) (Integer.valueOf(str1));
					content.setSubtitleStartTime(time);
					content.setSubtitleEndTime(endTime);
					content.setSubtitleLine(str2);
					subTitleMap.get(langStr).add(content);
				}
			}
		
		} finally {
			br.close();
		}
		return subTitleMap;
	}

}
