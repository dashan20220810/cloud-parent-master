package com.qianyi.casinocore.job;

import com.qianyi.casinocore.business.AccountChangeBusiness;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.modulecommon.executor.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 账变中心处理线程
 */
@Component
public class AccountChangeJob implements AsyncService<AccountChangeVo> {

    @Autowired
    AccountChangeBusiness accountChangeBusiness;

    @Override
    public void executeAsync(AccountChangeVo vo) {
        accountChangeBusiness.save(vo);
    }
}
