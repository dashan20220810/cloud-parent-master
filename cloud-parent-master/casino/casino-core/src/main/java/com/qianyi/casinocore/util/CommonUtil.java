package com.qianyi.casinocore.util;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class CommonUtil {

    private static final String SET = "set";
    private static final String METHOD_FORMAT = "{0}{1}";
    private static final String FIRSTPROXY = "firstProxy";
    private static final String SECONDPROXY = "secondProxy";
    private static final String THIRDPROXY = "thirdProxy";

    public static String getProxyCode(){

        Random rd = new Random();
        int rn= rd.nextInt(1) + Constants.MIN_PASSWORD_NUM;
        String n = "";
        int getNum;
        int getNum1;
        do {
            getNum = Math.abs(rd.nextInt()) % 10 + 48;// 产生数字0-9的随机数
            getNum1 = Math.abs(rd.nextInt()) % 26 + 97;//产生字母a-z的随机数
            char num1 = (char) getNum;
            char num2 = (char) getNum1;
            String dn = Character.toString(num1);
            String dn1 = Character.toString(num2);
            if(Math.random()>0.5){
                n += dn;
            }else{
                n += dn1;
            }
        } while (n.length() < rn);

        return n;
    }

    public static PageResultVO<?> handlePageResult(List<?> list, PageVo pageVO) {
        // 分页组装
        PageResultVO<?> pageResult = new PageResultVO<>();
        if (list == null || list.size() == 0) {
            pageResult.setContent(Collections.emptyList());
            pageResult.setTotalPages(0);
            pageResult.setTotalElements(0L);
            return pageResult;
        }
        int startIndex = (pageVO.getPageNo() - 1) * pageVO.getPageSize(); // 开始取值
        int endIndex = pageVO.getPageSize() + startIndex; // 结束取值
        List<?> pageList = null;
        pageResult.setTotalElements(Long.parseLong(list.size()+"")); // 总条数
        pageResult.setTotalPages(list.size() % pageVO.getPageSize() == 0 ? list.size() / pageVO.getPageSize() : list.size() / pageVO.getPageSize() + 1); // 总共多少页
        pageResult.setSize(pageVO.getPageSize());
        pageResult.setNumber(pageVO.getPageNo());
        try {
            pageList = list.subList(startIndex, endIndex);
            pageResult.setContent(pageList); // 分页显示的记录
            return pageResult;
        } catch (Exception e) {
            if (pageVO.getPageNo() > 1) {
                try {
                    pageList = list.subList(startIndex, list.size());
                    pageResult.setContent(pageList); // 分页显示的记录
                    return pageResult;
                } catch (Exception e1) {
                    PageResultVO<?> pageResultnew = new PageResultVO<>();
                    pageResultnew.setContent(Collections.emptyList());
                    return pageResultnew;
                }
            } else {
                pageList = list.subList(0, list.size());
                pageResult.setContent(pageList); // 分页显示的记录
                return pageResult;
            }
        }
    }
    public static Boolean setParameter(Object object, ProxyUser proxyUser){
        if (proxyUser == null){
            return true;
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
            return setMethod(object,FIRSTPROXY, proxyUser.getId());
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_2){
            return setMethod(object,SECONDPROXY, proxyUser.getId());
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_3){
            return setMethod(object,THIRDPROXY, proxyUser.getId());
        }else {
            return true;
        }
    }

    private static Boolean setMethod(Object object,String parameter,Long proxyRole){
        try {
            Method setMethod = object.getClass().getMethod(MessageFormat.format(METHOD_FORMAT, SET,
                com.qianyi.modulecommon.util.CommonUtil.toUpperCaseFirstOne(parameter)), Long.class);
            setMethod.invoke(object, proxyRole);
        } catch (Exception e) {
            log.error("反射生成对象异常",e);
            return true;
        }
        return false;
    }

    public static Long toHash(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(key.getBytes("utf-8"));
            BigInteger bigInt = new BigInteger(1, md5.digest());
            return Math.abs(bigInt.longValue());
        } catch (Exception ex) {
            return null;
        }

    }

    public static Map<Integer,String> findDates(String dateType, Date dBegin, Date dEnd){
        Map<Integer,String> mapDate = new HashMap<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        Integer count = CommonConst.NUMBER_0;
        while (calEnd.after(calBegin)) {
            count++;
            if (calEnd.after(calBegin))
                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            else
                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            switch (dateType) {
                case "M":
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);break;
                case "N":
                    calBegin.add(Calendar.SECOND, 1);break;
            }
        }
        return mapDate;
    }
}
