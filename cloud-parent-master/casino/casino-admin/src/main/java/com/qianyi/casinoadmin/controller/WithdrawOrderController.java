package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.WithdrawOrderVo;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.annotation.NoAuthentication;
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
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提现记录表
 */
@Slf4j
@RestController
@RequestMapping("/withdraw")
@Api(tags = "资金中心")
public class WithdrawOrderController {

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private WithdrawBusiness withdrawBusiness;

    @Autowired
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    PlatformConfigService platformConfigService;

    @ApiOperation("提现列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "status", value = "订单状态", required = false),
        @ApiImplicitParam(name = "no", value = "订单号", required = false),
        @ApiImplicitParam(name = "bankId", value = "银行卡Id", required = false),
        @ApiImplicitParam(name = "account", value = "用户账号", required = false),
        @ApiImplicitParam(name = "realName", value = "开户名", required = false),
        @ApiImplicitParam(name = "bankName", value = "银行名称", required = false),
        @ApiImplicitParam(name = "type", value = "会员类型:0、公司会员，1、渠道会员", required = false),
    })
    @NoAuthorization
    @GetMapping("/withdrawList")
    public ResponseEntity<WithdrawOrderVo> withdrawList(Integer pageSize,Integer pageCode, Integer status, String account,
        String no, String bankId,Integer type,String realName,String bankName,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            withdrawOrder.setUserId(user.getId());
        }
        List<Long> bankcardIds = null;
        if (!LoginUtil.checkNull(realName)){
            List<Bankcards> bankcards = bankcardsService.findByRealName(realName);
            if (LoginUtil.checkNull(bankcards) || bankcards.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }
            bankcardIds = bankcards.stream().map(Bankcards::getId).collect(Collectors.toList());
        }
        if (!LoginUtil.checkNull(bankName)){
            BankInfo byBankName = bankInfoService.findByBankName(bankName);
            if (LoginUtil.checkNull(byBankName)){
                return ResponseUtil.success();
            }
            List<Bankcards> bankcardList = bankcardsService.findByBankId(byBankName.getId().toString());
            if (LoginUtil.checkNull(bankcardList) || bankcardList.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }
            if (LoginUtil.checkNull(bankcardIds)){
                bankcardIds = bankcardList.stream().map(Bankcards::getId).collect(Collectors.toList());
            }else {
                bankcardIds.retainAll(bankcardList.stream().map(Bankcards::getId).collect(Collectors.toList()));
            }
        }
        withdrawOrder.setStatus(status);
        withdrawOrder.setNo(no);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setType(type);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, withdrawOrder,startDate,endDate,bankcardIds);
        PageResultVO<WithdrawOrderVo> pageResultVO = new PageResultVO(withdrawOrderPage);
        List<WithdrawOrder> content = withdrawOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<WithdrawOrderVo> withdrawOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(WithdrawOrder::getUserId).collect(Collectors.toList());
            List<String> collect = content.stream().map(WithdrawOrder::getBankId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            List<Bankcards> all = bankcardsService.findAll(collect);
            //查询银行卡
            List<BankInfo> bankInfoList = bankInfoService.findAll();
            List<Long> auditIds = content.stream().map(WithdrawOrder::getAuditId).collect(Collectors.toList());
            List<SysUser> sysUsers = sysUserService.findAllLong(auditIds);
            Map<Long, Bankcards> bankcardMap = all.stream().collect(Collectors.toMap(Bankcards::getId, a -> a, (k1, k2) -> k1));
            if(userList != null){
                content.stream().forEach(withdraw ->{
                    WithdrawOrderVo withdrawOrderVo = new WithdrawOrderVo(withdraw);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(withdraw.getUserId())){
                            withdrawOrderVo.setAccount(user.getAccount());
                            if (!LoginUtil.checkNull(withdrawOrderVo.getBankId())){
                                try {
                                    this.setBankcards(bankcardMap.get(Long.valueOf(withdrawOrderVo.getBankId())),withdrawOrderVo,bankInfoList);
                                }catch (Exception ex){
                                    log.info("bankId类型转换错误{}",withdrawOrderVo.getBankId());
                                }
                            }
                        }
                    });
                    sysUsers.stream().forEach(sysUser -> {
                        if (withdraw.getAuditId().intValue() != CommonConst.LONG_0.longValue()
                            && sysUser.getId().longValue() == withdraw.getAuditId().longValue()) {
                            withdrawOrderVo.setAuditIdModifier(sysUser.getUserName());
                        }
                    });
                    withdrawOrderVoList.add(withdrawOrderVo);
                });
            }
            pageResultVO.setContent(withdrawOrderVoList);
            userIds.clear();
            collect.clear();
            userList.clear();
            all.clear();
            bankInfoList.clear();
            bankcardMap.clear();
        }
        return ResponseUtil.success(pageResultVO);
    }
    private void  setBankcards(Bankcards bankcards,WithdrawOrderVo withdrawOrderVo,   List<BankInfo> bankInfoList ){
        if (LoginUtil.checkNull(bankcards)){
            return ;
        }
        withdrawOrderVo.setBankNo(bankcards.getBankAccount());
        withdrawOrderVo.setAccountName(bankcards.getRealName());
        //查询银行卡
        Map<Long, BankInfo> bankInfoMap = bankInfoList.stream().collect(Collectors.toMap(BankInfo::getId, a -> a, (k1, k2) -> k1));
        BankInfo bankInfo = bankInfoMap.get(Long.valueOf(bankcards.getBankId()));
        withdrawOrderVo.setBankName(bankInfo==null ? null : bankInfo.getBankName());
    }
    @ApiOperation("提现审核")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "订单id", required = true),
        @ApiImplicitParam(name = "status", value = "审核状态，1：通过，2：拒绝", required = true),
        @ApiImplicitParam(name = "remark", value = "备注", required = false),
    })
    @PostMapping("saveWithdraw")
    public ResponseEntity saveWithdraw(Long id, Integer status,String remark){
        if (LoginUtil.checkNull(id,status)){
            return ResponseUtil.custom("参数不合法");
        }
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
            return ResponseUtil.custom("参数不合法");
        }
        //        WithdrawOrder byId = withdrawOrderService.findById(id);
        //        if (LoginUtil.checkNull(byId)){
        //            return ResponseUtil.custom("订单不存在");
        //        }
        //        if (byId.getThirdProxy() != null && byId.getThirdProxy() >= CommonConst.LONG_1){
        //            return ResponseUtil.custom("代理提现订单不能处理");
        //        }
        Long userId = LoginUtil.getLoginUserId();
        SysUser sysUser = sysUserService.findById(userId);
        String lastModifier = (sysUser == null || sysUser.getUserName() == null)? "" : sysUser.getUserName();
        ResponseEntity responseEntity = withdrawBusiness.updateWithdrawAndUser(id, status, lastModifier, remark);
        if (responseEntity.getCode() == CommonConst.NUMBER_0 && status == CommonConst.NUMBER_1) {
            Object data = responseEntity.getData();
            platformConfigService.backstage(CommonConst.NUMBER_1, new BigDecimal(String.valueOf(data)));
        }
        return responseEntity;
    }


    /**
     * 修改提现备注
     *
     * @return
     */

    @ApiOperation("修改提现备注")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "订单id", required = true),
        @ApiImplicitParam(name = "remark", value = "备注", required = false)
    })
    @PostMapping("/updateWithdrawOrderRemark")
    public ResponseEntity updateWithdrawOrderRemark(Long id,String remark){
        if (LoginUtil.checkNull(id)){
            ResponseUtil.custom("参数不合法");
        }
        WithdrawOrder withdrawOrder = withdrawOrderService.findUserByIdUseLock(id);
        if(withdrawOrder == null){
            return ResponseUtil.custom("订单不存在");
        }
        withdrawOrderService.updateWithdrawOrderRemark(remark,withdrawOrder.getId());
        return ResponseUtil.success();
    }

    @ApiOperation("提现列表金额统计")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "订单状态", required = false),
        @ApiImplicitParam(name = "no", value = "订单号", required = false),
        @ApiImplicitParam(name = "bankId", value = "银行卡Id", required = false),
        @ApiImplicitParam(name = "account", value = "用户账号", required = false),
        @ApiImplicitParam(name = "type", value = "会员类型:0、公司会员，1、渠道会员", required = false),
    })
    @GetMapping("/findWithdrawOrderSum")
    @NoAuthentication
    public ResponseEntity<WithdrawOrderVo> findWithdrawOrderSum(Integer status, String account,String no, String bankId,Integer type,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                WithdrawOrderVo vo = new WithdrawOrderVo();
                vo.setPracticalAmount(BigDecimal.ZERO);
                vo.setWithdrawMoney(BigDecimal.ZERO);
                vo.setServiceCharge(BigDecimal.ZERO);
                return ResponseUtil.success(vo);
            }
            withdrawOrder.setUserId(user.getId());
        }
        withdrawOrder.setStatus(status);
        withdrawOrder.setNo(no);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setType(type);
        WithdrawOrder withdrawOrder1 = withdrawOrderService.findWithdrawOrderSum(withdrawOrder,startDate,endDate);
        WithdrawOrderVo vo = new WithdrawOrderVo();
        if (LoginUtil.checkNull(withdrawOrder1)){
            vo.setPracticalAmount(BigDecimal.ZERO);
            vo.setWithdrawMoney(BigDecimal.ZERO);
            vo.setServiceCharge(BigDecimal.ZERO);
        }else {
            vo.setPracticalAmount(withdrawOrder1.getPracticalAmount());
            vo.setWithdrawMoney(withdrawOrder1.getWithdrawMoney());
            vo.setServiceCharge(withdrawOrder1.getServiceCharge());
        }
        return ResponseUtil.success(vo);
    }
}
