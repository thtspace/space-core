package com.tht.space.utils;

import org.apache.commons.lang3.ObjectUtils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     * 空字符串
     */
    private static final String NULLSTR = "";

    /**
     * 下划线
     */
    private static final char SEPARATOR = '_';

    /**
     * 是否全部不为空
     *
     * @param css 字符串列表
     */
    public boolean isAllNotBlank(CharSequence... css) {
        for (CharSequence c : css) {
            if (isBlank(c)) {
                return false;
            }
        }
        return true;
    }

    //占位符
    private static final String PLACEHOLDER = "{}";

    /**
     * 将字符串格式化，例如：
     * template: 'name:{},age:{}',params:["张三",18]  =>  'name:张三,age:18'
     *
     * @param template 模板
     * @param params   参数
     * @return 格式化后的字符串
     */
    public static String formatter(String template, Object... params) {
        if (!ObjectUtils.allNotNull(template, params)) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template.length() + 50);
        int templateLength = template.length();
        int handledPosition = 0;
        int index;
        for (Object param : params) {
            index = template.indexOf(PLACEHOLDER, handledPosition);
            if (index == -1) {
                break;
            } else {
                sb.append(template, handledPosition, index);
                sb.append(param.toString());
                handledPosition = index + 2;
            }
        }
        sb.append(template, handledPosition, templateLength);
        return sb.toString();
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    public static String defaultValue(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    public static String toStr(Object value) {
        return toStr(value, null);
    }

    public static String toStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

}
