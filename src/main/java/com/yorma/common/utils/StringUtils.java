package com.yorma.common.utils;

public class StringUtils {

	// Performance testing notes (JDK 1.4, Jul03, scolebourne)
	// Whitespace:
	// Character.isWhitespace() is faster than WHITESPACE.indexOf()
	// where WHITESPACE is a string of all whitespace characters
	//
	// Character access:
	// String.charAt(n) versus toCharArray(), then array[n]
	// String.charAt(n) is about 15% worse for a 10K string
	// They are about equal for a length 50 string
	// String.charAt(n) is about 4 times better for a length 3 string
	// String.charAt(n) is best bet overall
	//
	// Append:
	// String.concat about twice as fast as StringBuffer.append
	// (not sure who tested this)

	/**
	 * The empty String <code>""</code>.
	 * 
	 * @since 2.0
	 */
	public static final String EMPTY = "";

	/**
	 * <p>
	 * Joins the elements of the provided array into a single String containing the
	 * provided list of elements.
	 * </p>
	 *
	 * <p>
	 * No delimiter is added before or after the list. A <code>null</code> separator
	 * is the same as an empty String (""). Null objects or empty strings within the
	 * array are represented by empty strings.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.join(null, *)                = null
	 * StringUtils.join([], *)                  = ""
	 * StringUtils.join([null], *)              = ""
	 * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
	 * StringUtils.join(["a", "b", "c"], null)  = "abc"
	 * StringUtils.join(["a", "b", "c"], "")    = "abc"
	 * StringUtils.join([null, "", "a"], ',')   = ",,a"
	 * </pre>
	 *
	 * @param array
	 *            the array of values to join together, may be null
	 * @param separator
	 *            the separator character to use, null treated as ""
	 * @return the joined String, <code>null</code> if null array input
	 */
	public static String join(Object[] array, String separator) {
		if (array == null) {
			return null;
		}
		return join(array, separator, 0, array.length);
	}

	/**
	 * <p>
	 * Joins the elements of the provided array into a single String containing the
	 * provided list of elements.
	 * </p>
	 *
	 * <p>
	 * No delimiter is added before or after the list. A <code>null</code> separator
	 * is the same as an empty String (""). Null objects or empty strings within the
	 * array are represented by empty strings.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.join(null, *)                = null
	 * StringUtils.join([], *)                  = ""
	 * StringUtils.join([null], *)              = ""
	 * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
	 * StringUtils.join(["a", "b", "c"], null)  = "abc"
	 * StringUtils.join(["a", "b", "c"], "")    = "abc"
	 * StringUtils.join([null, "", "a"], ',')   = ",,a"
	 * </pre>
	 *
	 * @param array
	 *            the array of values to join together, may be null
	 * @param separator
	 *            the separator character to use, null treated as ""
	 * @param startIndex
	 *            the first index to start joining from. It is an error to pass in
	 *            an end index past the end of the array
	 * @param endIndex
	 *            the index to stop joining from (exclusive). It is an error to pass
	 *            in an end index past the end of the array
	 * @return the joined String, <code>null</code> if null array input
	 */
	public static String join(Object[] array, String separator, int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		if (separator == null) {
			separator = EMPTY;
		}

		// endIndex - startIndex > 0: Len = NofStrings *(len(firstString) +
		// len(separator))
		// (Assuming that all Strings are roughly equally long)
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return EMPTY;
		}

		bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator.length());

		StringBuffer buf = new StringBuffer(bufSize);

		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * Checks if a String is not empty (""), not null and not whitespace only.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isNotBlank(null)      = false
	 * StringUtils.isNotBlank("")        = false
	 * StringUtils.isNotBlank(" ")       = false
	 * StringUtils.isNotBlank("bob")     = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null and not
	 *         whitespace
	 * @since 2.0
	 */
	public static boolean isNotBlank(String str) {
		return !StringUtils.isBlank(str);
	}

	/**
	 * <p>
	 * Checks if a String is whitespace, empty ("") or null.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 去掉小数点后面不需要的0
	 * 
	 * @param str
	 *            需要去除的字符串
	 * @return 去除后的字符串
	 */
	public static String removeDecimalPoint(String str) {
		if (str.indexOf(".") > 0) {
			// 正则表达
			str = str.replaceAll("0+?$", "");// 去掉后面无用的零
			str = str.replaceAll("[.]$", "");// 如小数点后面全是零则去掉小数点
		}
		return str;
	}
	/**
	 * 截取字符串str中指定字符 strStart、strEnd之间的字符串
	 *
	 * @param string
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String subString(String str, String strStart, String strEnd) {

        /* 找出指定的2个字符在 该字符串里面的 位置 */
		int strStartIndex = str.indexOf(strStart);
		int strEndIndex = str.indexOf(strEnd);

        /* index 为负数 即表示该字符串中 没有该字符 */
		if (strStartIndex < 0) {
			return "字符串 :---->" + str + "<---- 中不存在 " + strStart + ", 无法截取目标字符串";
		}
		if (strEndIndex < 0) {
			return "字符串 :---->" + str + "<---- 中不存在 " + strEnd + ", 无法截取目标字符串";
		}
        /* 开始截取 */
		String result = str.substring(strStartIndex, strEndIndex).substring(strStart.length());
		return result;
	}
}
