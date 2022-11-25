package com.qianyi.casinocore.util;

import java.util.Random;

/**
 * 邀请码生成器，算法原理：
 */
public class InviteCodeUtil {

    /**
     * 邀请码组合 - 数字、大写字母 - 常量
     * 不包换：0,O,1,I
     */
    private static final String RULE_INVITATION_CODE = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int RULE_INVITATION_CODE_LENGTH = 32;

    /**
     * 邀请码长度 - 常量
     */
    private static final int CODE_LENGTH_5 = 5;
    private static final int CODE_LENGTH_6 = 6;

    public static String randomCode(int length) {
        return randomCodeLength(length);
    }

    public static String randomCode6() {
        return randomCodeLength(CODE_LENGTH_6);
    }

    /**
     * 随机产生指定位数的随机字符串
     */
    private static String randomCodeLength(int length) {
        Random random = new Random();
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int i1 = random.nextInt(RULE_INVITATION_CODE_LENGTH);
            char c = RULE_INVITATION_CODE.charAt(i1);
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    /**
     * 随机产生指定位数的随机数
     */
    public static String randomCodeInt5() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH_5; i++) {
            stringBuilder.append(new Random().nextInt(10));
        }
        return stringBuilder.toString();
    }

    public static String randomNumCode(int length) {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }
}
