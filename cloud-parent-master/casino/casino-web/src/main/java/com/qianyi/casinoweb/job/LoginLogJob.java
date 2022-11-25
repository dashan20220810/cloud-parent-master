package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.LoginLogService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulecommon.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登陆信息处理线程
 */
@Component
public class LoginLogJob implements AsyncService<LoginLogVo> {

    @Autowired
    LoginLogService loginLogService;
    @Autowired
    UserService userService;

    @Override
    public void executeAsync(LoginLogVo vo) {

        LoginLog loginLog = new LoginLog();

        loginLog.setIp(vo.getIp());

        loginLog.setAccount(vo.getAccount());
        loginLog.setUserId(vo.getUserId());
        loginLog.setDescription(vo.getRemark());
        loginLog.setType(vo.getType());
        String address = IpUtil.getAddress(vo.getIp());
        if (address != null) {
            loginLog.setAddress(address);
        }
        User user = null;
        if (vo.getUserId() != null) {
            user = userService.findById(vo.getUserId());
        }
        if (user != null) {
            loginLog.setFirstProxy(user.getFirstProxy());
            loginLog.setSecondProxy(user.getSecondProxy());
            loginLog.setThirdProxy(user.getThirdProxy());
        }
        loginLogService.save(loginLog);

    }

}
