package com.qianyi.casinoweb.util;

/**
 * 判断请求设备是不是移动端
 */
public class DeviceUtil {
    private final static String[] agent = { "Android", "iPhone", "iPod","iPad", "Windows Phone", "MQQBrowser" }; //定义移动端请求的所有可能类型

    /**
     * 判断User-Agent 是不是来自于手机
     * @param userAgent
     * @return
     */
    public static boolean checkAgentIsMobile(String userAgent) {
        boolean flag = false;
        if (!userAgent.contains("Windows NT") || (userAgent.contains("Windows NT") && userAgent.contains("compatible; MSIE 9.0;"))) {
            // 排除 苹果桌面系统
            if (!userAgent.contains("Windows NT") && !userAgent.contains("Macintosh")) {
                for (String item : agent) {
                    if (userAgent.contains(item)) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }
}
