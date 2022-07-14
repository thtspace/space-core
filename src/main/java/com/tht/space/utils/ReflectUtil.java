package com.tht.space.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
public class ReflectUtil {
    private static final String SETTER_PREFIX = "set";

    private static final String GETTER_PREFIX = "get";

    public static <E> E invokeMethod(Object obj, String methodName, Class[] argsClass, Object[] args) throws Exception {
        if (obj == null || StringUtils.isBlank(methodName)) {
            return null;
        }
        Method method = getAccessibleMethod(obj, methodName, argsClass);
        if (method == null) {
            log.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + methodName + "] 方法 ");
            return null;
        }
        return (E) method.invoke(obj, args);
    }

    public static <E> E invokeGetter(final Object obj, final String propertyName) throws Exception {
        if (!ObjectUtils.allNotNull(obj, propertyName)) {
            return null;
        }
        Object object = obj;
        for (String name : StringUtils.split(propertyName, ".")) {
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            object = invokeMethod(object, getterMethodName, new Class[]{}, new Object[]{});
        }
        return (E) object;
    }

    public static <E> void invokeSetter(final Object obj, final String propertyName, final E value) throws Exception {
        Object object = obj;
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++) {
            if (i < names.length - 1) {
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(propertyName);
                object = invokeMethod(obj, getterMethodName, new Class[]{}, new Object[]{});
                if (object == null) {
                    Field field = obj.getClass().getDeclaredField(names[i]);
                    object = field.getType().newInstance();
                    String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
                    invokeMethod(obj, setterMethodName, new Class[]{object.getClass()}, new Object[]{object});
                }
            } else {
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
                invokeMethod(object, setterMethodName, new Class[]{value.getClass()}, new Object[]{value});
            }
        }
    }

    public static Method getAccessibleMethod(final Object obj, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> clazz = obj.getClass();
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        return method;
    }

    public static Method getGetterMethod(final Object obj, final String propertyName) throws NoSuchMethodException {
        if (obj == null || StringUtils.isBlank(propertyName)) {
            return null;
        }
        String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(propertyName);
        return getAccessibleMethod(obj, getterMethodName);
    }

    public static Method getSetterMethod(final Object obj, final String propertyName) throws NoSuchMethodException {
        if (obj == null || StringUtils.isBlank(propertyName)) {
            return null;
        }
        String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
        return getAccessibleMethod(obj, setterMethodName);
    }

}
