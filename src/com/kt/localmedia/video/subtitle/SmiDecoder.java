/**
 * 
 */
package com.kt.localmedia.video.subtitle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.kt.localmedia.util.DebugAnno;
import com.kt.localmedia.util.LogUtil;
import com.kt.localmedia.video.SubContentUtil;


/**
 * @author hm
 *
 */
@DebugAnno(isEnableDebug=false)
public class SmiDecoder extends SubtitleDecoder {
	private final String mTag = "SmiDecoder";

	private List<String> getLangStr(String str) throws IOException{
		Reader reader = new StringReader(str);
		BufferedReader r = new BufferedReader(reader);
		String line;
		List<String> langList = new ArrayList<String>();
		
		while((line=r.readLine())!=null) {
			line = line.trim();
			if(line.startsWith(".")){
				if(line.indexOf("{") == -1){
					langList.add(line.substring(1).trim());
				}else{
					langList.add(line.substring(1, line.indexOf("{")).trim());
				}
			}
		}
		
		return langList;
	}
	
	private String getClassName(String str) {
		StringTokenizer st = new StringTokenizer(str, " ");
		String tmp = null;
		while(st.hasMoreTokens()){
			tmp=st.nextToken();
			
			if(tmp.toUpperCase().trim().startsWith("CLASS")){
				return tmp.trim().split("=")[1].split(" ")[0];
			}
		}
		
		return null;
	}
	
	private void getTimeStr(SubtitleContent content, String str){
		StringTokenizer st = new StringTokenizer(str, " ");
		String tmp = null;
		while(st.hasMoreTokens()){
			tmp=st.nextToken();
			if(tmp.toUpperCase().trim().startsWith("START")){
				content.setSubtitleStartTime(Integer.valueOf(tmp.trim().split("=")[1].split(" ")[0]));
			}else if(tmp.toUpperCase().trim().startsWith("END")){
				content.setSubtitleEndTime(Integer.valueOf(tmp.trim().split("=")[1].split(" ")[0]));
			}
		}
	}
	
	private int getStartTime(String str){
		StringTokenizer st = new StringTokenizer(str, " ");
		String tmp = null;
		while(st.hasMoreTokens()){
			tmp=st.nextToken();
			if(tmp.toUpperCase().trim().startsWith("START")){
				return Integer.valueOf(tmp.trim().split("=")[1].split(" ")[0]);
			}
		}
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see android.rk.RockVideoPlayer.subtitle.SubtitleDecoder#decodeSubtitle(java.lang.String)
	 */
	@Override
	public Map<String, List<SubtitleContent>> decodeSubtitle(String subTitlePath,String encode)
			throws Exception {
		// TODO Auto-generated method stub
		LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> path="+subTitlePath);
		BufferedReader br = SubContentUtil.getEncodeType(subTitlePath);
		Map<String, List<SubtitleContent>> subTitleMap = new TreeMap<String, List<SubtitleContent>>();
		List<String> langList = new ArrayList<String>();
		SubtitleContent content = null;
		int indexCount = 0;
		int preTagType = SmiTokenizer.SMI_UNKNOWN;
		
		SmiTokenizer smiTokenizer = new SmiTokenizer(br);
		int tagType = SmiTokenizer.SMI_UNKNOWN;
		List<SubtitleContent> subTitleList = null;
		boolean hasMetNbsp = false;
		String subTitleLine = null;
		
		while((tagType = smiTokenizer.nextHtml()) != SmiTokenizer.SMI_EOF  && SubContentUtil.stopDecodeFlag == false){
			if(tagType == SmiTokenizer.SMI_SYNC){
				if(content != null && content.getmLanguageClass() != null && subTitleMap.get(content.getmLanguageClass()) != null){
					if(hasMetNbsp == false && content.getSubtitleEndTime() == 0){
						content.setSubtitleEndTime(getStartTime(smiTokenizer.sval));
					}
					LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SmiTokenizer.SMI_SYNC ");
					if(!subTitleMap.get(content.getmLanguageClass()).contains(content))
						subTitleMap.get(content.getmLanguageClass()).add(content);
				}
				
				content = new SubtitleContent();
				hasMetNbsp = false;
				getTimeStr(content, smiTokenizer.sval);
				content.setSubtitleIndex(indexCount++);
			}else if(tagType == SmiTokenizer.SMI_P){
				if(content != null){
				   content.setmLanguageClass(getClassName(smiTokenizer.sval));
				}
				LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SmiTokenizer.SMI_P ");
			}else if(tagType == SmiTokenizer.SMI_CSS){
				langList = getLangStr(smiTokenizer.sval);
				for(int i=0; i<langList.size(); i++){
					subTitleMap.put(langList.get(i), new ArrayList<SubtitleContent>());
				}
				LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SmiTokenizer.SMI_CSS ");
			}else if(tagType == SmiTokenizer.SMI_TEXT && content != null){
				if(subTitleLine == null)
					subTitleLine = SubContentUtil.removeHtmlTag(smiTokenizer.sval.toLowerCase().replaceAll("[\\r]?[\\n]", "\n").trim());
				else
					subTitleLine = subTitleLine + SubContentUtil.removeHtmlTag(smiTokenizer.sval.toLowerCase().replaceAll("[\\r]?[\\n]", "\n").trim());
				if("".equals(subTitleLine.trim())){
					hasMetNbsp = true;
					if(content.getmLanguageClass() != null){
						subTitleList = subTitleMap.get(content.getmLanguageClass());
					}
					
					if(subTitleList != null && subTitleList.size() != 0){
						subTitleList.get(subTitleList.size() - 1).setSubtitleEndTime(content.getSubtitleStartTime());
					}
					
					content = null;
				}else{
					if(preTagType == SmiTokenizer.SMI_BR){
						content.setSubtitleLine(content.getSubtitleLine() + subTitleLine);
					}else{
						content.setSubtitleLine(subTitleLine);
					}
					subTitleLine = null;
				}
				LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SmiTokenizer.SMI_TEXT ");
			}else if(tagType == SmiTokenizer.SMI_BR && content != null){
				content.setSubtitleLine(content.getSubtitleLine() + "<br>");
				LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SmiTokenizer.SMI_BR ");
			}else if(tagType == SmiTokenizer.SMI_BODY && content != null){
				if(content != null && content.getmLanguageClass() != null && subTitleMap.get(content.getmLanguageClass()) != null){
					if(hasMetNbsp == false && content.getSubtitleEndTime() == 0){
						content.setSubtitleEndTime(Integer.MAX_VALUE);
					}
					LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SmiTokenizer.SMI_BODY ");
					if(!subTitleMap.get(content.getmLanguageClass()).contains(content))
						subTitleMap.get(content.getmLanguageClass()).add(content);
				}
			}
			
			preTagType = tagType;
		}
		try {
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SubContentUtil.SortAllList(subTitleMap);
		LogUtil.Log(LogUtil.DEBUG_SUBTITLE,mTag,"decodeSubtitle<type smi> SubtitleMap.size()="+subTitleMap.size());
		return subTitleMap;
	}

}
