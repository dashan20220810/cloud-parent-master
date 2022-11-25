package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Autowired
    UserService userService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    PlatformConfigService platformConfigService;

    @Override
    protected boolean hasBan() {
       Long authId=CasinoWebUtil.getAuthId();
        User user=userService.findById(authId);
        boolean flag= User.checkUser(user);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request, HttpServletResponse response) {
        String token = CasinoWebUtil.getToken();
        if (JjwtUtil.check(token, Constants.CASINO_WEB)) {
            return true;
        }
        //token过期获取新token
        JjwtUtil.Token refreshJwtToken = refreshJwtToken(token);
        if (refreshJwtToken == null) {
            return false;
        }
        String newToken = refreshJwtToken.getNewToken();
        response.setHeader(Constants.AUTHORIZATION, newToken);
        return true;
    }

    /**
     * 通过旧token获取新token
     * @param token
     * @return
     */
    public JjwtUtil.Token refreshJwtToken(String token) {
        boolean checkNull = CommonUtil.checkNull(token);
        if (checkNull) {
            return null;
        }
        JjwtUtil.Subject subject = JjwtUtil.getSubject(token);
        if (subject == null || ObjectUtils.isEmpty(subject.getUserId())) {
            return null;
        }
        //获取登陆用户
        Long authId = Long.parseLong(subject.getUserId());
        User user = userService.findById(authId);
        synchronized (token.intern()) {
            //多个请求只有一个去刷新token
            Object redisToken = redisUtil.get(Constants.TOKEN_CASINO_WEB + authId);
            JjwtUtil.Token redisJwtToken = null;
            if (redisToken != null) {
                redisJwtToken = (JjwtUtil.Token) redisToken;
            }
            //判断其他请求是否已经获取到新token
            if (redisJwtToken != null && token.equals(redisJwtToken.getOldToken()) && !ObjectUtils.isEmpty(redisJwtToken.getNewToken())) {
                return redisJwtToken;
            }
            //获取新token
            String refreshToken = JjwtUtil.refreshToken(token, user.getPassword(), Constants.WEB_REFRESH_TTL, Constants.CASINO_WEB);
            if (ObjectUtils.isEmpty(refreshToken)) {
                return null;
            }
            //获取到新token后会把之前的token设置成旧的，用于判断后面其他带旧token的请求，有一个旧token获取到新token，其他直接从redis取新的
            JjwtUtil.Token jwtTiken = new JjwtUtil.Token();
            jwtTiken.setOldToken(token);
            jwtTiken.setNewToken(refreshToken);
            //不是最新的token也可以获取到新token，但是多设备校验的时候会拦截
            if (redisJwtToken != null && (token.equals(redisJwtToken.getOldToken()) || token.equals(redisJwtToken.getNewToken()))) {
                redisUtil.set(Constants.TOKEN_CASINO_WEB + authId, jwtTiken, Constants.WEB_REFRESH_TTL);
            } else {
                log.error("当前token={}，iss={} 已失效刷新token无效，redis中token信息为={}", token, Constants.CASINO_WEB, redisJwtToken);
            }
            return jwtTiken;
        }
    }


    /**
     * 多设备登录校验，后面登录的会踢掉前面登录的
     * @return
     */
    @Override
    protected boolean multiDeviceCheck() {
        Long authId = CasinoWebUtil.getAuthId();
        String token = CasinoWebUtil.getToken();
        String key = Constants.TOKEN_CASINO_WEB + authId;
        Object redisToken = redisUtil.get(key);
        if (ObjectUtils.isEmpty(redisToken)) {
            return true;
        }
        JjwtUtil.Token redisToken1 = (JjwtUtil.Token) redisToken;
        //新旧token有一个匹配得上说明就是最新token
        if (token.equals(redisToken1.getOldToken())||token.equals(redisToken1.getNewToken())) {
            return true;
        }
        return false;
    }

    /**
     * 平台维护开关校验
     * @return
     */
    @Override
    protected PlatformMaintenanceSwitch platformMaintainCheck() {
        PlatformMaintenanceSwitch vo = new PlatformMaintenanceSwitch();
        try {
            PlatformConfig platformConfig = platformConfigService.findFirst();
            if (platformConfig == null || platformConfig.getMaintenanceStart() == null || platformConfig.getMaintenanceEnd() == null) {
                vo.setOnOff(false);
                return vo;
            }
            Integer maintenance = platformConfig.getPlatformMaintenance();
            boolean switchb = maintenance == Constants.open ? true : false;
            //先判断开关是否是维护状态，在判断当前时间是否在维护时区间内
            if (switchb) {
                switchb = DateUtil.isEffectiveDate(new Date(), platformConfig.getMaintenanceStart(), platformConfig.getMaintenanceEnd());
            }
            vo.setOnOff(switchb);
            //最后确定状态
            if (switchb) {
                SimpleDateFormat sd = DateUtil.getSimpleDateFormat();
                if (platformConfig.getMaintenanceStart() != null) {
                    vo.setStartTime(sd.format(platformConfig.getMaintenanceStart()));
                }
                if (platformConfig.getMaintenanceEnd() != null) {
                    vo.setEndTime(sd.format(platformConfig.getMaintenanceEnd()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("检查KK平台维护时报错，msg={}", e.getMessage());
        }
        return vo;
    }
}
