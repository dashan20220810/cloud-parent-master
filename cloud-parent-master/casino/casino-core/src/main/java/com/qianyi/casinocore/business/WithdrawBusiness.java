package com.qianyi.casinocore.business;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.RedisLockUtil;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WithdrawBusiness {

    @Autowired
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private CodeNumChangeService codeNumChangeService;

    @Autowired
    private UserMoneyBusiness userMoneyBusiness;

    // 默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR = new BigDecimal("10");

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;

    @Autowired
    private AccountChangeService accountChangeService;

    public User getUserById(Long userId) {
        return userService.findById(userId);
    }

    public User getUserByLock(Long userId) {
        return userService.findUserByIdUseLock(userId);
    }

    public BigDecimal checkMoney(String money) {
        BigDecimal decMoney = null;
        try {
            decMoney = new BigDecimal(money);
        } catch (Exception e) {
            decMoney = new BigDecimal(-1);
        }

        return decMoney;
    }

    @Transactional
    public ResponseEntity payWithdraw(Long id, String lastModifier,Integer status, String remark) {
        String value = id.toString();
        String key = MessageFormat.format(RedisLockUtil.PROXY_GAME_RECORD_REPORT_BUSINESS, value);
        Boolean lock = false;
        try {
            lock = redisLockUtil.getLock(key, value);
            if (lock) {
                WithdrawOrder withdrawOrder = withdrawOrderService.findById(id);
                if (withdrawOrder == null) {
                    return ResponseUtil.custom("订单已被处理");
                }
                if (withdrawOrder.getStatus() != Constants.pass_the_audit) {
                    return ResponseUtil.custom("订单已被处理");
                }
                withdrawOrder.setRemark(remark);
                withdrawOrder.setLastModifier(lastModifier);
                withdrawOrder.setWithdrawTime(new Date());
                if (status == Constants.withdrawOrder_success) {// 确认成功
                    BigDecimal money = withdrawOrder.getWithdrawMoney();
                    //                                        PlatformConfig first = platformConfigService.findFirst();
                    //                                        if (first != null) {
                    //                                            // 得到手续费
                    //                                            BigDecimal serviceCharge = first.getWithdrawServiceCharge(withdrawOrder.getWithdrawMoney());
                    //                                            BigDecimal practicalAmount = withdrawOrder.getWithdrawMoney().subtract(serviceCharge);
                    //                                            withdrawOrder.setServiceCharge(serviceCharge);
                    //                                            withdrawOrder.setPracticalAmount(practicalAmount);
                    //                                        } else {
                    //                                            withdrawOrder.setServiceCharge(BigDecimal.ZERO);
                    //                                            withdrawOrder.setPracticalAmount(money);
                    //                                        }
                    withdrawOrder.setStatus(Constants.withdrawOrder_success);
                    withdrawOrderService.saveOrder(withdrawOrder);
                    log.info("通过提现userId {} 订单号 {} withdrawMoney is {}, practicalAmount is {}",
                        withdrawOrder.getUserId(), withdrawOrder.getNo(), money, withdrawOrder.getPracticalAmount());
                    return ResponseUtil.success(withdrawOrder.getPracticalAmount()==null?money:withdrawOrder.getPracticalAmount());
                } else if (status == Constants.paragraph_to_refuse) {// 拒绝
                    if (this.moneyback(withdrawOrder,Constants.paragraph_to_refuse)) {
                        return ResponseUtil.custom("退款失败");
                    }
                } else {
                    return ResponseUtil.custom("参数不合法");
                }
            } else {
                return ResponseUtil.custom("订单已被处理");
            }
        } catch (Exception ex) {
            log.error("财务出款出现异常{}", ex);
            return ResponseUtil.custom("系统异常请联系管理员");
        } finally {
            if (lock) {
                log.info("释放redis锁{}", key);
                redisLockUtil.releaseLock(key, value);
            }
        }
        return ResponseUtil.success();
    }

    @Transactional
    public ResponseEntity auditWithdraw(Long id, Long operator, Integer status, String remark) {
        String value = id.toString();
        String key = MessageFormat.format(RedisLockUtil.PROXY_GAME_RECORD_REPORT_BUSINESS, value);
        Boolean lock = false;
        try {
            lock = redisLockUtil.getLock(key, value);
            if (lock) {
                WithdrawOrder withdrawOrder = withdrawOrderService.findById(id);
                if (withdrawOrder == null) {
                    return ResponseUtil.custom("订单已被处理");
                }
                if (status == Constants.review_the_order) {// 审核接单
                    if (withdrawOrder.getStatus() != Constants.withdrawOrder_wait) {
                        return ResponseUtil.custom("订单已被处理");
                    }
                    withdrawOrder.setStatus(Constants.review_the_order);
                    withdrawOrder.setAuditId(operator);
                    withdrawOrder.setAuditRemark(remark);
                    withdrawOrderService.saveOrder(withdrawOrder);
                } else if (status == Constants.pass_the_audit) {// 审核通过
                    if (withdrawOrder.getStatus() != Constants.review_the_order
                        || withdrawOrder.getAuditId().longValue() != operator.longValue()) {
                        return ResponseUtil.custom("订单已被处理");
                    }
                    //                    BigDecimal money = withdrawOrder.getWithdrawMoney();
                    //                    PlatformConfig first = platformConfigService.findFirst();
                    //                    if (first != null) {
                    //                        // 得到手续费
                    //                        BigDecimal serviceCharge = first.getWithdrawServiceCharge(withdrawOrder.getWithdrawMoney());
                    //                        BigDecimal practicalAmount = withdrawOrder.getWithdrawMoney().subtract(serviceCharge);
                    //                        withdrawOrder.setServiceCharge(serviceCharge);
                    //                        withdrawOrder.setPracticalAmount(practicalAmount);
                    //                    } else {
                    //                        withdrawOrder.setServiceCharge(BigDecimal.ZERO);
                    //                        withdrawOrder.setPracticalAmount(money);
                    //                    }
                    withdrawOrder.setAuditTime(new Date());
                    withdrawOrder.setStatus(Constants.pass_the_audit);
                    withdrawOrder.setAuditRemark(remark);
                    withdrawOrderService.saveOrder(withdrawOrder);
                } else if (status == Constants.withdrawOrder_wait) {// 放弃
                    if (withdrawOrder.getStatus() != Constants.review_the_order
                        || withdrawOrder.getAuditId().longValue() != operator.longValue()) {
                        return ResponseUtil.custom("订单已被处理");
                    }
                    withdrawOrder.setStatus(Constants.withdrawOrder_wait);
                    withdrawOrder.setAuditId(CommonConst.LONG_0);
                    withdrawOrder.setAuditRemark(remark);
                    withdrawOrderService.saveOrder(withdrawOrder);
                } else if (status == Constants.withdrawOrder_fail) {// 拒绝,退钱
                    if (withdrawOrder.getStatus() != Constants.review_the_order
                        || withdrawOrder.getAuditId().longValue() != operator.longValue()) {
                        return ResponseUtil.custom("订单已被处理");
                    }
                    withdrawOrder.setAuditTime(new Date());
                    withdrawOrder.setAuditRemark(remark);
                    if (this.moneyback(withdrawOrder,Constants.withdrawOrder_fail)) {
                        return ResponseUtil.custom("退款失败");
                    }
                } else {
                    return ResponseUtil.custom("参数不合法");
                }
            } else {
                return ResponseUtil.custom("订单已被处理");
            }
        } catch (Exception ex) {
            log.error("审核出款出现异常{}", ex);
            return ResponseUtil.custom("系统异常请联系管理员");
        } finally {
            if (lock) {
                log.info("释放redis锁{}", key);
                redisLockUtil.releaseLock(key, value);
            }
        }
        return ResponseUtil.success();
    }

    private Boolean moneyback(WithdrawOrder withdrawOrder,Integer status) {
        BigDecimal money = withdrawOrder.getWithdrawMoney();
        withdrawOrder.setStatus(status);
        // 对用户数据进行行锁
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(withdrawOrder.getUserId());
        if (userMoney == null) {
            return true;
        }
        withdrawOrderService.saveOrder(withdrawOrder);
        BigDecimal amountBefore = userMoney.getMoney();
        userMoney.setMoney(userMoney.getMoney().add(money));
        userMoneyService.save(userMoney);
        // 记录用户账变
        this.saveAccountChang(AccountChangeEnum.WITHDRAWDEFEATED_CODE, userMoney.getUserId(), withdrawOrder,
            amountBefore, userMoney.getMoney());
        log.info("拒绝提现userId {} 订单号 {} withdrawMoney is {}, money is {}", userMoney.getUserId(), withdrawOrder.getNo(),
            money, userMoney.getMoney());
        return false;
    }

    /**
     * 获取用户的可提现金额
     *
     * @param userId
     * @return
     */
    @Transactional
    public BigDecimal getWithdrawMoneyByUserId(Long userId) {
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (userMoney == null) {
            return defaultVal;
        }
        BigDecimal codeNum = userMoney.getCodeNum();
        // 打码量为0时才有可提现金额
        if (codeNum != null && BigDecimal.ZERO.compareTo(codeNum) == 0) {
            BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney();
            return money.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return defaultVal;
    }

    /*
     * 检测参数
     * */
    public String checkParams(String withdrawPwd, BigDecimal decMoney, User user) {
        // 判断密码是否正确
        if (!withdrawPwd.equals(user.getWithdrawPassword())) {
            return "交易密码错误";
        }
        BigDecimal withdrawMoney = getWithdrawMoneyByUserId(user.getId());
        log.info("bigdecimal is devid is {}", decMoney.compareTo(withdrawMoney));

        // 判断是否大于可提金额
        if (decMoney.compareTo(withdrawMoney) >= 0) {
            return "超过可提金额";
        }
        return null;
    }

    /*
     * 进行转账
     * */
    @Transactional
    public ResponseEntity processWithdraw(BigDecimal money, String bankId, Long userId) {
        if (money == null) {
            return ResponseUtil.custom("提现金额不允许为空");
        }
        Bankcards bankcards = bankcardsService.findById(Long.parseLong(bankId));
        if (bankcards == null || !bankcards.getUserId().equals(userId)) {
            return ResponseUtil.custom("银行卡不属于当前用户");
        }
        Integer count = withdrawOrderService.countByUserIdAndStatus(userId, 0);
        if (count > 0) {
            return ResponseUtil.custom("您有一笔提款订单正在审核,无法再次提交");
        }
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        BigDecimal codeNum = userMoney.getCodeNum();
        // 打码量未清0没有可提现金额
        if (codeNum.compareTo(BigDecimal.ZERO) == 1) {
            return ResponseUtil.custom("当前用户可提现金额为0");
        }
        BigDecimal withdrawMoney = userMoney.getMoney();
        if (money.compareTo(withdrawMoney) == 1) {
            return ResponseUtil.custom("超过可提金额,最高可提额为", withdrawMoney.stripTrailingZeros().toPlainString());
        }
        // 查询提现金额限制
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig != null) {
            BigDecimal minMoney = platformConfig.getWithdrawMinMoney();
            BigDecimal maxMoney = platformConfig.getWithdrawMaxMoney();
            if (minMoney != null && money.compareTo(minMoney) == -1) {
                return ResponseUtil.custom("提现金额小于最低限额,单笔最低限额为", minMoney.stripTrailingZeros().toPlainString());
            }
            if (maxMoney != null && money.compareTo(maxMoney) == 1) {
                return ResponseUtil.custom("提现金额大于最高限额,单笔最高限额为", maxMoney.stripTrailingZeros().toPlainString());
            }
        }
        WithdrawOrder withdrawOrder = getWidrawOrder(money, bankId, userId, bankcards);
        withdrawOrderService.saveOrder(withdrawOrder);
        log.info("money is {}, draw money is {}", money, userMoney.getMoney());
        userMoneyService.subMoney(userId, money);
        // 账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.WITHDRAW_APPLY);
        vo.setAmount(money.negate());
        vo.setAmountBefore(userMoney.getMoney());
        BigDecimal moneyAfter = userMoney.getMoney().subtract(money);
        vo.setAmountAfter(moneyAfter);
        asyncService.executeAsync(vo);
        userMoney.setMoney(moneyAfter);
        return ResponseUtil.success(userMoney);
    }

    private WithdrawOrder getWidrawOrder(BigDecimal money, String bankId, Long userId, Bankcards bankcards) {
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setWithdrawMoney(money);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setUserId(userId);
        withdrawOrder.setNo(orderService.getOrderNo());
        withdrawOrder.setStatus(0);
        withdrawOrder.setRemitType(1);
        withdrawOrder.setBankAccount(bankcards.getBankAccount());
        User user = userService.findById(userId);
        if (user != null) {
            withdrawOrder.setFirstProxy(user.getFirstProxy());
            withdrawOrder.setSecondProxy(user.getSecondProxy());
            withdrawOrder.setThirdProxy(user.getThirdProxy());
            withdrawOrder.setType(user.getType());
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (first != null) {
            // 得到手续费
            BigDecimal serviceCharge = first.getWithdrawServiceCharge(withdrawOrder.getWithdrawMoney());
            BigDecimal practicalAmount = withdrawOrder.getWithdrawMoney().subtract(serviceCharge);
            withdrawOrder.setServiceCharge(serviceCharge);
            withdrawOrder.setPracticalAmount(practicalAmount);
        } else {
            withdrawOrder.setServiceCharge(BigDecimal.ZERO);
            withdrawOrder.setPracticalAmount(money);
        }
        return withdrawOrder;
    }

    // @Transactional
    // public ResponseEntity updateWithdrawAndUser(WithdrawOrder withdrawOrder) {
    // Long userId = withdrawOrder.getUserId();
    // //对用户数据进行行锁
    // UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
    // if (userMoney == null) {
    // return ResponseUtil.custom("用户不存在");
    // }
    // userMoney.setMoney(userMoney.getMoney().add(withdrawOrder.getWithdrawMoney()));
    // WithdrawOrder withdraw = withdrawOrderService.saveOrder(withdrawOrder);
    // log.info("user sum money is {}, add withdrawMoney is {}",userMoney.getMoney(), withdrawOrder.getWithdrawMoney());
    // userMoneyService.save(userMoney);
    // return ResponseUtil.success(withdraw);
    // }
    // 后台直接下分
    @Transactional
    public ResponseEntity updateWithdrawAndUser(User user, Long userId, BigDecimal withdrawMoney, String bankId,
        Integer status, Long auditId, String remark) {
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        if (userMoney.getMoney().compareTo(withdrawMoney) < 0) {
            return ResponseUtil.custom("余额不足");
        }
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setFirstProxy(user.getFirstProxy());
        withdrawOrder.setSecondProxy(user.getSecondProxy());
        withdrawOrder.setThirdProxy(user.getThirdProxy());
        withdrawOrder.setWithdrawMoney(withdrawMoney);
        // withdrawOrder.setPracticalAmount(withdrawMoney);
        // withdrawOrder.setServiceCharge(BigDecimal.ZERO);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setUserId(userId);
        withdrawOrder.setNo(orderService.getOrderNo());
        withdrawOrder.setStatus(status);
        withdrawOrder.setRemark(remark);
        //        withdrawOrder.setLastModifier(lastModifier);
        withdrawOrder.setAuditId(auditId);
        withdrawOrder.setAuditTime(new Date());
        withdrawOrder.setType(user.getType());
        withdrawOrder.setRemitType(CommonConst.NUMBER_4);
        withdrawOrderService.saveOrder(withdrawOrder);
        BigDecimal amountBefore = userMoney.getMoney();
        BigDecimal money = amountBefore.subtract(withdrawMoney);
        userMoney.setMoney(money);
        userMoneyService.save(userMoney);
        // 清零点方法
        checkClearBalance(userId, withdrawOrder.getWithdrawMoney(), userMoney);
        log.info("后台直接下分userId {} 订单号 {} withdrawMoney is {}, money is {}", userMoney.getUserId(),
            withdrawOrder.getNo(), withdrawMoney, money);
        // 记录用户账变
        this.saveAccountChang(AccountChangeEnum.SUB_CODE, userMoney.getUserId(), withdrawOrder, amountBefore,
            userMoney.getMoney());
        return ResponseUtil.success(userMoney);
    }

    @Async
    private void checkClearBalance(Long userId, BigDecimal withdrawMoney, UserMoney user) {

        PlatformConfig platformConfig = platformConfigService.findFirst();
        BigDecimal minCodeNumVal = DEFAULT_CLEAR;
        if (platformConfig != null && platformConfig.getClearCodeNum() != null) {
            minCodeNumVal = platformConfig.getClearCodeNum();
        }
        // 余额小于等于最小清零打码量时 直接清0
        BigDecimal beforeBalance = user.getBalance().subtract(withdrawMoney);
        if (beforeBalance.compareTo(minCodeNumVal) < 1) {
            this.checkClearCodeNum(userId, user, minCodeNumVal);
        } else {
            userMoneyBusiness.subBalanceAdmin(userId, withdrawMoney);
        }
    }

    public void checkClearCodeNum(Long userId, UserMoney user, BigDecimal minCodeNumVal) {
        // 打码已经归0，实时余额直接归0
        if (user.getCodeNum().compareTo(BigDecimal.ZERO) == 0) {
            userMoneyService.subBalance(userId, user.getBalance());
            return;
        }

        // 余额小于等于最小清零打码量时 直接清0
        // 打码量和实时余额都清0
        userMoneyService.subCodeNum(userId, user.getCodeNum());
        userMoneyService.subBalance(userId, user.getBalance());
        CodeNumChange codeNumChange =
            CodeNumChange.setCodeNumChange(userId, null, null, user.getCodeNum(), BigDecimal.ZERO);
        codeNumChange.setType(1);
        codeNumChange.setClearCodeNum(minCodeNumVal);
        codeNumChangeService.save(codeNumChange);
        log.info("触发最小清零打码量，打码量清0,最小清0点={},UserId={}", minCodeNumVal, userId);
    }

    @Transactional
    public ResponseEntity updateWithdrawAndUser(Long id, Integer status, String lastModifier, String remark) {
        WithdrawOrder withdrawOrder = withdrawOrderService.findUserByIdUseLock(id);
        if (withdrawOrder == null || withdrawOrder.getStatus() != 0) {
            return ResponseUtil.custom("订单已被处理");
        }
        withdrawOrder.setLastModifier(lastModifier);
        withdrawOrder.setRemark(remark);
        // 提现通过或其他
        // if(status == Constants.withdrawOrder_freeze){//冻结提现金额
        // withdrawOrder.setStatus(status);
        // withdrawOrderService.saveOrder(withdrawOrder);
        // return ResponseUtil.success();
        // }
        Long userId = withdrawOrder.getUserId();
        BigDecimal money = withdrawOrder.getWithdrawMoney();
        PlatformConfig first = platformConfigService.findFirst();
        if (status == Constants.WITHDRAW_PASS) {// 通过提现审核的计算手续费
            if (first != null) {
                // 得到手续费
                BigDecimal serviceCharge = first.getWithdrawServiceCharge(withdrawOrder.getWithdrawMoney());
                BigDecimal practicalAmount = withdrawOrder.getWithdrawMoney().subtract(serviceCharge);
                withdrawOrder.setServiceCharge(serviceCharge);
                withdrawOrder.setPracticalAmount(practicalAmount);
            } else {
                withdrawOrder.setServiceCharge(BigDecimal.ZERO);
                withdrawOrder.setPracticalAmount(money);
            }
            withdrawOrder.setStatus(status);
            withdrawOrderService.saveOrder(withdrawOrder);
            log.info("通过提现userId {} 订单号 {} withdrawMoney is {}, practicalAmount is {}", withdrawOrder.getUserId(),
                withdrawOrder.getNo(), money, withdrawOrder.getWithdrawMoney());
            return ResponseUtil.success(withdrawOrder.getPracticalAmount());
        }
        // 对用户数据进行行锁
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        withdrawOrder.setStatus(status);
        withdrawOrderService.saveOrder(withdrawOrder);
        BigDecimal amountBefore = userMoney.getMoney();
        userMoney.setMoney(userMoney.getMoney().add(money));
        userMoneyService.save(userMoney);

        // 记录用户账变
        this.saveAccountChang(AccountChangeEnum.WITHDRAWDEFEATED_CODE, userMoney.getUserId(), withdrawOrder,
            amountBefore, userMoney.getMoney());
        log.info("拒绝提现userId {} 订单号 {} withdrawMoney is {}, money is {}", userMoney.getUserId(), withdrawOrder.getNo(),
            money, userMoney.getMoney());
        return ResponseUtil.success();
    }

    // private void saveAccountChang(AccountChangeEnum changeEnum, Long userId, BigDecimal amount, BigDecimal
    // amountBefore,
    // BigDecimal amountAfter,String orderNo){
    // AccountChangeVo vo=new AccountChangeVo();
    // vo.setUserId(userId);
    // vo.setOrderNo(orderNo);
    // vo.setChangeEnum(changeEnum);
    // vo.setAmount(amount);
    // vo.setAmountBefore(amountBefore);
    // vo.setAmountAfter(amountAfter);
    // asyncService.executeAsync(vo);
    // }
    private void saveAccountChang(AccountChangeEnum changeEnum, Long userId, WithdrawOrder withdrawOrder,
        BigDecimal amountBefore, BigDecimal amountAfter) {
        AccountChange change = new AccountChange();
        change.setUserId(userId);
        change.setOrderNo(getOrderNo(changeEnum));
        change.setType(changeEnum.getType());
        change.setAmount(withdrawOrder.getWithdrawMoney());
        change.setAmountBefore(amountBefore);
        change.setAmountAfter(amountAfter);
        change.setFirstProxy(withdrawOrder.getFirstProxy());
        change.setSecondProxy(withdrawOrder.getSecondProxy());
        change.setThirdProxy(withdrawOrder.getThirdProxy());
        accountChangeService.save(change);
    }

    public String getOrderNo(AccountChangeEnum changeEnum) {
        String orderNo = changeEnum.getCode();
        String today = DateUtil.today("yyyyMMddHHmmssSSS");
        String randNum = CommonUtil.random(3);
        orderNo = orderNo + today + randNum;
        return orderNo;
    }

    @Transactional
    public void save(User user) {
        userService.save(user);
    }
}
