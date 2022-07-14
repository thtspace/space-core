package com.tht.space.utils;

import com.tht.space.exception.MessageException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @program: hz-service
 * @author: tan haitao
 * @create: 2021-10-22 09:36
 * @description: 枚举工具类
 **/
public class EnumUtils extends org.apache.commons.lang3.EnumUtils {
    public static Object getFieldValue(Enum<?> e, String fieldName){
        try {
            Field field = e.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(e);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <E> List<E> getValues(Class<E> enumClass, String attr){
        Object[] es = enumClass.getEnumConstants();
        List<E> values = new ArrayList<>(es.length);
        for (Object e : es) {
            String methodName = "get" + StringUtils.capitalize(attr);
            try {
                Method method = e.getClass().getMethod(methodName);
                Object obj = method.invoke(e);
                values.add((E) obj);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        return values;
    }
}
