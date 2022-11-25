package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChargeBusiness {

    @Autowired
    private CollectionBankcardService collectionBankcardService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private BankcardsService bankcardsService;

    public List<CollectionBankcard> getCollectionBankcards(){
        return collectionBankcardService.getCollectionBandcards();
    }


    public ResponseEntity submitOrder(String chargeAmount,Integer remitType,String remitterName,Long bankcardId,Long userId){
        if (ObjectUtils.isEmpty(chargeAmount)) {
            return ResponseUtil.custom("充值金额不允许为空");
        }
        if (remitType != 1 && remitType != 2 && remitType != 3) {
            return ResponseUtil.custom("汇款方式填写错误");
        }
        if (!remitterName.matches(RegexEnum.NAME.getRegex())) {
            return ResponseUtil.custom("汇款人" + RegexEnum.NAME.getDesc());
        }
        CollectionBankcard bankcard = collectionBankcardService.findById(bankcardId);
        if (bankcard == null) {
            return ResponseUtil.custom("收款银行卡不存在");
        }
        if (bankcard.getDisable() != null && bankcard.getDisable() == 1) {
            return ResponseUtil.custom("当前收款银行卡已被禁用,请重新选择");
        }
        Integer count = chargeOrderService.countByUserIdAndStatus(userId,0);
        if (count > 0) {
            return ResponseUtil.custom("您有一笔充值订单正在审核,无法再次提交");
        }
        BigDecimal decChargeAmount = new BigDecimal(chargeAmount);
        //查询充值金额限制
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig != null) {
            BigDecimal minMoney = platformConfig.getChargeMinMoney();
            BigDecimal maxMoney = platformConfig.getChargeMaxMoney();
            if (minMoney != null && decChargeAmount.compareTo(minMoney) == -1) {
                return ResponseUtil.custom("充值金额小于最低限额,单笔最低限额为",minMoney.stripTrailingZeros().toPlainString());
            }
            if (maxMoney != null && decChargeAmount.compareTo(maxMoney) == 1) {
                return ResponseUtil.custom("充值金额大于最高限额,单笔最高限额为", maxMoney.stripTrailingZeros().toPlainString());
            }
        }
        ChargeOrder chargeOrder = getChargeOrder(decChargeAmount,remitType,remitterName,bankcardId,userId);
        ChargeOrder saveOrder = chargeOrderService.saveOrder(chargeOrder);
        return ResponseUtil.success(saveOrder);
    }

    private ChargeOrder getChargeOrder(BigDecimal chargeAmount,Integer remitType,String remitterName,Long bankcardId,Long userId){
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setOrderNo(orderService.getOrderNo());
        chargeOrder.setChargeAmount(chargeAmount);
        chargeOrder.setStatus(0);
        chargeOrder.setRemitter(remitterName);
        chargeOrder.setUserId(userId);
        chargeOrder.setRemitType(remitType);
        chargeOrder.setRemark("");
        chargeOrder.setBankcardId(bankcardId);
        //查询打码倍率
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig != null) {
            chargeOrder.setBetRate(platformConfig.getBetRate());
        }
        User user = userService.findById(userId);
        if (user != null) {
            chargeOrder.setFirstProxy(user.getFirstProxy());
            chargeOrder.setSecondProxy(user.getSecondProxy());
            chargeOrder.setThirdProxy(user.getThirdProxy());
            chargeOrder.setType(user.getType());
        }
        return chargeOrder;
    }
    /**
     * 管理后台充值账变
     */
    @Transactional
    public ResponseEntity updateChargeOrderAndUser(ChargeOrder chargeOrder) {
        Long userId = chargeOrder.getUserId();
        User user = userService.findUserByIdUseLock(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if(userMoney == null){
            return ResponseUtil.custom("用户额度表不存在");
        }
        log.info("账变开始orderNo is {},chargeAmount is {} userMoney is {}",chargeOrder.getOrderNo(),chargeOrder.getChargeAmount(),userMoney.getMoney());
        BigDecimal money = userMoney.getMoney() == null ? BigDecimal.ZERO : userMoney.getMoney();
        userMoney.setMoney(money);
        userMoney.setMoney(userMoney.getMoney().add(chargeOrder.getChargeAmount()));
        chargeOrderService.saveOrder(chargeOrder);
        userMoneyService.save(userMoney);
        log.info("账变结束orderNo is {},money is {}",chargeOrder.getOrderNo(),userMoney.getMoney());
        return ResponseUtil.success(chargeOrder);
    }
    /**
     * 管理后台定时清除超时充值订单
     */
    @Transactional
    public void updateChargeOrderStatus(Long id){
        ChargeOrder chargeOrder = chargeOrderService.findChargeOrderByIdUseLock(id);
        if (chargeOrder !=null && chargeOrder.getStatus()!=0)
            return;
        chargeOrder.setStatus(3);
        chargeOrderService.saveOrder(chargeOrder);
    }

}
