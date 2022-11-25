package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.DomainConfig;
import com.qianyi.casinocore.model.Visits;
import com.qianyi.casinocore.service.DomainConfigService;
import com.qianyi.casinocore.service.VisitsService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "访问量")
@RestController
@RequestMapping("/visits")
public class VisitsController {
    @Autowired
    private VisitsService visitsService;
    @Autowired
    private DomainConfigService domainConfigService;

    @GetMapping("/addVisits")
    @ApiOperation("新增访问量")
    @NoAuthentication
    public ResponseEntity addVisits(HttpServletRequest request) {
        //获取访问时用的域名
        String origin = request.getHeader("origin");
        DomainConfig domainConfig = domainConfigService.findByDomainUrlAndDomainStatus(origin, Constants.open);
        if (domainConfig == null) {
            return ResponseUtil.success();
        }
        String ip = IpUtil.getIp(request);
        Visits visits = new Visits();
        visits.setIp(ip);
        visits.setDomainName(origin);
        visitsService.save(visits);
        return ResponseUtil.success();
    }
}
