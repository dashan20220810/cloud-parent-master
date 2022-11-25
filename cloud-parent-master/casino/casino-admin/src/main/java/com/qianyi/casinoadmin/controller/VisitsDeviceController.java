package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.VisitsDevice;
import com.qianyi.casinocore.model.VisitsDeviceAddressBook;
import com.qianyi.casinocore.service.VisitsDeviceAddressBookService;
import com.qianyi.casinocore.service.VisitsDeviceService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/visitsDevice")
@Api(tags = "运营中心")
public class VisitsDeviceController {

    @Autowired
    private VisitsDeviceService visitsDeviceService;

    @Autowired
    private VisitsDeviceAddressBookService visitsDeviceAddressBookService;

    @ApiOperation("访问设备列表分页查询")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "ip", value = "IP", required = false),
        @ApiImplicitParam(name = "model", value = "设备型号", required = false),
        @ApiImplicitParam(name = "udid", value = "设备型号", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @GetMapping("/findPage")
    public ResponseEntity<VisitsDevice> findPage(Integer pageSize,Integer pageCode, String ip,
        String model, String udid,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        VisitsDevice visitsDevice = new VisitsDevice();
        visitsDevice.setIp(ip);
        visitsDevice.setModel(model);
        visitsDevice.setUdid(udid);
        Page<VisitsDevice> page = visitsDeviceService.findPage(pageable, visitsDevice, startDate, endDate);
        return ResponseUtil.success(page);
    }

    @ApiOperation("分页查询通讯录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "visitsDeviceId", value = "访问设备ID", required = true),
    })
    @GetMapping("/findPageBook")
    @NoAuthentication
    public ResponseEntity<VisitsDeviceAddressBook> findPageBook(Integer pageSize,Integer pageCode, Long visitsDeviceId){
        if (LoginUtil.checkNull(visitsDeviceId)){
            return ResponseUtil.custom("参数不合法");
        }
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        VisitsDeviceAddressBook visitsDeviceAddressBook = new VisitsDeviceAddressBook();
        visitsDeviceAddressBook.setVisitsDeviceId(visitsDeviceId);
        Page<VisitsDeviceAddressBook> page = visitsDeviceAddressBookService.findPage(pageable, visitsDeviceAddressBook);
        return ResponseUtil.success(page);
    }
}
