package com.tht.space.utils;

/**
 * @program: space-core
 * @author: tht
 * @create: 2022-06-06 16:12
 * @description:
 **/
public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

    public static boolean isNull(Object data) {
        return data == null;
    }

    public static boolean isNotNull(Object data) {
        return !isNull(data);
    }
}
