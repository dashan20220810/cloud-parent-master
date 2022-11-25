package com.qianyi.casinocore.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 公共常量类
 */
public class CommonConst {
    public static List cardLevel = new ArrayList();
    static {
        cardLevel.add(CommonConst.STRING_1);
        cardLevel.add(CommonConst.STRING_2);
        cardLevel.add(CommonConst.STRING_3);
        cardLevel.add(CommonConst.STRING_4);
    }
    /**
     * 连字符号("-")
     */
    public static final String HYPHEN = "-";
    /**
     * 连字符号("_")
     */
    public static final String UNDERLINE_SYMBOL = "_";

    /**
     * 空格连字符号(" - ")
     */
    public static final String BLANK_HYPHEN = " - ";

    /**
     * 左夸号("(")
     */
    public static final String LEFT_KUA = "(";

    /**
     * 右夸号(")")
     */
    public static final String RIGHT_KUA = ")";
    /**
     * 点分割符
     */
    public static final String POINT_SPLIT = ".";
    /**
     * 逗号分割符
     */
    public static final String COMMA_SPLIT = ",";
    /**
     * '|'分割符
     */
    public static final String VERTICAL_SPLIT = "|";
    /**
     * '|'分割符
     */
    public static final String VERTI_SPLIT = "[|]";
    /**
     * 空符号
     */
    public static final String BLANK_SYMBOL = "";
    /**
     * 空格字符号(" ")
     */
    public static final String SPACE_SPLIT = " ";
    /**
     * 冒号字符号(":")
     */
    public static final String COLON_SPLIT = ":";
    /**
     * 等于号字符号("=")
     */
    public static final String EQUALS_SPLIT = "=";

    public static final String AT_SPLIT="@";
    public static final int NUMBER_1 = 1;
    public static final int NUMBER_0 = 0;
    public static final int NUMBER_2 = 2;
    public static final int NUMBER_3 = 3;
    public static final int NUMBER_4 = 4;
    public static final int NUMBER_5 = 5;
    public static final int NUMBER_6 = 6;
    public static final int NUMBER_7 = 7;
    public static final int NUMBER_8 = 8;
    public static final int NUMBER_9 = 9;
    public static final int NUMBER_10 = 10;
    public static final int NUMBER_12 = 12;
    public static final int NUMBER_20 = 20;
    public static final int NUMBER_30 = 30;
    public static final int NUMBER_50 = 50;
    public static final int NUMBER_100 = 100;
    public static final int NUMBER_99999999 = 99999999;
    public static final String SUCCESS = "SUCCESS";
    public static final Long LONG_0 = 0L;
    public static final Long LONG_1 = 1L;
    public static final Long LONG_2 = 2L;
    public static final Long LONG_3 = 3L;
    public static final Float FLOAT_0 = 0F;
    public static final Float FLOAT_001 = 0.01F;
    public static final Float FLOAT_1 = 1F;
    public static final Float FLOAT_100 = 100F;

    public static final BigDecimal BIGDECIMAL_1 = new BigDecimal(NUMBER_1);

    public static final BigDecimal BIGDECIMAL_100 = new BigDecimal(NUMBER_100);

    public static final String IDNOTNULL = "无效id";

    public static final String PICTURENOTUP = "图片不符合规格";

    public static final String TOOMANYPICTURESONTHECLIENT = "当前客户端图片序号冲突";

    public static final String THENUMBERISLIMITEDTO20 = "数量限制为20条";

    public static final String NETWORK_ANOMALY = "网络异常";

    public static final String SECOND_FORMAT = "{0}%(总代)";

    public static final String THIRD_FORMAT = "{0}%(总代)-{1}%(区域)-{2}%(基层)";

    public static final String JICENG = "{0}%(基层)";

    public static final String STRING_0 = "0";
    public static final String STRING_1 = "1";
    public static final String STRING_2 = "2";
    public static final String STRING_3 = "3";
    public static final String STRING_4 = "4";


    public static final String REMARKS = "每万返";
    public static final String COMPANY = "元";


    public static final String REBATE_LEVEL_1 = "L1";
    public static final String REBATE_LEVEL_2 = "L2";
    public static final String REBATE_LEVEL_3 = "L3";
    public static final String REBATE_LEVEL_4 = "L4";
    public static final String REBATE_LEVEL_5 = "L5";
    public static final String REBATE_LEVEL_6 = "L6";
    public static final String REBATE_LEVEL_7 = "L7";
    public static final String REBATE_LEVEL_8 = "L8";
    public static final String REBATE_LEVEL = "未达到最低返佣级别";

}
