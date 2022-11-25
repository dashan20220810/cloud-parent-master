package com.qianyi.payadmin.controller;

import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.payadmin.util.PayUtil;
import com.qianyi.paycore.model.LoginLog;
import com.qianyi.paycore.service.LoginLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
public class UserController {
    @Autowired
    LoginLogService loginLogService;

    @GetMapping
    @ApiOperation("获取登陆日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id，传值为单个查询", required = false),
            @ApiImplicitParam(name = "pageCode", value = "分页页码（默认第1页）", required = false),
            @ApiImplicitParam(name = "pageSize", value = "分页大小(默认10条)", required = false)

    })
    public ResponseEntity pageList(Long userId,Integer pageCode,Integer pageSize) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);

        Sort sort=Sort.by("createTime").descending();
        Pageable pageable = PayUtil.setPageable(pageCode,pageSize, sort);
        Page<LoginLog> page = loginLogService.pageByCondition(loginLog,pageable);
        return ResponseUtil.success(page);
    }

}
