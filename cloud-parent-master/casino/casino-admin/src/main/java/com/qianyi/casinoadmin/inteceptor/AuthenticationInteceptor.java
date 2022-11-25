package com.qianyi.casinoadmin.inteceptor;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {


    @Autowired
    SysUserService sysUserService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    protected boolean hasBan() {
        Long authId=LoginUtil.getLoginUserId();
        if(authId == null){
            return true;
        }
        SysUser user=sysUserService.findById(authId);
        boolean flag= SysUser.checkUser(user);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request, HttpServletResponse response) {
        String token = LoginUtil.getToken();

        if(JjwtUtil.check(token, "casino-admin")){
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
        SysUser user = sysUserService.findById(authId);
        synchronized (token.intern()) {
            //多个请求只有一个去刷新token
            Object redisToken = redisUtil.get(Constants.REDIS_TOKEN_ADMIN + authId);
            JjwtUtil.Token redisJwtToken = null;
            if (redisToken != null) {
                redisJwtToken = (JjwtUtil.Token) redisToken;
            }
            //判断其他请求是否已经获取到新token
            if (redisJwtToken != null && token.equals(redisJwtToken.getOldToken()) && !ObjectUtils.isEmpty(redisJwtToken.getNewToken())) {
                return redisJwtToken;
            }
            //获取新token
            String refreshToken = JjwtUtil.refreshToken(token, user.getPassWord(), Constants.ADMIN_REFRESH_TTL, Constants.CASINO_ADMIN);
            if (ObjectUtils.isEmpty(refreshToken)) {
                return null;
            }
            //获取到新token后会把之前的token设置成旧的，用于判断后面其他带旧token的请求，有一个旧token获取到新token，其他直接从redis取新的
            JjwtUtil.Token jwtTiken = new JjwtUtil.Token();
            jwtTiken.setOldToken(token);
            jwtTiken.setNewToken(refreshToken);
            //不是最新的token也可以获取到新token，但是多设备校验的时候会拦截
            if (redisJwtToken != null && (token.equals(redisJwtToken.getOldToken()) || token.equals(redisJwtToken.getNewToken()))) {
                redisUtil.set(Constants.REDIS_TOKEN_ADMIN + authId, jwtTiken, Constants.ADMIN_REFRESH_TTL);
            } else {
                log.error("当前token={}，iss={} 已失效刷新token无效，redis中token信息为={}", token, Constants.CASINO_ADMIN, redisJwtToken);
            }
            return jwtTiken;
        }
    }

    @Override
    protected boolean multiDeviceCheck() {
        Long authId = LoginUtil.getLoginUserId();
        String token = LoginUtil.getToken();
        String key = Constants.REDIS_TOKEN_ADMIN + "admin:"+ authId;
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
}
