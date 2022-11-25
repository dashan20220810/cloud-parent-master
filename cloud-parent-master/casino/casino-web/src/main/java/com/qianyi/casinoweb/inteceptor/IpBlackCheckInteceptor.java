package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinocore.model.IpBlack;
import com.qianyi.casinocore.service.IpBlackService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractIpBlackCheckInteceptor;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class IpBlackCheckInteceptor extends AbstractIpBlackCheckInteceptor {

    @Autowired
    private IpBlackService ipBlackService;

    @Override
    protected String ipBlackCheck(HttpServletRequest request) {
        String ip = IpUtil.getIp(request);
        IpBlack ipBlack = ipBlackService.findByIp(ip);
        if (ipBlack != null && !Constants.yes.equals(ipBlack.getStatus())) {
            return ipBlack.getRemark();
        }
        return null;
    }
}
