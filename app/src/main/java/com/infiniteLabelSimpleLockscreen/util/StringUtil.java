package com.infiniteLabelSimpleLockscreen.util;

/**
 * 密码内容操作
 * @author Crazy24k@gmail.com
 * 
 */
public class StringUtil {
	/**
	 * 是否不为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNotEmpty(String s) {
		return s != null && !"".equals(s.trim());
	}

	/**
	 * 是否为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		return s == null || "".equals(s.trim());
	}
}
