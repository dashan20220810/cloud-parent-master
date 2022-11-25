package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.VisitsSum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.vo.VisitsVo;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.casinocore.vo.UserVo;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/domain")
@Api(tags = "域名管理")
public class DomainConfigController {

    @Autowired
    private DomainConfigService domainConfigService;

    @Autowired
    private VisitsService visitsService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private ProxyUserService proxyUserService;

    public final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/visitsFindList")
    @ApiOperation("域名访问量统计")
    @NoAuthorization
    @ApiImplicitParams({
        @ApiImplicitParam(name = "domainName", value = "域名名称", required = false),
        @ApiImplicitParam(name = "StartTime", value = "开始时间", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true),
    })
    public ResponseEntity<VisitsVo> visitsFindList(String domainName, String ip, String StartTime, String endTime) {
        List<Map<String, Object>> list = visitsService.findListSum(domainName, ip, StartTime, endTime);
        List<VisitsVo> visitsVoList = new ArrayList<>();
        List<VisitsSum> visitsSumList = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : list) {
            String dataStr = JSON.toJSONString(stringObjectMap);
            VisitsSum visitsSum = JSON.parseObject(dataStr, VisitsSum.class);
            visitsSumList.add(visitsSum);
        }

        if(visitsSumList== null || visitsSumList.isEmpty()){
            return ResponseUtil.success();
        }
        Map<String, List<VisitsSum>> collect = visitsSumList.stream().collect(Collectors.groupingBy(VisitsSum::getDomainName));
        for (String s : collect.keySet()) {
            VisitsVo visitsVo = new VisitsVo();
            visitsVo.setDomainName(s);
            List<VisitsSum> visitss = collect.get(s);
            visitsVo.setDomainIpCount(visitss.size());
            Integer sumDomainCount = visitss.stream().collect(Collectors.summingInt(VisitsSum::getDomainCount));
            visitsVo.setDomainCount(sumDomainCount);
            Set<Long> users = userService.findUserByRegisterDomainName(visitsVo.getDomainName(), StartTime, endTime);
            visitsVo.setNums(users.size());
            List<ChargeOrder> chargeNums = chargeOrderService.getChargeNums(users);
            visitsVo.setChargeNums(chargeNums == null?0:chargeNums.size());
            visitsVoList.add(visitsVo);
        }
        return ResponseUtil.success(visitsVoList);
    }


    @ApiOperation("用户列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "tag", value = "注册:1  充值:2", required = true),
        @ApiImplicitParam(name = "domainName", value = "域名名称", required = true),
        @ApiImplicitParam(name = "startDate", value = "开始时间", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间", required = true)
    })
    @GetMapping("findUser")
    public ResponseEntity<UserVo> findUserTotal(Integer pageSize, Integer pageCode,Integer tag, String domainName, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        if (LoginUtil.checkNull(tag,domainName,startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }

        PageResultVO<UserVo> pageResultVO = null;
        List<User> userList;
        if (tag == CommonConst.NUMBER_1){
            Sort sort=Sort.by("id").descending();
            Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
            User user = new User();
            user.setRegisterDomainName(domainName);
            Page<User> userPage = userService.findUserPage(pageable, user,startDate,endDate);
            pageResultVO = new PageResultVO(userPage);
            userList = userPage.getContent();
            if(userList != null && userList.size() > 0){
                List<UserVo> userVoList = new LinkedList();
                List<Long> firstPids = userList.stream().map(User::getFirstPid).collect(Collectors.toList());
                List<User> firstPidUsers = userService.findAll(firstPids);
                List<Long> thirdProxys = userList.stream().map(User::getThirdProxy).collect(Collectors.toList());
                List<Long> firstProxys = userList.stream().map(User::getFirstProxy).collect(Collectors.toList());
                thirdProxys.addAll(firstProxys);
                List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(thirdProxys);
                if(userList != null){
                    userList.stream().forEach(u -> {
                        UserVo userVo = DTOUtil.toDTO(u, UserVo.class);
                        firstPidUsers.stream().forEach(firstPid -> {
                            if(firstPid.getId().equals(u.getFirstPid() == null ? "":u.getFirstPid())){
                                userVo.setFirstPidAccount(firstPid.getAccount());
                            }
                        });
                        proxyUsers.stream().forEach(proxyUser -> {
                            if(proxyUser.getId().equals(u.getThirdProxy() == null ? "":u.getThirdProxy())){
                                userVo.setThirdProxyAccount(proxyUser.getUserName());
                                userVo.setThirdProxyId(proxyUser.getId());
                            }
                            if(proxyUser.getId().equals(u.getFirstProxy() == null ? "":u.getFirstProxy())){
                                userVo.setFirstProxyAccount(proxyUser.getUserName());
                            }
                        });
                        userVoList.add(userVo);
                    });
                    pageResultVO.setContent(userVoList);
                }
            }
        }else if (tag == CommonConst.NUMBER_2){
            String startTime = formatter.format(startDate);
            String endTime = formatter.format(endDate);
            Set<Long> users = userService.findUserByRegisterDomainName(domainName, startTime, endTime);
            List<ChargeOrder> chargeNums = chargeOrderService.getChargeNums(users);
            if (chargeNums != null && chargeNums.size()> CommonConst.NUMBER_0){
                List<Long> userIds = chargeNums.stream().map(ChargeOrder::getUserId).collect(Collectors.toList());
                PageVo pageVO = new PageVo(pageCode,pageSize);
                PageResultVO<Long> pageResultId = (PageResultVO<Long>) CommonUtil.handlePageResult(userIds, pageVO);
                pageResultVO = new PageResultVO(pageResultId);
                List<UserVo> userVoList = new LinkedList();
                List<Long> list = (List<Long>)pageResultId.getContent();

                for (Long user:list){
                    UserVo vo = new UserVo();
                    User byId = userService.findById(user);
                    if (LoginUtil.checkNull(byId)){
                        continue;
                    }

                    vo.setAccount(byId.getAccount());
                    vo.setCreateTime(byId.getCreateTime());
                    if (!LoginUtil.checkNull(byId.getFirstPid())){
                        User byId1 = userService.findById(byId.getFirstPid());
                        vo.setFirstPidAccount(byId1==null?"":byId1.getAccount());
                    }
                    if (!LoginUtil.checkNull(byId.getThirdProxy())){
                        User third = userService.findById(byId.getThirdProxy());
                        vo.setThirdProxyAccount(third==null?"":third.getAccount());
                        User first = userService.findById(byId.getFirstProxy());
                        vo.setFirstProxyAccount(first==null?"":first.getAccount());
                    }
                    userVoList.add(vo);
                }
                pageResultVO.setContent(userVoList);
            }
        }else {
            return ResponseUtil.custom("参数不合法");
        }
        return ResponseUtil.success(pageResultVO);
    }
    @GetMapping("/findList")
    @ApiOperation("域名列表")
    public ResponseEntity<DomainConfig> findList() {
        List<DomainConfig> domainConfigList = domainConfigService.findList();
        if(domainConfigList != null && !domainConfigList.isEmpty()){
            List<String> createBy = domainConfigList.stream().map(DomainConfig::getUpdateBy).collect(Collectors.toList());
            List<SysUser> sysUsers = sysUserService.findAll(createBy);
            domainConfigList.stream().forEach(domainConfig -> {
                sysUsers.stream().forEach(sysUser -> {
                    if (sysUser.getId().toString().equals(domainConfig.getUpdateBy() == null ? "" : domainConfig.getCreateBy())) {
                        domainConfig.setUpdateBy(sysUser.getUserName());
                    }
                });
            });
        }

        return ResponseUtil.success(domainConfigList);
    }

    @ApiOperation("新增或者修改域名")
    @PostMapping(value = "/saveDomain",name = "新增或者修改域名")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "domainName", value = "域名名称", required = false),
        @ApiImplicitParam(name = "domainUrl", value = "域名地址", required = true),
        @ApiImplicitParam(name = "id", value = "id", required = false),
        @ApiImplicitParam(name = "domainStatus", value = "状态 0：禁用， 1：启用", required = true)})
    public ResponseEntity<DomainConfig> saveDomain(String domainName,String domainUrl,Long id, Integer domainStatus){
        if (LoginUtil.checkNull(domainUrl) || domainStatus == null){
            ResponseUtil.custom("参数不合法");
        }
        if(domainStatus != CommonConst.NUMBER_0 && domainStatus != CommonConst.NUMBER_1){
            ResponseUtil.custom("参数不合法");
        }
        DomainConfig domainConfig = new DomainConfig();
        if(id != null){
            DomainConfig domain = domainConfigService.findById(id);
            if(domain != null){
                domainConfig = domain;
            }
        }

        domainConfig.setDomainName(domainName);
        domainConfig.setDomainUrl(domainUrl);
        domainConfig.setDomainStatus(domainStatus);
        domainConfigService.save(domainConfig);
        return ResponseUtil.success(domainConfig);
    }

    @ApiOperation("修改域名状态")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", required = true),
        @ApiImplicitParam(name = "domainStatus", value = "状态 0：禁用， 1：启用", required = true)
    })
    @PostMapping("domainStatus")
    public ResponseEntity<DomainConfig> updateDomainStatus(Long id, Integer domainStatus){
        if(domainStatus != CommonConst.NUMBER_0 && domainStatus != CommonConst.NUMBER_1){
            ResponseUtil.custom("参数不合法");
        }
        DomainConfig domainConfig = domainConfigService.findById(id);
        domainConfig.setDomainStatus(domainStatus);
        domainConfigService.save(domainConfig);
        return ResponseUtil.success(domainConfig);
    }

    @ApiOperation("删除域名")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", required = true)
    })
    @GetMapping("deleteId")
    public ResponseEntity<DomainConfig> deleteId(Long id){

        domainConfigService.deleteId(id);
        return ResponseUtil.success();
    }
}
