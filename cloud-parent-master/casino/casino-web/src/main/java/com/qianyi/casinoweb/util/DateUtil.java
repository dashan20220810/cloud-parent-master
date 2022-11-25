package com.qianyi.casinoweb.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    /**
     * 时间戳转换成日期格式字符串
     * @param mills 精确到毫秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(Long mills,String format) {
        if(format == null || format.isEmpty()){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(mills);
    }

    public static long next5MinuteTime(){
        long now = System.currentTimeMillis();
        return (now - now % (1000*60*5))/1000;
//        return (now - now % (1000*60*5) + (100*60*5))/1000;

    }
}
