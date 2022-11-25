package com.qianyi.casinoreport.business.company;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CompanyProxyDailyBusiness {

    @Autowired
    private CompanyProxyDetailService companyProxyDetailService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private CompanyLevelProcessBusiness companyLevelProcessBusiness;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    //传入计算当天的时间  yyyy-MM-dd 格式
    @Transactional
    public void processDailyReport(String dayTime){
        String startTime = getStartTime(dayTime);
        String endTime = getEndTime(dayTime);
        //删除天
        companyProxyDetailService.deleteByDayTime(startTime.substring(0,10));
        log.info("processDailyReport start startTime:{} endTime:{}",startTime,endTime);
        //查询当天游戏记录(third_proxy is not null)
        List<CompanyOrderAmountVo> companyOrderAmountVoList = gameRecordService.getStatisticsResult(startTime,endTime);
        if(companyOrderAmountVoList.size()>0){
            processingData(companyOrderAmountVoList);
        }
        //查询电子游戏数据
        List<CompanyOrderAmountVo> statisticsResult = gameRecordGoldenFService.getStatisticsResult(startTime, endTime);
        if(statisticsResult.size()>0){
            processingData(statisticsResult);
        }

        //查询电子游戏数据
        List<CompanyOrderAmountVo> obdjStatisticsResult = gameRecordObdjService.getStatisticsResult(startTime, endTime);
        if(obdjStatisticsResult.size()>0){
            processingData(obdjStatisticsResult);
        }

        //查询电子游戏数据
        List<CompanyOrderAmountVo> obTYStatisticsResult = gameRecordObtyService.getStatisticsResult(startTime, endTime);
        if(obTYStatisticsResult.size()>0){
            processingData(obTYStatisticsResult);
        }

        if(companyOrderAmountVoList.size() == 0 && statisticsResult.size()==0) {
            log.info("first level is no user");
            return;
        }


    }

    public void  processingData (List<CompanyOrderAmountVo> companyOrderAmountVoList){
        List<CompanyProxyDetail> firstList= new ArrayList<>();
        List<CompanyProxyDetail> secondeList= new ArrayList<>();
        List<CompanyProxyDetail> thirdList= new ArrayList<>();

        companyOrderAmountVoList.forEach(item->processOrder(item,firstList,secondeList,thirdList));

        if(firstList.size() == 0) {
            log.info("first level is no user");
            return;
        }

        log.info("firstList size is {}, secondeList size is {}, thirdList size is {}",firstList.size(),secondeList.size(),thirdList.size());

        // 处理总返佣：总返佣group_totalprofit = 下级profit_amount总计
        List<CompanyProxyDetail> secondeCompanyProxyDetail = processSecTemp(secondeList,thirdList,2);
        List<CompanyProxyDetail> firstCompanyProxyDetail = processSecTemp(firstList,secondeCompanyProxyDetail,1);

        List<CompanyProxyDetail> resultList = Stream.concat(thirdList.stream(),secondeCompanyProxyDetail.stream()).collect(Collectors.toList());
        resultList.addAll(firstCompanyProxyDetail);

        log.info("save all proxyDetail data");
        log.info("resultList:{}",resultList);
        companyProxyDetailService.saveAll(resultList);
        log.info("save all proxyDetail data finish");
    }
    private List<CompanyProxyDetail> processSecTemp(List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> secList,int level) {
        Map<Long,List<CompanyProxyDetail>> firstProxy=new HashMap<>();
        if (level==2){
            firstProxy = secList.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getSecondProxy));
        }else {
            firstProxy = secList.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getFirstProxy));
        }
        Map<Long, List<CompanyProxyDetail>> finalFirstProxy = firstProxy;
        firstList.forEach(info ->{
            List<CompanyProxyDetail> subList = finalFirstProxy.get(info.getUserId());
            if (subList!=null){
                if (subList!=null){
                    //处理总返佣：总返佣group_totalprofit = 下级profit_amount总计
                    info.setGroupTotalprofit(subList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add));
                }
            }
        });
        return firstList;
    }

    private List<CompanyProxyDetail> processSec(List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> secList,int level) {
        List<CompanyProxyDetail> companyProxyDetailList = new ArrayList<>();
        Map<Long,List<CompanyProxyDetail>> groupSec = firstList.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getUserId));
        List<CompanyProxyDetail> subThird = new ArrayList<>();
        for (Long userId : groupSec.keySet()) {
            List<CompanyProxyDetail> subList = groupSec.get(userId);
            if(level==2)
                subThird = secList.stream().filter(x->x.getSecondProxy()==userId).collect(Collectors.toList());
            else
                subThird = secList.stream().filter(x->x.getFirstProxy()==userId).collect(Collectors.toList());

            CompanyProxyDetail secondeItem = processfirst(subList,subThird);
            companyProxyDetailList.add(secondeItem);
        }

        return companyProxyDetailList;
    }

    private CompanyProxyDetail processfirst(List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> subList) {
        BigDecimal groupBetAmount = firstList.stream().map(x->x.getGroupBetAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal profitAmount = firstList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal groupTotalProfit = subList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);

        CompanyProxyDetail item = firstList.get(0);

        CompanyProxyDetail actItem = (CompanyProxyDetail) item.clone();
        actItem.setGroupBetAmount(groupBetAmount);
        actItem.setProfitAmount(profitAmount);
        actItem.setGroupTotalprofit(groupTotalProfit);
        return actItem;
    }


    /**
     * 分别计算各级代理的分佣
     * @param companyOrderAmountVo
     * @param firstList
     * @param secondeList
     * @param thirdList
     */
    public void processOrder(CompanyOrderAmountVo companyOrderAmountVo,List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> secondeList,List<CompanyProxyDetail> thirdList){
        //返佣比例
        CompanyLevelBO companyLevelBO = companyLevelProcessBusiness.getLevelData(new BigDecimal(companyOrderAmountVo.getValidbet()),companyOrderAmountVo.getFirstProxy(),companyOrderAmountVo.getGameType());
        //根据基层代查询代理佣金配置表
        ProxyCommission proxyCommission = proxyCommissionService.findByProxyUserId(companyOrderAmountVo.getThirdProxy());

        if(proxyCommission==null){
            log.info("proxyCommission代理佣金配置表为null,基层贷id:{}",companyOrderAmountVo.getThirdProxy());
            return;
        }
        log.info("companyLevelBO:{}",companyLevelBO);
        log.info("proxyCommission:{}",proxyCommission);


        firstList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getFirstProxy(),proxyCommission.getFirstCommission(),1));
        secondeList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getSecondProxy(),proxyCommission.getSecondCommission(),2));
        thirdList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getThirdProxy(),proxyCommission.getThirdCommission(),3));
    }

    public CompanyProxyDetail calculateDetail(CompanyLevelBO companyLevelBO,CompanyOrderAmountVo companyOrderAmountVo,Long userid,BigDecimal profitRate,Integer proxyType){
        log.info("companyLevelBO:{}",companyLevelBO);
        //个人佣金结算:返佣金额(如:达到1w返佣10元) * 实际倍数(下注金额/配置得返佣金额线) * 代理佣金配置值
        BigDecimal totalAmount = companyLevelBO.getProfitAmount().multiply(BigDecimal.valueOf(companyLevelBO.getProfitActTimes()));
        CompanyProxyDetail companyProxyDetail= CompanyProxyDetail.builder()
                .benefitRate(profitRate)
                .firstProxy(companyOrderAmountVo.getFirstProxy())
                .secondProxy(companyOrderAmountVo.getSecondProxy())
                .thirdProxy(companyOrderAmountVo.getThirdProxy())
                .proxyRole(proxyType)
                .userId(userid)
                .gameType(companyOrderAmountVo.getGameType())
                .profitLevel(companyLevelBO.getProfitLevel()+"")
                .profitRate(companyLevelBO.getProfitAmount().toString())
                .profitAmountLine(companyLevelBO.getProfitAmountLine().toString())
                .groupBetAmount(new BigDecimal(companyOrderAmountVo.getValidbet()))
                .playerNum(companyOrderAmountVo.getPlayerNum())
                .profitAmount(totalAmount.multiply(profitRate))
                .groupTotalprofit(BigDecimal.ZERO)
                .settleStatus(0)
                .staticsTimes(companyOrderAmountVo.getBetTime().substring(0,10))
                .betTime(LocalDateTime.parse(companyOrderAmountVo.getBetTime().replace(' ','T')))
                .build();
        log.info("companyProxyDetail:{}",companyProxyDetail);
        return companyProxyDetail;
    }


    public String getStartTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(dayTime+"T00:00:00");
        return df.format(startTime.plusDays(-1));
    }

    public String getEndTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endTime = LocalDateTime.parse(dayTime+"T00:00:00");
        endTime=endTime.plusSeconds(-1);
        return df.format(endTime);
    }


}
