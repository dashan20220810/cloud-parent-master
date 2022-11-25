package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.TotalPlatformQuotaRecord;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.TotalPlatformQuotaRecordService;
import com.qianyi.casinocore.util.TaskConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TotalPlatformQuotaRecordTask {


    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private TotalPlatformQuotaRecordService totalPlatformQuotaRecordService;

    @Scheduled(cron = TaskConst.TOTAL_PLATFORM_QUOTA_TASK)
    public void create(){
        Date date=new Date();
        log.info("每12小时记录平台总额度开始start=============================================》");
        PlatformConfig first = platformConfigService.findFirst();
        if(first!=null && first.getTotalPlatformQuota()!=null){
            BigDecimal totalPlatformQuota = first.getTotalPlatformQuota();
            TotalPlatformQuotaRecord totalPlatformQuotaRecord=new TotalPlatformQuotaRecord();
            totalPlatformQuotaRecord.setTotalPlatformQuota(totalPlatformQuota);
            totalPlatformQuotaRecord.setTime(date);
            totalPlatformQuotaRecordService.save(totalPlatformQuotaRecord);
        }else {
            log.info("平台总额度为空=============================================》");
        }
        log.info("每12小时记录平台总额度结束end=============================================》");
    }
}
