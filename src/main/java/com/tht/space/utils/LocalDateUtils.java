package com.tht.space.utils;

import com.tht.space.exception.MessageException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: hz-service
 * @author: tan haitao
 * @create: 2022-01-07 14:18
 * @description:
 **/
public class LocalDateUtils {
    public static LocalDate toLocalDate(String date){
        if (StringUtils.isBlank(date)){
            return null;
        }
        return toLocalDate(toDate(date));
    }

    public static LocalDateTime toLocalDateTime(String date){
        if (StringUtils.isBlank(date)){
            return null;
        }
        return toLocalDateTime(toDate(date));
    }

    public static LocalDate toLocalDate(Date date){
        if (ObjectUtils.isEmpty(date)){
            return null;
        }
        return toLocalDateTime(date).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date){
        if (ObjectUtils.isEmpty(date)){
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }



    public static Date toDate(String str) {
        if (StringUtils.isBlank(str)){
            return null;
        }
        str = str.trim();
        String pattern = "\\d{4}([^\\d]?)\\d{1,2}\\1\\d{1,2}( \\d{1,2}([^\\d])\\d{1,2})?";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        String dateSplit, timeSplit;
        if (m.find()) {
            dateSplit = m.group(1);
            timeSplit = m.group(3);
            String formatStr = String.format("yyyy%sMM%sdd", dateSplit, dateSplit);
            if (timeSplit != null) {
                String timeStr = str.substring(str.indexOf(" "));
                String[] split = timeStr.split(timeSplit);
                if (split.length == 2) {
                    formatStr += String.format(" HH%smm", timeSplit);
                }
                if (split.length > 2) {
                    formatStr += String.format(" HH%smm%sss", timeSplit, timeSplit);
                }
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
            try {
                return simpleDateFormat.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new MessageException("输入的时间格式不符");
        }
    }
}
