/**
 * 
 */
package com.kt.localmedia.video.subtitle;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author hm
 *
 */
public class AliasUtil {

	public static Map<String, String> aliasMap = new TreeMap<String, String>(); 
	static{
		aliasMap.put("windows-1252", "Unicode");
//		aliasMap.put("windows-1252", "GB2312");
		aliasMap.put("GB18030", "GBK");
	}
	
	public static String getAlias(String orgName){
		if(!aliasMap.containsKey(orgName))
			return orgName;
		
		return aliasMap.get(orgName);
	}
}
