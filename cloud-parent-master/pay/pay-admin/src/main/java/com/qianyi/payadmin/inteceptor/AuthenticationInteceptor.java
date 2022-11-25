package com.qianyi.payadmin.inteceptor;

import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.payadmin.util.PayUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Override
    protected boolean hasBan() {
        return false;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request, HttpServletResponse response) {
        String token = PayUtil.getToken();

        if(JjwtUtil.check(token, Constants.PAY_ADMIN)){
            return true;
        }
        return false;
    }

    @Override
    protected boolean multiDeviceCheck() {
        return true;
    }
}
