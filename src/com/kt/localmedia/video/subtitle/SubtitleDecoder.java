/**
 * 
 */
package com.kt.localmedia.video.subtitle;

import java.util.List;
import java.util.Map;

/**
 * @author hm
 *
 */
public abstract class SubtitleDecoder {
	public abstract Map<String, List<SubtitleContent>> decodeSubtitle(String subTitlePath,String encode) throws Exception;
}
