package com.qianyi.casinocore.util;

public final class TaskConst {
    /**
     * 每一分钟一次
     */
    public final static String ONLINE_USER_TASK = "0 0/1 * * * ? ";
    /**
     * 每三分钟一次
     */
    public final static String BACK_ORDER = "0 0/3 * * * ? ";

    /**
     * 每十分钟执行一次
     */
    public final static String BACK_GAME_ORDER = "0 0/10 * * * ? ";
    /**
     * 每日凌晨一点
     */
    public final static String HOME_PAGE_REPORT = "0 30 0 * * ?";
    /**
     * 12:00执行
     */
    public final static String HOME_PAGE_REPORT_NEW = "0 0 12 * * ?";
    /**
     * 每天12.20执行
     */
    public final static String GAME_RECORD_REPORT_TASK = "0 20 12 * * ?";
    /**
     * 每日零点五十分
     */
    public final static String PROXY_HOME_PAGE_REPORT = "0 50 0 * * ?";

    /**
     * 每日零点四十分
     */
    public final static String USER_RUNNING_WATER = "0 40 0 * * ?";

    /**
     * 每日零点四十分
     */
    public final static String GAMERECORD_TASK = "0 15,30,45 * * * ?";

    /**
     * 每小时十九分
     */
    public final static String GAMERECORD_TASK_NEW = "0 18 * * * ?";


    /**
     * 每12小时跑一次
     */
    public final static String TOTAL_PLATFORM_QUOTA_TASK = "0 0 0/12 * * ?";
}
