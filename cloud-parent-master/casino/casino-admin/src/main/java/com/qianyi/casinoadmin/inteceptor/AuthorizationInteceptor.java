package com.qianyi.casinoadmin.inteceptor;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.CustomUserServiceBusiness;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.inteceptor.AbstractAuthorizationInteceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorizationInteceptor extends AbstractAuthorizationInteceptor {

    @Autowired
    SysUserService sysUserService;

    @Autowired
    CustomUserServiceBusiness customUserServiceBusiness;

    @Override
    protected boolean hasBan() {
        Long authId= LoginUtil.getLoginUserId();
//        if(authId == null){
//            return true;
//        }
        SysUser user=sysUserService.findById(authId);
        boolean flag= SysUser.checkUser(user);
        return !flag;
    }

    @Override
    protected boolean discharged() {
        Long authId= LoginUtil.getLoginUserId();
        if(authId == null){
            return true;
        }
//        SysUser user=sysUserService.findById(authId);
//        if(user.getUserName().equals("admin")){//admin所有权限
//            return true;
//        }

        return false;
    }

    /**
     * 此处进行权限认证
     *
     * @param request
     * @return
     */
    @Override
    public boolean hasPermission(HttpServletRequest request) {
        Long authId= LoginUtil.getLoginUserId();
        if(authId != null){
            //进行权限认证操作
            List<SysPermission> sysPermissionList = customUserServiceBusiness.findByAdminUserId(authId);
            if(LoginUtil.checkNull(sysPermissionList)){
                return false;
            }

            String servletPath = request.getServletPath();
            List<SysPermission> sysPermissions = sysPermissionList.stream().filter(sysPermission -> sysPermission.getUrl().trim().equals(servletPath)).collect(Collectors.toList());
            if(LoginUtil.checkNull(sysPermissions)){
                return false;
            }
            return true;
        }

        return false;
    }


}
