package com.tht.space.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

public class ServletUtil {

    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    public static HttpServletRequest getRequest() {
        return Objects.requireNonNull(getRequestAttributes()).getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse() {
        return Objects.requireNonNull(getRequestAttributes()).getResponse();
    }

    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }



    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    public static Integer getParameterToInt(String name) {
        String parameter = getRequest().getParameter(name);
        return StringUtils.isNotBlank(parameter) ? Integer.parseInt(parameter) : null;
    }

    public static String getHeader(String name) {
        return getRequest().getHeader(name);
    }
}
