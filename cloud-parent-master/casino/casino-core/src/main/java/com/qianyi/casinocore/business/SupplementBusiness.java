package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.repository.ErrorOrderRepository;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinocore.vo.WmMemberTradeReportVo;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.executor.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class SupplementBusiness {

    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private ErrorOrderRepository errorOrderRepository;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;

    /**
     * 尝试3次补单
     *
     * @param errorOrder
     * @param thirdAccount
     */
    public void trySupplement(ErrorOrder errorOrder, String thirdAccount) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
                Thread.sleep(30 * 1000);
                PublicWMApi.ResponseEntity entity = wmApi.getMemberTradeReport(thirdAccount, null, orderNo, null, null, null);
                if (entity == null) {
                    log.error("查询WM交易记录时远程请求异常");
                    continue;
                }
                if (entity.getErrorCode() == 107) {
                    log.info("订单号:{}查询无记录", orderNo);
                    //转入wm时，wm查询无记录说明wm加点失败，要把本地的钱加回来，
                    if (errorOrder.getType() == AccountChangeEnum.WM_IN.getType()) {
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + errorOrder.getMoney() + ",转入WM时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), errorOrder.getMoney());
                            //记录账变
                            saveAccountChange(errorOrder, errorOrder.getMoney());
                        }
                    } else if (errorOrder.getType() == AccountChangeEnum.RECOVERY.getType()) {
                        //转出wm时，是先扣减wm的钱再加回本地，wm查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出WM时扣点失败,额度未丢失");
                    }
                    break;
                }
                if (entity.getErrorCode() != 0) {
                    log.info("订单号:{}查询交易记录异常,result={}", orderNo, entity.toString());
                    continue;
                }
                if (entity.getErrorCode() == 0) {
                    String result = entity.getResult().toString();
                    List<WmMemberTradeReportVo> records = JSON.parseArray(result, WmMemberTradeReportVo.class);
                    for (WmMemberTradeReportVo vo : records) {
                        if (thirdAccount.equals(vo.getUser()) && orderNo.equals(vo.getOrdernum())) {
                            //转入wm,本地先扣减，确认三方加点成功无需加回本地余额
                            if (vo.getOp_code() == 121 && errorOrder.getType() == AccountChangeEnum.WM_IN.getType()) {
                                updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转入WM时加点成功,额度未丢失");
                                //转出WM时，三方先扣减，确认三方扣点成功加回本地余额
                            } else if (vo.getOp_code() == 122 && errorOrder.getType() == AccountChangeEnum.RECOVERY.getType()) {
                                //以WM额度为准
                                BigDecimal money = new BigDecimal(vo.getMoney()).abs();
                                //更新错误订单表状态
                                Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money + ",转出WM时扣点成功,加回本地额度");
                                if (count > 0) {
                                    //加回额度
                                    addMoney(errorOrder.getUserId(), money);
                                    //记录账变
                                    saveAccountChange(errorOrder, money);
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询WM交易记录时异常,msg={}", e.getMessage());
            }
        }
    }

    public void addMoney(Long userId, BigDecimal money) {
        userMoneyService.addMoney(userId, money);
        log.info("订单自动补单成功,userMoney表金额补偿成功,userId={},money={}", userId, money);
    }

    public Integer updateErrorOrderStatus(ErrorOrder errorOrder, String remark) {
        //调save方法更新无效，所以自定义方法
        Integer count = errorOrderRepository.updateErrorStatusRemark(1, remark, errorOrder.getId());
        log.info("订单自动补单成功,errorOrder表更新成功,errorOrder={}", errorOrder.toString());
        return count;
    }

    public void saveAccountChange(ErrorOrder errorOrder, BigDecimal money) {
        //账变中心记录账变
        UserMoney userMoney = userMoneyService.findByUserId(errorOrder.getUserId());
        AccountChangeVo accountChangeVo = new AccountChangeVo();
        accountChangeVo.setUserId(errorOrder.getUserId());
        accountChangeVo.setChangeEnum(AccountChangeEnum.SYSTEM_UPP);
        accountChangeVo.setAmount(money);
        accountChangeVo.setAmountAfter(userMoney.getMoney());
        accountChangeVo.setAmountBefore(userMoney.getMoney().subtract(money));
        accountChangeVo.setOrderNo(errorOrder.getOrderNo());
        asyncService.executeAsync(accountChangeVo);
        log.info("订单自动补单成功,AccountChange表账变记录成功,AccountChange={}", accountChangeVo.toString());
    }
}
