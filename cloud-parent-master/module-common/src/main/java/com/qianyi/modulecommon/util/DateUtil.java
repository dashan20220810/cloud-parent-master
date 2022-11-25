package com.qianyi.modulecommon.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    private static String patten = "yyyy-MM-dd HH:mm:ss";

    private static String patten1 = "yyyy-MM-dd";

    private static String patten2 = "yyyy-MM-dd HH";

    private static String pattenMonth = "yyyy-MM";

    private static String pattenYear = "yyyy";

    public static final String YYYYMM = "yyyyMM";

    public static final String YYYYMMDD = "yyyyMMdd";

    public static final String format = "HH:mm:ss";

    public static final String YYYYMMDD_HHmmSS = "yyyyMMdd%20HH%3Amm%3ASS";

    public static  Date startTime;

    public static  Date endTime;

    public static SimpleDateFormat getSimpleDateFormat(String patten) {
        return new SimpleDateFormat(patten);
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return getSimpleDateFormat(patten);
    }

    public static SimpleDateFormat getSimpleDateFormat1() {
        return getSimpleDateFormat(patten1);
    }

    public static SimpleDateFormat getSimpleDateFormat2() {
        return getSimpleDateFormat(patten2);
    }

    public static SimpleDateFormat getSimpleDateFormatMonth() {
        return getSimpleDateFormat(pattenMonth);
    }

    public static SimpleDateFormat getSimpleDateFormatYear() {
        return getSimpleDateFormat(pattenYear);
    }

    public static String today(String patten) {
        return getSimpleDateFormat(patten).format(new Date());
    }

    public static String today() {
        return today(patten);
    }

    static{
        try {
            startTime = new SimpleDateFormat(format).parse("00:00:00");
            endTime = new SimpleDateFormat(format).parse("01:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    public static Integer getWeek(String today){
        SimpleDateFormat format = new SimpleDateFormat(patten1);
        Date date = null;
        try {
            date = format.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
    /**
     * 零点到一点不能修改代理返佣相关配置
     * @return
     */
    public static boolean verifyTime(){
        String string = DateUtil.dateToHHmmss(new Date());
        try {
            Date nowTime = new SimpleDateFormat(DateUtil.format).parse(string);
            boolean effectiveDate = DateUtil.isEffectiveDate(nowTime, DateUtil.startTime, DateUtil.endTime);
            if (effectiveDate){
                return true;
            }
            return false;
        } catch (ParseException e) {
            return true;
        }
    }
    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     * @param nowTime
     @param startTime
     @param endTime
      * @return
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
            || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }
    public static String dateToHHmmss(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.format); //定义将日期格式要换成的格式
        return formatter.format(time);
    }

    public static String dateToyyyyMMdd(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.YYYYMMDD); //定义将日期格式要换成的格式
        return formatter.format(time);
    }

    public static String dateToPatten(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.patten); //定义将日期格式要换成的格式
        return formatter.format(time);
    }

    public static String dateToPatten1(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.patten1); //定义将日期格式要换成的格式
        return formatter.format(time);
    }

    public static String dateToPatten2(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.patten2); //定义将日期格式要换成的格式
        return formatter.format(time);
    }

    public static String dateToYYYYMM(Date date) {
        SimpleDateFormat sf = new SimpleDateFormat(DateUtil.YYYYMM);
        return sf.format(date);
    }

    /**
     * 获取那一天的开始时间 num=0表示当天 -1表示前一天
     * @param num
     * @return
     */
    public static String getStartTime(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,num);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String time = getSimpleDateFormat().format(calendar.getTime());
        return time;
    }

    /**
     * 获取那一天的结束时间 num=0表示当天 -1表示前一天
     * @param num
     * @return
     */
    public static String getEndTime(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,num);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        String time = getSimpleDateFormat().format(calendar.getTime());
        return time;
    }

    /**
     * 计算两个日期之间相差的天数
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate,Date bdate) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        smdate=sdf.parse(sdf.format(smdate));
        bdate=sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }


    /**
     * 获取前几个月的开始时间
     * @param num
     * @return
     */
    public static String getMonthAgoStartTime(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, num);//得到前一个月
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String time = getSimpleDateFormat().format(calendar.getTime());
        return time;
    }

    // 获得本周一日期
    public static Date getWeekStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONDAY), calendar.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTime();
    }

    // 获得本周日日期
    public static Date getWeekEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getWeekStartDate());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        return calendar.getTime();
    }
}
