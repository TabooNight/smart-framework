package org.smart4j.framework.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串工具类
 */
public final class StringUtil {

    /**
     * 判断字符串是否为空
     *
     * @param str
     *              目标字符串
     * @return
     */
    public static boolean isEmpty(String str) {

        if (str != null) {
            str = str.trim();
        }
        return StringUtils.isEmpty(str);

    }

    /**
     * 判断字符串是否非空
     *
     * @param str
     *              目标字符串
     * @return
     */
    public static boolean isNotEmpty(String str) {

        return !isEmpty(str);

    }

    /**
     * 分割字符串
     *
     * @param str
     *                  待分割字符串
     * @param splitStr
     *                  分隔符
     * @return
     */
    public static String[] splitString(String str, String splitStr) {

        if (isNotEmpty(str)) {
            return str.split(splitStr);
        }
        return null;

    }

}
