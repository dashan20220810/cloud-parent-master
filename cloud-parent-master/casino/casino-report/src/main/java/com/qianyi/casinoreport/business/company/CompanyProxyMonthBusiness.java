package com.qianyi.casinoreport.business.company;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.BTimeUtil;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import com.qianyi.modulecommon.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 注释
 *
 * @author lance
 * @since 2022 -02-26 11:44:37
 */
@Slf4j
@Service
public class CompanyProxyMonthBusiness {

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private CompanyLevelProcessBusiness companyLevelProcessBusiness;

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ExtractPointsChangeService extractPointsChangeService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;


    @Autowired
    private MessageUtil messageUtil;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;


    //传入计算当天的时间  yyyy-MM-dd 格式
    @Transactional
    public void processMonthReport(String dayTime){
        String startTime = BTimeUtil.getStartTime(dayTime);
        String endTime = BTimeUtil.getEndTime(dayTime);
        //删除月
        companyProxyMonthService.deleteAllMonth(BTimeUtil.getMonthTime(dayTime));
        log.info("processDailyReport start startTime:{} endTime:{}",startTime,endTime);

        //根据不用的游戏拿取不同游戏的返佣比列进行计算

        //查询当天游戏记录(third_proxy is not null)
        List<CompanyOrderAmountVo> companyOrderAmountVoList = gameRecordService.getStatisticsResult(startTime,endTime);
        List<CompanyProxyMonth> firstList= new ArrayList<>();
        List<CompanyProxyMonth> secondeList= new ArrayList<>();
        List<CompanyProxyMonth> thirdList= new ArrayList<>();
        companyOrderAmountVoList.forEach(item->processOrder(item,firstList,secondeList,thirdList));

        //查询电子游戏数据
        List<CompanyProxyMonth> firstList1= new ArrayList<>();
        List<CompanyProxyMonth> secondeList1= new ArrayList<>();
        List<CompanyProxyMonth> thirdList1= new ArrayList<>();
        List<CompanyOrderAmountVo> statisticsResult = gameRecordGoldenFService.getStatisticsResult(startTime, endTime);
        statisticsResult.forEach(item->processOrder(item,firstList1,secondeList1,thirdList1));

        //查询电子游戏数据
        List<CompanyProxyMonth> obdjFirstList= new ArrayList<>();
        List<CompanyProxyMonth> obdjSecondeList = new ArrayList<>();
        List<CompanyProxyMonth> obdjThirdList = new ArrayList<>();
        List<CompanyOrderAmountVo> obdjResult = gameRecordObdjService.getStatisticsResult(startTime, endTime);
        obdjResult.forEach(item->processOrder(item,obdjFirstList,obdjSecondeList,obdjThirdList));

        //查询电子游戏数据
        List<CompanyProxyMonth> obtyFirstList= new ArrayList<>();
        List<CompanyProxyMonth> obtySecondeList = new ArrayList<>();
        List<CompanyProxyMonth> obtyThirdList = new ArrayList<>();
        List<CompanyOrderAmountVo> obtyResult = gameRecordObtyService.getStatisticsResult(startTime, endTime);
        obtyResult.forEach(item->processOrder(item,obtyFirstList,obtySecondeList,obtyThirdList));


        if(firstList.size() == 0 && firstList1.size()==0) {
            log.info("first level is no user");
            return;
        }
        firstList.addAll(firstList1);
        secondeList.addAll(secondeList1);
        thirdList.addAll(thirdList1);

        List<CompanyProxyMonth> resultList=new ArrayList<>();
        resultList.addAll(firstList);
        resultList.addAll(secondeList);
        resultList.addAll(thirdList);
       /* List<CompanyProxyMonth> resultList = processingData(firstList, secondeList, thirdList);


*/
        resultList=proxyList(resultList);
        log.info("save all proxyDetail data");
        log.info("resultList:{}",resultList);
        companyProxyMonthService.saveAll(resultList);
        log.info("save all proxyDetail data finish");
    }


