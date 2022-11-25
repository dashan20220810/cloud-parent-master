package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.OrderService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.OrderVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("order")
@Api(tags = "资金中心")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageUtil messageUtil;
    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     * @return
     */
    @ApiOperation("订单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "用户账号", required = false),
            @ApiImplicitParam(name = "no", value = "订单号", required = false),
            @ApiImplicitParam(name = "gamePlatformName", value = "平台名称: WM, PG/CQ9, OBDJ, OBTY, SABA", required = false),
    })
    @GetMapping("findOrderList")
    public ResponseEntity<OrderVo> findOrderList(Integer pageSize, Integer pageCode,  String account, String no,   String gamePlatformName){
        Order order = new Order();
        if (CasinoProxyUtil.setParameter(order)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        order.setNo(no);
        order.setGamePlatformName(gamePlatformName);
        if (!CasinoProxyUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            order.setUserId(user.getId());
        }
        Sort sort=Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        Page<Order> userPage = orderService.findOrderPage(pageable, order);
        PageResultVO<OrderVo> pageResultVO = new PageResultVO(userPage);
        List<Order> content = userPage.getContent();
        if(content != null && content.size() > 0){
            List<OrderVo> orderVoList =new LinkedList<>();
            List<Long> userIds = content.stream().map(Order::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(change ->{
                    OrderVo orderVo = new OrderVo(change);
                    orderVo.setRemark(messageUtil.get(orderVo.getRemark()));
                    userList.stream().forEach(user->{
                        if (user.getId().equals(change.getUserId())){
                            orderVo.setAccount(user.getAccount());
                        }
                    });
                    orderVoList.add(orderVo);
                });
            }
            pageResultVO.setContent(orderVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

//    @ApiOperation("添加订单")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userId", value = "用户名", required = true),
//            @ApiImplicitParam(name = "money", value = "订单金额", required = true),
//            @ApiImplicitParam(name = "remark", value = "备注", required = false),
//    })
//    @PostMapping("saveOrder")
//    public ResponseEntity saveOrder(Long userId, BigDecimal money, String remark){
//        Order order = new Order();
//        order.setUserId(userId);
//        order.setMoney(money);
//        order.setNo(orderService.getOrderNo());
//        order.setRemark(remark + "");
//        order.setState(Constants.order_wait);
//        orderService.save(order);
//        return ResponseUtil.success();
//    }
//
//    @ApiOperation("修改订单")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "订单id", required = true),
//            @ApiImplicitParam(name = "status", value = "订单状态", required = true),
//            @ApiImplicitParam(name = "remark", value = "备注", required = false),
//    })
//    @PostMapping("updateOrder")
//    public ResponseEntity updateOrder(Long id, Integer status, String remark){
//        Order order = orderService.findById(id);
//        if(order == null){
//            return ResponseUtil.custom("订单不存在");
//        }
//
//        order.setState(status);
//        if(LoginUtil.checkNull(remark)){
//            order.setRemark(remark);
//        }
//
//        orderService.save(order);
//        return ResponseUtil.success();
//    }



}
