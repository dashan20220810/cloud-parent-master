package com.qianyi.casinoreport.util;

public class ShareProfitUtils {

    public static boolean compareIntegerNotNull(Long uid){
        if(uid != null){
            return uid!=0;
        }
        return false;
    }
}
