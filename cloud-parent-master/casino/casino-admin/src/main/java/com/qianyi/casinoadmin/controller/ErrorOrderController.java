package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.annotation.NoAuthorization;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/aErrorOrder")
@Api(tags = "资金中心")
public class ErrorOrderController {


    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    private UserController userController;


    @Autowired
    private SysUserService sysUserService;


    @ApiOperation("异常订单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "status", value = "状态", required = false),
            @ApiImplicitParam(name = "type", value = "类型", required = false),
            @ApiImplicitParam(name = "userName", value = "用户名", required = false),
            @ApiImplicitParam(name = "platform", value = "平台名称", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = false),
    })
    @GetMapping("/errorOrderList")
    @NoAuthorization
    public ResponseEntity<ErrorOrder> errorOrderList(Integer pageSize, Integer pageCode,String orderNo,Integer status,Integer type,String userName,String platform,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        Sort sort = Sort.by("updateTime").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        ErrorOrder order = new ErrorOrder();
        order.setOrderNo(orderNo);
        order.setStatus(status);
        order.setType(type);
        order.setUserName(userName);
        order.setPlatform(platform);
        //查询异常列表
        Page<ErrorOrder> errorOrderPage = errorOrderService.findErrorOrderPage(order, pageable, startDate, endDate);
        PageResultVO<ErrorOrder> pageResult =new PageResultVO(errorOrderPage);
        List<ErrorOrder> content = errorOrderPage.getContent();
        List<String> updateBys = content.stream().map(ErrorOrder::getUpdateBy).collect(Collectors.toList());
        //根据最后操作人id查询用户列表
        List<SysUser> userList = sysUserService.findAll(updateBys);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getId, a -> a, (k1, k2) -> k1));
        content.stream().forEach(info ->{
            if (info.getUpdateBy()!=null){
                SysUser user = userMap.get(Long.valueOf(info.getUpdateBy()));
                if (user!=null){
                    info.setUpdateBy(user.getUserName());
                }
            }

        });
        pageResult.setContent(content);
        return ResponseUtil.success(pageResult);
    }

    /**
     * 修改异常订单状态
     *
     * @return
     */
    @ApiOperation("修改异常订单状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "status", value = "状态(0.失败、1.自动补单成功、2.后台审核通过、3.后台拒绝、4.后台审核通过上分)", required = false)
    })
    @PostMapping("/updateErrorOrdersStatus")
    @NoAuthorization
    public ResponseEntity updateErrorOrdersStatus(Long id,Integer status){
        if (LoginUtil.checkNull(id) || LoginUtil.checkNull(status)){
            ResponseUtil.custom("参数不合法");
        }
        ErrorOrder order = errorOrderService.findErrorOrderByIdUseLock(id);
        if(order !=null && order.getStatus()==status){
            return ResponseUtil.custom("订单不存在或已被处理");
        }
        if (status==CommonConst.NUMBER_4){
            //人工上分
            ResponseEntity responseEntity = userController.saveSystemChargeOrder(order.getOrderNo(),order.getUserId(), order.getUserName(), "补单",order.getMoney().toString(), BigDecimal.ONE);
            if (responseEntity.getCode()!=CommonConst.NUMBER_0){
                return ResponseUtil.fail();
            }
        }
        order.setStatus(status);
        errorOrderService.save(order);
        return ResponseUtil.success();
    }
}
