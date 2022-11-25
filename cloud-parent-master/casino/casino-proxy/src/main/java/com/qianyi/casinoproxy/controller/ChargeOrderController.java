package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.business.ChargeOrderBusiness;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.ChargeOrderVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 充值申请
 */
@Slf4j
@RestController
@RequestMapping("/chargeOrder")
@Api(tags = "资金中心")
public class ChargeOrderController {
    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private ChargeOrderBusiness chargeOrderBusiness;

    @Autowired
    private CollectionBankcardService collectionBankcardService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;
    /**
     * 充值申请列表
     *
     * @param status 状态(0未确认 1已确认)
     * @param orderNo 充值订单号
     * @param account 会员账号 account
     * @return
     */
    @ApiOperation("充值申请列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "状态(0未确认 1已确认)", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "account", value = "会员账号", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = false),
    })
    @GetMapping("/chargeOrderList")
    public ResponseEntity<ChargeOrderVo> chargeOrderList(Integer pageSize, Integer pageCode, Integer status,
        String orderNo, String account,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        ChargeOrder order = new ChargeOrder();
        if (CasinoProxyUtil.setParameter(order)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        order.setStatus(status);
        order.setOrderNo(orderNo);
        if (!CasinoProxyUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            order.setUserId(user.getId());
        }
        Page<ChargeOrder> chargeOrderPage = chargeOrderService.findChargeOrderPage(order, pageable,startDate,endDate);
        PageResultVO<ChargeOrderVo> pageResultVO =new PageResultVO(chargeOrderPage);
        List<ChargeOrder> content = chargeOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<ChargeOrderVo> chargeOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(ChargeOrder::getUserId).collect(Collectors.toList());
            List<Long> collect = content.stream().map(ChargeOrder::getBankcardId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            List<CollectionBankcard> all = collectionBankcardService.findAll(collect);
            Map<Long, CollectionBankcard> bankcardMap = all.stream().collect(Collectors.toMap(CollectionBankcard::getId, a -> a, (k1, k2) -> k1));
            if(userList != null){
                content.stream().forEach(chargeOrder ->{
                    ChargeOrderVo chargeOrderVo = new ChargeOrderVo(chargeOrder);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(chargeOrder.getUserId())){
                            chargeOrderVo.setAccount(user.getAccount());
                            this.setCollectionBankcard(bankcardMap.get(chargeOrder.getBankcardId()),chargeOrderVo);
                        }
                    });
                    chargeOrderVoList.add(chargeOrderVo);
                });
            }
            pageResultVO.setContent(chargeOrderVoList);
            userIds.clear();
            collect.clear();
            userList.clear();
            all.clear();
            bankcardMap.clear();
        }
        return ResponseUtil.success(pageResultVO);
    }
    private void  setCollectionBankcard(CollectionBankcard collectionBankcard, ChargeOrderVo chargeOrderVo){
        if (CasinoProxyUtil.checkNull(collectionBankcard)){
            return ;
        }
        chargeOrderVo.setBankNo(collectionBankcard.getBankNo());
        chargeOrderVo.setAccountName(collectionBankcard.getAccountName());
    }

//    /**
//     * 充值申请列表
//     *
//     * @param id 充值订单id
//     * @param status 汇款状态，0.未确认。 1.成功   2.失败, 3.失效
//     * @return
//     */
//    @ApiOperation("审核充值订单")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "订单id", required = true),
//            @ApiImplicitParam(name = "status", value = "汇款状态， 1.成功  2.失败", required = true),
//            @ApiImplicitParam(name = "remark", value = "备注", required = false),
//    })
//    @PostMapping("/updateChargeOrder")
//    public ResponseEntity updateChargeOrder(Long id, Integer status,String remark){
//        if (CasinoProxyUtil.checkNull(id,status)){
//            ResponseUtil.custom("参数不合法");
//        }
//        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
//            return ResponseUtil.custom("参数不合法");
//        }
//        Long authId = CasinoProxyUtil.getAuthId();
//        ProxyUser byId = proxyUserService.findById(authId);
//        String lastModifier = (byId == null || byId.getUserName() == null)? "" : byId.getUserName();
//        return chargeOrderBusiness.checkOrderSuccess(id,status,remark,lastModifier);
//    }
//
//
//
//    /**
//     * 修改充值备注
//     *
//     * @return
//     */
//    @ApiOperation("修改充值备注")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "订单id", required = true),
//            @ApiImplicitParam(name = "remark", value = "备注", required = false)
//    })
//    @PostMapping("/updateChargeOrdersRemark")
//    public ResponseEntity updateChargeOrdersRemark(Long id,String remark){
//        if (CasinoProxyUtil.checkNull(id)){
//            ResponseUtil.custom("参数不合法");
//        }
//        ChargeOrder order = chargeOrderService.findChargeOrderByIdUseLock(id);
//        if(order == null){
//            return ResponseUtil.custom("订单不存在或已被处理");
//        }
//        chargeOrderService.updateChargeOrdersRemark(remark,order.getId());
//        return ResponseUtil.success();
//    }

    @ApiOperation("充值申请列表统计")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "状态(0未确认 1已确认)", required = false),
        @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
        @ApiImplicitParam(name = "account", value = "会员账号", required = false),
        @ApiImplicitParam(name = "type", value = "会员类型:0、公司会员，1、渠道会员 2、官方会员", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间", required = false),
    })
    @GetMapping("/findChargeOrderSum")
    public ResponseEntity<ChargeOrderVo> findChargeOrderSum(Integer status, String orderNo,
        String account,Integer type,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ChargeOrder order = new ChargeOrder();
        if (CasinoProxyUtil.setParameter(order)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        Long userId = null;
        if (!CasinoProxyUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (CasinoProxyUtil.checkNull(user)){
                ChargeOrderVo vo = new ChargeOrderVo();
                vo.setChargeAmount(BigDecimal.ZERO);
                return ResponseUtil.success(vo);
            }
            userId = user.getId();
        }
        order.setUserId(userId);
        order.setStatus(status);
        order.setOrderNo(orderNo);
        order.setType(type);
        ChargeOrder chargeOrder = chargeOrderService.findChargeOrderSum(order,startDate,endDate);
        ChargeOrderVo vo = new ChargeOrderVo();
        vo.setChargeAmount(chargeOrder==null?BigDecimal.ZERO:chargeOrder.getChargeAmount());
        return ResponseUtil.success(vo);
    }
}
