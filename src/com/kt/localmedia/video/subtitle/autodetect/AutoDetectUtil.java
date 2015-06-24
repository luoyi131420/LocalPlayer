/**
 * 
 */
package com.kt.localmedia.video.subtitle.autodetect;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * @author hm
 *
 */
public class AutoDetectUtil {
	static String result = null;
	/**
	 * 1. Japanese
	 * 2. Chinese
     * 3. Simplified Chinese
     * 4. Traditional Chinese
     * 5. Korean
     * 6. Dont know (默认)
	 * @param maybeLanguage
	 * @return
	 */
	public static String detectEncode(int maybeLanguage, BufferedInputStream imp){
		result = null;
		nsICharsetDetectionObserver cdo = new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				HtmlCharsetDetector.found = true;
				result = charset;
			}
		};
		
		nsDetector det = new nsDetector(maybeLanguage);
		det.Init(cdo);
		byte[] buf = new byte[1024] ;
		boolean done = false; 
		int len = -1;
		boolean isAscii = true;
		boolean found = false;
		int readcount = 0;
		try {
			while((len=imp.read(buf,0,buf.length)) != -1) {
				if (isAscii) isAscii = det.isAscii(buf,len);
				if (!isAscii && !done) done = det.DoIt(buf,len, false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		det.DataEnd();
		
		if(HtmlCharsetDetector.found && result != null){
			return result;
		}
		
		if (isAscii) {
			found = true;
			return "ascii";
		}
		
		if (!found) {
			String prob[] = det.getProbableCharsets() ;
			return prob[0];
		}
		
		return null;
	}
}
