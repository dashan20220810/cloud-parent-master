package com.qianyi.casinoadmin.config.security.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    /**
     * 接口url
     */
    public static Map<String,String> URL_MAPPING_MAP = new HashMap<>();

    /**
     *  获取项目根目录
     */
    public static String PROJECT_ROOT_DIRECTORY = System.getProperty("user.dir");

    /**
     * 密码加密相关
     */
    public static String SALT = "zhengqing";
    public static final int HASH_ITERATIONS = 1;

    /**
     * 请求头 - token
     */
//    public static final String REQUEST_HEADER = "X-Token";
    public static final String REQUEST_HEADER = "authorization";

    /**
     * 请求头类型：
     * application/x-www-form-urlencoded ： form表单格式
     * application/json ： json格式
     */
    public static final String REQUEST_HEADERS_CONTENT_TYPE = "application/json";

    /**
     * 登录者角色
     */
    public static final String ROLE_LOGIN = "role_login";
}
