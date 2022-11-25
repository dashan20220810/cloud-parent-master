package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.Date;

@Api(tags = "运营中心")
@RestController
@Slf4j
@RequestMapping("rePullGameRecord")
public class RePullGameRecord {

    @Autowired
    private PlatformConfigService platformConfigService;

    private String rePullDataUrl = "/supplement/supplementByPlatform?";

    /**
     * 1分钟只能请求一次
     *
     * @param startDate
     * @param endDate
     * @param platform
     * @return
     */
    @RequestLimit(limit = 3,timeout = 60)
    @ApiOperation("补历史注单(1分钟三次)")
    @GetMapping("pullGameRecordDate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "开始时间", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = true),
            @ApiImplicitParam(name = "platform", value = "平台：WM,PG,CQ9", required = true)
    })
    public ResponseEntity pullGameRecordDate(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                             @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,
                                             String platform){

        try {
            int numDay = DateUtil.daysBetween(startDate, endDate);
            if(numDay >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("参数不合法");
            }

            PlatformConfig first = platformConfigService.findFirst();
            String rePullGameurl = first == null?"":first.getWebConfiguration();
            if(LoginUtil.checkNull(rePullGameurl)){
                return ResponseUtil.custom("web域名错误");
            }
            rePullGameurl = rePullGameurl + rePullDataUrl;
            String param = "startTime={0}&endTime={1}&platform={2}";
            String startStr = DateUtil.dateToPatten(startDate);
            String endTineStr = DateUtil.dateToPatten(endDate);
            startStr = startStr.replaceAll(":", "%3A").replaceAll(" ", "%20");
            endTineStr = endTineStr.replaceAll(":", "%3A").replaceAll(" ", "%20");

            param = MessageFormat.format(param,startStr,endTineStr,platform);
            String s = HttpClient4Util.getWeb(rePullGameurl + param);
            log.info("平台【{}】，开始时间：【{}】，结束时间：【{}】查询web接口返回【{}】", platform, startDate, endDate, s);
            JSONObject jsonObject = JSONObject.parseObject(s);
            if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                return ResponseUtil.custom("拉取数据失败");
            }
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                return ResponseUtil.success(jsonObject.get("data"));
            }
            return ResponseUtil.custom(jsonObject.get("msg").toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.custom("参数不合法");
        }
    }

}