    /**
     * 更新 CompanyProxyMonthReport 的额外信息
     *
     * @param dayTime 入参释义
     * @author lance
     * @since 2022 -02-26 11:44:37
     */
    @Transactional
    public void updateCompanyProxyMonthReport(String dayTime){
        String startTime = BTimeUtil.getStartTime(dayTime);
        String endTime = BTimeUtil.getEndTime(dayTime);
        String monthTime = BTimeUtil.getMonthTime(dayTime);

        /*----------------- 更新抽点数据 **/

        // 抽点记录
        List<ExtractPointsChange> changes = extractPointsChangeService.findBetween(startTime, endTime);
        Map<Long, List<ExtractPointsChange>> changeGroup = changes.stream().collect(Collectors.groupingBy(ExtractPointsChange::getPoxyId));

        List<CompanyProxyMonth> thirdResultList = companyProxyMonthService.findAllByStaticsTimesAndProxyRole(monthTime, 3);

        // 只有基层代有抽水
        thirdResultList.forEach(res ->{
            List<ExtractPointsChange> group = changeGroup.get(res.getUserId());
            if (null != group) {
                BigDecimal water = group.stream().map(ExtractPointsChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                // 设置抽点总额
                res.setExtractPointsAmount(water);
            }
        });

        /*----------------- 更新抽点数据end **/

        companyProxyMonthService.saveAll(thirdResultList);
    }


    /**
     * 合并代理数据
     * @param proxyInfoList
     * @return
     */
    private List<CompanyProxyMonth> proxyList(List<CompanyProxyMonth>  proxyInfoList){
        Map<Long, CompanyProxyMonth> proxyMap = new HashMap<>();
        proxyInfoList.forEach(info ->{
            //判断代理id是否一直
            CompanyProxyMonth last = proxyMap.get(info.getUserId());
            if (null !=last ){
                //用户id一致 ? playerNum : playerNum+1
                if (last.getUserIdTemp().equals(info.getUserIdTemp())){
                    info.setPlayerNum(info.getPlayerNum());
                }else {
                    info.setPlayerNum(info.getPlayerNum()+last.getPlayerNum());
                }
                info.setGroupBetAmount(info.getGroupBetAmount().add(last.getGroupBetAmount()));
                info.setGroupTotalprofit(info.getGroupTotalprofit().add(last.getGroupTotalprofit()));
                info.setProfitAmount(info.getProfitAmount().add(last.getProfitAmount()));
                proxyMap.put(info.getUserId(),info);
            }else {
                proxyMap.put(info.getUserId(),info);
            }
        });
       return proxyMap.values().stream().collect(Collectors.toList());
    }


    //处理总返佣
    private List<CompanyProxyMonth>  processingData (List<CompanyProxyMonth> firstList,  List<CompanyProxyMonth> secondeList,  List<CompanyProxyMonth> thirdList){
        log.info("firstList size is {}, secondeList size is {}, thirdList size is {}",firstList.size(),secondeList.size(),thirdList.size());
        // 处理总返佣：总返佣group_totalprofit = 下级profit_amount总计
        List<CompanyProxyMonth> secondeCompanyProxyDetail = processSec(secondeList,thirdList,2);
        List<CompanyProxyMonth> firstCompanyProxyDetail = processSec(firstList,secondeCompanyProxyDetail,1);

        List<CompanyProxyMonth> resultList = Stream.concat(thirdList.stream(),secondeCompanyProxyDetail.stream()).collect(Collectors.toList());
        resultList.addAll(firstCompanyProxyDetail);
        return resultList;
    }

    private List<CompanyProxyMonth> processSec(List<CompanyProxyMonth> firstList,List<CompanyProxyMonth> secList,int level) {
        List<CompanyProxyMonth> companyProxyDetailList = new ArrayList<>();
        Map<Long,List<CompanyProxyMonth>> groupSec = firstList.stream().collect(Collectors.groupingBy(CompanyProxyMonth::getUserId));
        List<CompanyProxyMonth> subThird = new ArrayList<>();
        for (Long userId : groupSec.keySet()) {
            List<CompanyProxyMonth> subList = groupSec.get(userId);
            if(level==2)
                subThird = secList.stream().filter(x->x.getSecondProxy().equals(userId)).collect(Collectors.toList());
            else
                subThird = secList.stream().filter(x->x.getFirstProxy().equals(userId)).collect(Collectors.toList());


            if(userId == 4 && level== 1){
                log.info("++++++++++++");
                log.info("subList is {}",subList);
                log.info("subThird is {}",subThird);
                log.info("++++++++++++");
            }

            CompanyProxyMonth secondeItem = processfirst(subList,subThird);
            companyProxyDetailList.add(secondeItem);
        }

        return companyProxyDetailList;
    }

    private CompanyProxyMonth processfirst(List<CompanyProxyMonth> firstList,List<CompanyProxyMonth> subList) {
        BigDecimal groupBetAmount = firstList.stream().map(x->x.getGroupBetAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal profitAmount = firstList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal groupTotalProfit = subList.stream().map(x->x.getGroupTotalprofit()).reduce(BigDecimal.ZERO,BigDecimal::add);
        Integer playnums = subList.stream().map(x-> x.getPlayerNum()).reduce(0,Integer::sum);


        CompanyProxyMonth item = firstList.get(0);

        CompanyProxyMonth actItem = (CompanyProxyMonth) item.clone();
        actItem.setGroupBetAmount(groupBetAmount);
        actItem.setProfitAmount(profitAmount);
        actItem.setGroupTotalprofit(groupTotalProfit);
        actItem.setPlayerNum(playnums);
        return actItem;
    }


    /**
     * 月结表：分别计算各级代理的分佣
     * @param companyOrderAmountVo
     * @param firstList
     * @param secondeList
     * @param thirdList
     */
    private void processOrder(CompanyOrderAmountVo companyOrderAmountVo,List<CompanyProxyMonth> firstList,List<CompanyProxyMonth> secondeList,List<CompanyProxyMonth> thirdList){
        //根据投注金额匹配推广返佣配置中对应的返佣比列
        CompanyLevelBO companyLevelBO = companyLevelProcessBusiness.getLevelData(new BigDecimal(companyOrderAmountVo.getValidbet()),companyOrderAmountVo.getFirstProxy(),companyOrderAmountVo.getGameType());
        //根据基层代查询代理佣金配置表
        ProxyCommission proxyCommission = proxyCommissionService.findByProxyUserId(companyOrderAmountVo.getThirdProxy());
        if(proxyCommission==null){
            log.info("proxyCommission代理佣金配置表为null,基层贷id:{}",companyOrderAmountVo.getThirdProxy());
            return;
        }
        log.info("companyLevelBO:{}",companyLevelBO);
        log.info("proxyCommission:{}",proxyCommission);


        //组装各级代理层数据
        firstList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getFirstProxy(),proxyCommission.getFirstCommission(),1));
        secondeList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getSecondProxy(),proxyCommission.getSecondCommission(),2));
        thirdList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getThirdProxy(),proxyCommission.getThirdCommission(),3));
    }

    private CompanyProxyMonth calculateDetail(CompanyLevelBO companyLevelBO,CompanyOrderAmountVo companyOrderAmountVo,Long userid,BigDecimal profitRate,Integer proxyType){
        log.info("companyLevelBO:{}",companyLevelBO);
        //个人佣金结算:返佣金额(如:达到1w返佣10元) * 实际倍数(下注金额/配置得返佣金额线)
        BigDecimal totalAmount = companyLevelBO.getProfitAmount().multiply(BigDecimal.valueOf(companyLevelBO.getProfitActTimes()));
        CompanyProxyMonth companyProxyMonth= CompanyProxyMonth.builder()
                .benefitRate(profitRate)
                .firstProxy(companyOrderAmountVo.getFirstProxy())
                .secondProxy(companyOrderAmountVo.getSecondProxy())
                .thirdProxy(companyOrderAmountVo.getThirdProxy())
                .proxyRole(proxyType)
                .userId(userid)
                .userIdTemp(companyOrderAmountVo.getUserId())
                .groupBetAmount(new BigDecimal(companyOrderAmountVo.getValidbet()))
                .playerNum(companyOrderAmountVo.getPlayerNum())
                // 返佣金额(如:达到1w返佣10元) * 实际倍数(下注金额/10000) * 代理佣金配置值
                .profitAmount(totalAmount.multiply(profitRate))
//                .groupTotalprofit(proxyType==3? totalAmount:BigDecimal.ZERO)
                .groupTotalprofit(totalAmount)
                .settleStatus(0)
                .staticsTimes(companyOrderAmountVo.getBetTime().substring(0,7))
//                .betTime(LocalDateTime.parse(companyOrderAmountVo.getBetTime().replace(' ','T')))
                .build();
        log.info("companyProxyMonth:{}",companyProxyMonth);
        return companyProxyMonth;
    }

    /**
     * 根据返佣金额查询当前返佣级别
     * @return
     */
    private String queryRebateLevel(String profitAmount,Long proxyUserId){
        //ProxyUser proxyUser = proxyUserService.findById(proxyUserId);
        //查询父级
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findByProxyUserIdAndGameType(proxyUserId,null);
        RebateConfig rebateConfig = rebateConfigService.findFirst();
        String profit= profitAmount;
        //L1
        Integer firstMoney=proxyRebateConfig!=null ?proxyRebateConfig.getFirstMoney() :rebateConfig.getFirstMoney();
        if(profit.equals(String.valueOf(firstMoney))){
            return CommonConst.REBATE_LEVEL_1;
        }
        Integer secondMoney=proxyRebateConfig!=null ?proxyRebateConfig.getSecondMoney() :rebateConfig.getSecondMoney();
        if(profit.equals(String.valueOf(secondMoney))){
            return CommonConst.REBATE_LEVEL_2;
        }
        Integer thirdMoney=proxyRebateConfig!=null ?proxyRebateConfig.getThirdMoney() :rebateConfig.getThirdMoney();
        if(profit.equals(String.valueOf(thirdMoney))){
            return CommonConst.REBATE_LEVEL_3;
        }

        Integer fourMoney=proxyRebateConfig!=null ?proxyRebateConfig.getFourMoney() :rebateConfig.getFourMoney();
        if(profit.equals(String.valueOf(fourMoney))){
            return CommonConst.REBATE_LEVEL_4;
        }
        Integer fiveMoney=proxyRebateConfig!=null ?proxyRebateConfig.getFiveMoney() :rebateConfig.getFiveMoney();
        if(profit.equals(String.valueOf(fiveMoney))){
            return CommonConst.REBATE_LEVEL_5;
        }
        Integer sixMoney=proxyRebateConfig!=null ?proxyRebateConfig.getSixMoney() :rebateConfig.getSixMoney();
        if(profit.equals(String.valueOf(sixMoney))){
            return CommonConst.REBATE_LEVEL_6;
        }
        Integer sevenMoney=proxyRebateConfig!=null ?proxyRebateConfig.getSevenMoney() :rebateConfig.getSevenMoney();
        if(profit.equals(String.valueOf(sevenMoney))){
            return CommonConst.REBATE_LEVEL_7;
        }
        Integer eightMoney=proxyRebateConfig!=null ?proxyRebateConfig.getEightMoney() :rebateConfig.getEightMoney();
        if(profit.equals(String.valueOf(eightMoney))){
            return CommonConst.REBATE_LEVEL_8;
        }
        String rebateLevel=messageUtil.get(CommonConst.REBATE_LEVEL);
        return rebateLevel;
    }


    ////////////////////////////////////////////////////////////////////////////

    //传入计算当天的时间  yyyy-MM-dd 格式
//    @Transactional
//    public void processCompanyMonth(String dayTime){
//        String starTime = getStartTime(dayTime);
//        String endTime = getEndTime(dayTime);
//
//        //删除当月的数据
//        companyProxyMonthService.deleteAllMonth(getMonthTime(dayTime));
//        //统计当月的数据
//        List<CompanyProxyMonth> companyProxyMonthList = companyProxyMonthService.queryMonthByDay(starTime,endTime);
//        //插入当月的数据
//        companyProxyMonthService.saveAll(companyProxyMonthList);
//    }
}
