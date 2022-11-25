package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.WithdrawOrderVo;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/withdrawAudit")
@Api(tags = "资金中心")
public class WithdrawAuditController {

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

    public static List<Integer> status = new ArrayList<>();

    static {
        status.add(CommonConst.NUMBER_6);// 审核接单
        status.add(CommonConst.NUMBER_0);// 待审核
    }

    @ApiOperation("风控审核列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "no", value = "订单号", required = false),
        @ApiImplicitParam(name = "bankId", value = "银行卡Id", required = false),
        @ApiImplicitParam(name = "account", value = "用户账号", required = false),
        // @ApiImplicitParam(name = "status", value = "订单状态", required = false),
        @ApiImplicitParam(name = "realName", value = "开户名", required = false),
        @ApiImplicitParam(name = "bankName", value = "银行名称", required = false),
        @ApiImplicitParam(name = "type", value = "会员类型:0、公司会员，1、渠道会员", required = false),})
    @GetMapping("/findPage")
    public ResponseEntity<WithdrawOrderVo> withdrawList(Integer pageSize, Integer pageCode, String account, String no,
        String bankId, Integer type, String realName, String bankName,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if (!LoginUtil.checkNull(account)) {
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)) {
                return ResponseUtil.custom("用户不存在");
            }
            withdrawOrder.setUserId(user.getId());
        }
        List<Long> bankcardIds = null;
        if (!LoginUtil.checkNull(realName)) {
            List<Bankcards> bankcards = bankcardsService.findByRealName(realName);
            if (LoginUtil.checkNull(bankcards) || bankcards.size() == CommonConst.NUMBER_0) {
                return ResponseUtil.success();
            }
            bankcardIds = bankcards.stream().map(Bankcards::getId).collect(Collectors.toList());
        }
        if (!LoginUtil.checkNull(bankName)) {
            BankInfo byBankName = bankInfoService.findByBankName(bankName);
            if (LoginUtil.checkNull(byBankName)) {
                return ResponseUtil.success();
            }
            List<Bankcards> bankcardList = bankcardsService.findByBankId(byBankName.getId().toString());
            if (LoginUtil.checkNull(bankcardList) || bankcardList.size() == CommonConst.NUMBER_0) {
                return ResponseUtil.success();
            }
            if (LoginUtil.checkNull(bankcardIds)) {
                bankcardIds = bankcardList.stream().map(Bankcards::getId).collect(Collectors.toList());
            } else {
                bankcardIds.retainAll(bankcardList.stream().map(Bankcards::getId).collect(Collectors.toList()));
            }
        }
        withdrawOrder.setNo(no);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setType(type);
        Sort sort = Sort.by("auditId").descending();
        sort = sort.and(Sort.by("id").ascending());
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        List<Long> auditIds = new ArrayList<>();
        auditIds.add(CommonConst.LONG_0);
        auditIds.add(LoginUtil.getLoginUserId());
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, withdrawOrder, startDate,
            endDate, bankcardIds, auditIds, status);
        PageResultVO<WithdrawOrderVo> pageResultVO = new PageResultVO(withdrawOrderPage);
        List<WithdrawOrder> content = withdrawOrderPage.getContent();
        if (content != null && content.size() > 0) {
            List<WithdrawOrderVo> withdrawOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(WithdrawOrder::getUserId).collect(Collectors.toList());
            List<String> collect = content.stream().map(WithdrawOrder::getBankId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            List<Bankcards> all = bankcardsService.findAll(collect);
            // 查询银行卡
            List<BankInfo> bankInfoList = bankInfoService.findAll();
            // List<String> updateBys = content.stream().map(WithdrawOrder::getUpdateBy).collect(Collectors.toList());
            // List<SysUser> sysUsers = sysUserService.findAll(updateBys);
            Map<Long, Bankcards> bankcardMap =
                all.stream().collect(Collectors.toMap(Bankcards::getId, a -> a, (k1, k2) -> k1));
            if (userList != null) {
                content.stream().forEach(withdraw -> {
                    WithdrawOrderVo withdrawOrderVo = new WithdrawOrderVo(withdraw);
                    userList.stream().forEach(user -> {
                        if (user.getId().equals(withdraw.getUserId())) {
                            withdrawOrderVo.setAccount(user.getAccount());
                            if (!LoginUtil.checkNull(withdrawOrderVo.getBankId())) {
                                try {
                                    this.setBankcards(bankcardMap.get(Long.valueOf(withdrawOrderVo.getBankId())),
                                        withdrawOrderVo, bankInfoList);
                                } catch (Exception ex) {
                                    log.info("bankId类型转换错误{}", withdrawOrderVo.getBankId());
                                }
                            }
                        }
                    });
                    // sysUsers.stream().forEach(sysUser->{
                    // if (withdraw.getStatus() != CommonConst.NUMBER_0 &&
                    // sysUser.getId().toString().equals(withdraw.getUpdateBy() == null?"":withdraw.getUpdateBy())){
                    // withdrawOrderVo.setUpdateBy(sysUser.getUserName());
                    // }
                    // });
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

    private void setBankcards(Bankcards bankcards, WithdrawOrderVo withdrawOrderVo, List<BankInfo> bankInfoList) {
        if (LoginUtil.checkNull(bankcards)) {
            return;
        }
        withdrawOrderVo.setBankNo(bankcards.getBankAccount());
        withdrawOrderVo.setAccountName(bankcards.getRealName());
        // 查询银行卡
        Map<Long, BankInfo> bankInfoMap =
            bankInfoList.stream().collect(Collectors.toMap(BankInfo::getId, a -> a, (k1, k2) -> k1));
        BankInfo bankInfo = bankInfoMap.get(Long.valueOf(bankcards.getBankId()));
        withdrawOrderVo.setBankName(bankInfo == null ? null : bankInfo.getBankName());
    }

    @ApiOperation("风控审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "订单id", required = true),
        @ApiImplicitParam(name = "status", value = "审核状态，0.放弃 2.拒绝 6.审核接单 7.审核通过", required = true),
        @ApiImplicitParam(name = "remark", value = "备注", required = false),})
    @PostMapping("audit")
    public ResponseEntity saveWithdraw(Long id, Integer status, String remark) {
        if (LoginUtil.checkNull(id, status)) {
            return ResponseUtil.custom("参数不合法");
        }
        Long operator = LoginUtil.getLoginUserId();
        return withdrawBusiness.auditWithdraw(id, operator, status, remark);
    }
}
