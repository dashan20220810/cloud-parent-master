package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.ChargeOrderVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.business.ChargeOrderBusiness;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    PlatformConfigService platformConfigService;
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
            @ApiImplicitParam(name = "type", value = "会员类型:0、公司会员，1、渠道会员 2、官方会员", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = false),
    })
    @NoAuthorization
    @GetMapping("/chargeOrderList")
    public ResponseEntity<ChargeOrderVo> chargeOrderList(Integer pageSize, Integer pageCode, Integer status, String orderNo,
                                                         String account,Integer type,
                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        ChargeOrder order = new ChargeOrder();
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = user.getId();
        }
        order.setUserId(userId);
        order.setStatus(status);
        order.setOrderNo(orderNo);
        order.setType(type);
        Page<ChargeOrder> chargeOrderPage = chargeOrderService.findChargeOrderPage(order, pageable,startDate,endDate);
        PageResultVO<ChargeOrderVo> pageResultVO =new PageResultVO(chargeOrderPage);
        List<ChargeOrder> content = chargeOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<ChargeOrderVo> chargeOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(ChargeOrder::getUserId).collect(Collectors.toList());
            List<Long> collect = content.stream().map(ChargeOrder::getBankcardId).collect(Collectors.toList());

            List<User> userList = userService.findAll(userIds);
            List<CollectionBankcard> all = collectionBankcardService.findAll(collect);
//            List<String> updateBys = content.stream().map(ChargeOrder::getUpdateBy).collect(Collectors.toList());
//            List<SysUser> sysUsers = sysUserService.findAll(updateBys);
            Map<Long, CollectionBankcard> bankcardMap = all.stream().collect(Collectors.toMap(CollectionBankcard::getId, a -> a, (k1, k2) -> k1));
            List<String> bankInfoIds = bankcardMap.values().stream().map(CollectionBankcard::getBankId).collect(Collectors.toList());
            List<BankInfo> bankInfos = bankInfoService.findAll(bankInfoIds);
            if(userList != null){
                content.stream().forEach(chargeOrder ->{
                    ChargeOrderVo chargeOrderVo = new ChargeOrderVo(chargeOrder);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(chargeOrder.getUserId())){
                            chargeOrderVo.setAccount(user.getAccount());
                            if (!LoginUtil.checkNull(user.getThirdProxy())){
                                ProxyUser proxyUserById = proxyUserService.findById(user.getThirdProxy());
                                chargeOrderVo.setThirdProxy(proxyUserById != null?proxyUserById.getUserName():"");
                            }
                        }
                    });
                    this.setCollectionBankcard(bankcardMap.get(chargeOrder.getBankcardId()),chargeOrderVo,bankInfos);
//                    sysUsers.stream().forEach(sysUser->{
//                        if (chargeOrder.getStatus() != CommonConst.NUMBER_0 && sysUser.getId().toString().equals(chargeOrder.getUpdateBy() == null?"":chargeOrder.getUpdateBy())){
//                            chargeOrderVo.setUpdateBy(sysUser.getUserName());
//                        }
//                    });
                    chargeOrderVoList.add(chargeOrderVo);
                });
            }
            pageResultVO.setContent(chargeOrderVoList);
            userIds.clear();
            collect.clear();
            userList.clear();
            all.clear();
            bankcardMap.clear();
            bankInfoIds.clear();
            bankInfos.clear();
        }
        return ResponseUtil.success(pageResultVO);
    }
    private void  setCollectionBankcard(CollectionBankcard collectionBankcard,ChargeOrderVo chargeOrderVo,List<BankInfo> bankInfos){
        if (LoginUtil.checkNull(collectionBankcard)){
            return ;
        }
        chargeOrderVo.setBankNo(collectionBankcard.getBankNo());
        chargeOrderVo.setAccountName(collectionBankcard.getAccountName());
        bankInfos.stream().forEach(bankInfo -> {
            if (bankInfo.getId().toString().equals(collectionBankcard.getBankId())){
                chargeOrderVo.setBankName(bankInfo.getBankName());
            }
        });
    }

    /**
     * 充值申请列表
     *
     * @param id 充值订单id
     * @param status 汇款状态，0.未确认。 1.成功   2.失败, 3.失效
     * @return
     */
    @ApiOperation("审核充值订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "汇款状态， 1.成功  2.失败", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
//            @ApiImplicitParam(name = "money", value = "上分金额", required = false),
    })
    @PostMapping("/updateChargeOrder")
    public ResponseEntity updateChargeOrder(Long id, Integer status,String remark){
        if (LoginUtil.checkNull(id,status)){
            ResponseUtil.custom("参数不合法");
        }
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
            return ResponseUtil.custom("参数不合法");
        }
//        ChargeOrder byId = chargeOrderService.findById(id);
//        if (LoginUtil.checkNull(byId)){
//            return ResponseUtil.custom("订单不存在");
//        }
//        if (byId.getThirdProxy() != null && byId.getThirdProxy() >= CommonConst.LONG_1){
//            return ResponseUtil.custom("代理充值订单不能处理");
//        }
        Long userId = LoginUtil.getLoginUserId();
        SysUser sysUser = sysUserService.findById(userId);
        String lastModifier = (sysUser == null || sysUser.getUserName() == null)? "" : sysUser.getUserName();

        if (status == CommonConst.NUMBER_1){
            Boolean aBoolean = platformConfigService.queryTotalPlatformQuota();
            if (!aBoolean){
                return  ResponseUtil.custom("审核通过失败,平台额度不足");
            }
        }
        ResponseEntity responseEntity = chargeOrderBusiness.checkOrderSuccess(id, status, remark, lastModifier);
        if (responseEntity.getCode() == CommonConst.NUMBER_0 && status == CommonConst.NUMBER_1) {
            Object data = responseEntity.getData();
            platformConfigService.backstage(CommonConst.NUMBER_0, new BigDecimal(String.valueOf(data)));
        }
        return responseEntity;
    }

    /**
     * 修改充值备注
     *
     * @return
     */
    @ApiOperation("修改充值备注")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false)
    })
    @PostMapping("/updateChargeOrdersRemark")
    public ResponseEntity updateChargeOrdersRemark(Long id,String remark){
        if (LoginUtil.checkNull(id)){
            ResponseUtil.custom("参数不合法");
        }
        ChargeOrder order = chargeOrderService.findChargeOrderByIdUseLock(id);
        if(order == null){
            return ResponseUtil.custom("订单不存在或已被处理");
        }
        chargeOrderService.updateChargeOrdersRemark(remark,order.getId());
        return ResponseUtil.success();
    }

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
    @NoAuthentication
    public ResponseEntity<ChargeOrderVo> findChargeOrderSum(Integer status, String orderNo,
                                                         String account,Integer type,
                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ChargeOrder order = new ChargeOrder();
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
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
