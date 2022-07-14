package com.tht.space.exception;

import com.tht.space.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageException extends RuntimeException {
    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Object... params) {
        super(StringUtils.formatter(message, params));
    }

    /**
     * 断言异常，条件为真抛出异常
     *
     * @param bool    条件
     * @param message 异常信息
     */
    public static void assertException(boolean bool, String message) {
        if (bool) throw new MessageException(message);
    }


    public static void assertException(boolean bool, String template, Object... params) {
        if (bool) throw new MessageException(StringUtils.formatter(template, params));
    }


}
