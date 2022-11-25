package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.service.LoginLogService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/loginLog")
@Api(tags = "客户中心")
public class LoginLogController {
    @Autowired
    private LoginLogService loginLogService;
    /**
     * 分页查询用户登录日志
     *
     * @param ip 会员登录ip
     * @param userId 会员id
     * @param account 会员账号
     * @return
     */
    @ApiOperation("分页查询用户登录日志")
    @GetMapping("/findLoginLogPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "ip", value = "会员登录ip", required = false),
            @ApiImplicitParam(name = "userId", value = "会员id", required = false),
            @ApiImplicitParam(name = "account", value = "会员账号", required = false),
    })
    public ResponseEntity<LoginLog> findLoginLogPage(Integer pageSize, Integer pageCode, String ip, Long userId, String account){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        LoginLog loginLog = new LoginLog();
        loginLog.setIp(ip);
        loginLog.setAccount(account);
        loginLog.setUserId(userId);
        Page<LoginLog> loginLogPage = loginLogService.findLoginLogPage(loginLog, pageable);
        return ResponseUtil.success(loginLogPage);

    }


}
