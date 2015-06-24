/**
 * 
 */
package com.kt.localmedia.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hm
 * Debug annotation, use to set debug flag for class
 */
@Retention(RetentionPolicy.RUNTIME)    
@Target({ElementType.TYPE})    
public @interface DebugAnno {
	boolean isEnableDebug() default false;
}
