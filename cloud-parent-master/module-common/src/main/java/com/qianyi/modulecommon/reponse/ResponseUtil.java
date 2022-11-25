package com.qianyi.modulecommon.reponse;

import com.qianyi.modulecommon.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {

    private static MessageUtil messageUtil;

    @Autowired
    public void setMessageUtil(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }


    public static ResponseEntity error() {
        return new ResponseEntity(ResponseCode.ERROR);
    }

    public static ResponseEntity success() {
        return new ResponseEntity(ResponseCode.SUCCESS);
    }

    public static ResponseEntity success(Object data) {
        return new ResponseEntity(ResponseCode.SUCCESS, data);
    }

    public static ResponseEntity success(String msg, Object data) {
        return new ResponseEntity(ResponseCode.SUCCESS,messageUtil.get(msg) + " " + data);
    }

    public static ResponseEntity authenticationNopass() {
        return new ResponseEntity(ResponseCode.AUTHENTICATION_NOPASS,messageUtil.get(ResponseCode.AUTHENTICATION_NOPASS.getMsg()));
    }

    public static ResponseEntity authorizationNopass() {
        return new ResponseEntity(ResponseCode.AUTHORIZATION_NOPASS,messageUtil.get(ResponseCode.AUTHORIZATION_NOPASS.getMsg()));
    }

    public static ResponseEntity requestLimit() {
        return new ResponseEntity(ResponseCode.REQUEST_LIMIT,messageUtil.get(ResponseCode.REQUEST_LIMIT.getMsg()));
    }

    public static ResponseEntity parameterNotNull() {
        return new ResponseEntity(ResponseCode.PARAMETER_NOTNULLL,messageUtil.get(ResponseCode.PARAMETER_NOTNULLL.getMsg()));
    }

    public static ResponseEntity custom(String msg) {
        return new ResponseEntity(messageUtil.get(msg));
    }

    public static ResponseEntity custom(String msg,Object data) {
        String translateMsg = messageUtil.get(msg) + " " + data;
        return new ResponseEntity(translateMsg);
    }

    public static ResponseEntity customBefore(Object data, String msg) {
        String translateMsg = data + " " + messageUtil.get(msg);
        return new ResponseEntity(translateMsg);
    }

    public static ResponseEntity risk() {
        return new ResponseEntity(ResponseCode.RISK,messageUtil.get(ResponseCode.RISK.getMsg()));

    }

    public static ResponseEntity googleAuthNoPass() {
        return new ResponseEntity(ResponseCode.GOOGLEAUTH_NOPASS,messageUtil.get(ResponseCode.GOOGLEAUTH_NOPASS.getMsg()));
    }

    public static ResponseEntity fail() {
        return new ResponseEntity(ResponseCode.FAIL);
    }

    public static ResponseEntity emptytWithdrawMoney() {
        return new ResponseEntity(ResponseCode.EMPTY_TWITHDRAWMONEY,messageUtil.get(ResponseCode.EMPTY_TWITHDRAWMONEY.getMsg()));
    }

    public static ResponseEntity multiDevice() {
        return new ResponseEntity(ResponseCode.MULTIDEVICE,messageUtil.get(ResponseCode.MULTIDEVICE.getMsg()));
    }

    public static ResponseEntity platformMaintain() {
        return new ResponseEntity(ResponseCode.PLATFORM_MAINTAIN,messageUtil.get(ResponseCode.PLATFORM_MAINTAIN.getMsg()));
    }
}
