package com.qianyi.casinoweb.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.WashCodeVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.*;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("washCode")
@Api(tags = "洗码")
public class WashCodeController {

    @Autowired
    private WashCodeConfigService washCodeConfigService;
    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

    @ApiOperation("用户洗码列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "时间：0：今天，1：昨天，2：近7天", required = true)
    })
    @GetMapping("/getList")
    public ResponseEntity<ChargeOrderListData> chargeOrderList(String date, HttpServletRequest request) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        String startTime = null;
        String endTime = null;
        if ("1".equals(date)) {
            startTime = DateUtil.getStartTime(-1);
            endTime = DateUtil.getEndTime(-1);
        } else if ("2".equals(date)) {
            startTime = DateUtil.getStartTime(-7);
            endTime = DateUtil.getEndTime(0);
        } else {//默认查今天
            startTime = DateUtil.getStartTime(0);
            endTime = DateUtil.getEndTime(0);
        }
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal washCode = BigDecimal.ZERO.setScale(2);
        ;
        if (userMoney != null && userMoney.getWashCode() != null) {
            washCode = userMoney.getWashCode();
        }
        List<WashCodeConfig> washCodeConfig = userWashCodeConfigService.getWashCodeConfig(userId);
        List<WashCodeChange> list = washCodeChangeService.getList(userId, startTime, endTime);
        ChargeOrderListData chargeOrderListData = new ChargeOrderListData();
        List<WashCodeVo> voList = new ArrayList<>();
        //洗码比例取配置表的,数据为空返回默认值
        if (CollectionUtils.isEmpty(list)) {
            WashCodeVo washCodeVo = null;
            for (WashCodeConfig config : washCodeConfig) {
                washCodeVo = new WashCodeVo();
                BeanUtils.copyProperties(config, washCodeVo);
                setGameName(request, washCodeVo, config);
                washCodeVo.setValidbet(BigDecimal.ZERO);
                washCodeVo.setRate(config.getRate() + "%");
                washCodeVo.setAmount(BigDecimal.ZERO);
                voList.add(washCodeVo);
            }
            chargeOrderListData.setTotalAmount(washCode);
            chargeOrderListData.setList(voList);
            return ResponseUtil.success(chargeOrderListData);
        }
        WashCodeVo washCodeVo = null;
        for (WashCodeConfig config : washCodeConfig) {
            washCodeVo = new WashCodeVo();
            boolean flag = true;
            for (WashCodeChange change : list) {
                //WM的洗码配置是配置到下面的游戏项上
                if (Constants.PLATFORM_WM.equals(change.getPlatform()) && !ObjectUtils.isEmpty(config.getGameId()) && config.getGameId().equals(change.getGameId())) {
                    BeanUtils.copyProperties(change, washCodeVo);
                    washCodeVo.setRate(config.getRate() + "%");
                    setGameName(request, washCodeVo, config);
                    voList.add(washCodeVo);
                    flag = false;
                    break;
                    //PG/CQ9的洗码配置是配置到平台项上
                } else if (!ObjectUtils.isEmpty(config.getGameId()) && config.getGameId().equals(change.getPlatform())) {
                    BeanUtils.copyProperties(change, washCodeVo);
                    washCodeVo.setRate(config.getRate() + "%");
                    setGameName(request, washCodeVo, config);
                    voList.add(washCodeVo);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                BeanUtils.copyProperties(config, washCodeVo);
                setGameName(request, washCodeVo, config);
                washCodeVo.setValidbet(BigDecimal.ZERO);
                washCodeVo.setRate(config.getRate() + "%");
                washCodeVo.setAmount(BigDecimal.ZERO);
                voList.add(washCodeVo);
            }
        }
        chargeOrderListData.setTotalAmount(washCode);
        chargeOrderListData.setList(voList);
        return ResponseUtil.success(chargeOrderListData);
    }

    /**
     * 根据前端选择的语音切换游戏名称
     *
     * @param request
     * @param washCodeVo
     * @param config
     */
    private void setGameName(HttpServletRequest request, WashCodeVo washCodeVo, WashCodeConfig config) {
        //语言切换
        String language = request.getHeader(Constants.LANGUAGE);
        if (!Locale.CHINA.toString().equals(language)) {
            washCodeVo.setGameName(config.getGameEnName());
        } else {
            washCodeVo.setGameName(config.getGameName());
        }
    }

    @ApiOperation("用户领取洗码")
    @GetMapping("/receiveWashCode")
    public ResponseEntity<String> receiveWashCode() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal washCode = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getWashCode() != null) {
            washCode = userMoney.getWashCode();
        }
        if (washCode.compareTo(BigDecimal.ONE) == -1) {
            return ResponseUtil.custom("金额小于1,不能领取");
        }
        userMoneyService.addMoney(userId, washCode);
        userMoneyService.subWashCode(userId, washCode);

        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.WASH_CODE);
        vo.setAmount(washCode);
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(washCode));
        asyncService.executeAsync(vo);
        //后台异步增减平台总余额
        platformConfigService.reception(CommonConst.NUMBER_0,washCode.stripTrailingZeros());
        return ResponseUtil.success("成功领取金额", washCode.stripTrailingZeros().toPlainString());
    }

    @Data
    @ApiModel("用户洗码列表")
    class ChargeOrderListData {
        @ApiModelProperty(value = "洗码金额")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal totalAmount;

        @ApiModelProperty(value = "数据列表")
        private List<WashCodeVo> list;
    }
}
