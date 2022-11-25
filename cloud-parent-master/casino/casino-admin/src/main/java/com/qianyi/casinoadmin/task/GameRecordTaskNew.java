package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.util.TaskConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Slf4j
@Component
public class GameRecordTaskNew {

    @Autowired
    private GameRecordReportNewService gameRecordReportService;

    @Autowired
    private GameRecordService gameRecordService;

    @Scheduled(cron = TaskConst.GAMERECORD_TASK_NEW)
    public void create(){
        log.info("每小时报表统计开始start=============================================》");
        gameRecordReportService.saveGameRecordReportWM();
        gameRecordReportService.saveGameRecordReportPG();
        gameRecordReportService.saveGameRecordReportCQ9();
        gameRecordReportService.saveGameRecordReportOBDJ();
        gameRecordReportService.saveGameRecordReportOBTY();
        gameRecordReportService.saveGameRecordReportSABASPORT();
        log.info("每小时报表统计结束end=============================================》");
    }
}
