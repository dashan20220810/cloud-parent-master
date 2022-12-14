package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.ProxyHomePageReportBusiness;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyCommissionService;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.util.PasswordUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.ProxyUserVo;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "????????????")
@RestController
@Slf4j
@RequestMapping("proxyUser")
public class ProxyUserController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyHomePageReportBusiness proxyHomePageReportBusiness;

    @Autowired
    private MessageUtil messageUtil;
    /**
     * ??????????????????
     *
     * @param proxyRole ?????????
     * @param userFlag ????????????
     * @param tag 1???????????? 0????????????
     * @return
     */
    @ApiOperation("??????????????????")
    @GetMapping("/findProxyUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "????????????(??????10???)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "?????????(???????????????)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "????????????1???????????? 2??????????????? 3???????????????", required = false),
            @ApiImplicitParam(name = "userFlag", value = "1????????? 2?????????", required = false),
            @ApiImplicitParam(name = "isDelete", value = "1????????? 2?????????", required = false),
            @ApiImplicitParam(name = "userName", value = "??????", required = false),
            @ApiImplicitParam(name = "tag", value = "1???????????? 0????????????", required = true),
            @ApiImplicitParam(name = "startDate", value = "????????????????????????", required = false),
            @ApiImplicitParam(name = "endDate", value = "????????????????????????", required = false),
    })
    public ResponseEntity<ProxyUserVo> findProxyUser(Integer pageSize, Integer pageCode,Integer proxyRole,Integer userFlag,Integer tag,String userName,
                                        Integer isDelete,
                                        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ProxyUser proxyUser = new ProxyUser();
        Sort sort=Sort.by("proxyRole").ascending();
        sort = sort.and(Sort.by("id").descending());
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setUserFlag(userFlag);
        if (LoginUtil.checkNull(isDelete)){
            proxyUser.setIsDelete(CommonConst.NUMBER_1);
        }else {
            proxyUser.setIsDelete(isDelete);
        }
        if (!LoginUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    proxyUser.setFirstProxy(byUserName.getId());
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                }else {
                    proxyUser.setId(byUserName.getId());
                }
            }else {
                proxyUser.setUserName(userName);
            }
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable, proxyUser, startDate, endDate);
        PageResultVO<ProxyUserVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<ProxyUser> proxyUserList = proxyUserPage.getContent();
        List<Long> proxyIds = proxyUserList.stream().map(ProxyUser::getId).collect(Collectors.toList());
        List<ProxyCommission> proxyCommissions = proxyCommissionService.findProxyUser(proxyIds);
        if(proxyUserList != null && proxyUserList.size() > 0){
            List<ProxyUserVo> userVoList = new LinkedList();
            List<Long> firstProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() != CommonConst.NUMBER_1).map(ProxyUser::getFirstProxy).collect(Collectors.toList());
            List<Long> secondProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() == CommonConst.NUMBER_3).map(ProxyUser::getSecondProxy).collect(Collectors.toList());
            firstProxyIds.addAll(secondProxyIds);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(firstProxyIds);
            if (proxyUsers != null){
                proxyUserList.stream().forEach(u -> {
                    ProxyUserVo proxyUserVo = new ProxyUserVo(u);
                    proxyUsers.stream().forEach(proxy->{
                        if (u.getProxyRole() == CommonConst.NUMBER_2 && u.getFirstProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                            proxyUserVo.setFirstProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getSecondProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getFirstProxy().equals(proxy.getId())){
                            proxyUserVo.setFirstProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_1){
                            proxyUserVo.setFirstProxyAccount(proxyUserVo.getUserName());
                        }
                    });
                    proxyCommissions.stream().forEach(proxyCommission->{
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getId().equals(proxyCommission.getProxyUserId())){
                            String commissionRatio = MessageFormat.format(messageUtil.get(CommonConst.THIRD_FORMAT),proxyCommission.getFirstCommission() == null?"0":proxyCommission.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100),
                                    proxyCommission.getSecondCommission() == null?"0":proxyCommission.getSecondCommission().multiply(CommonConst.BIGDECIMAL_100),
                                    proxyCommission.getThirdCommission() == null?"0":proxyCommission.getThirdCommission().multiply(CommonConst.BIGDECIMAL_100));
                            proxyUserVo.setCommissionRatio(commissionRatio);
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_2 && u.getId().equals(proxyCommission.getProxyUserId())){
                            String commissionRatio = MessageFormat.format(messageUtil.get(CommonConst.SECOND_FORMAT),proxyCommission.getFirstCommission() == null?"0":proxyCommission.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100));
                            proxyUserVo.setCommissionRatio(commissionRatio);
                        }
                    });
                    User user = new User();
                    if (u.getProxyRole() == CommonConst.NUMBER_1){
                        user.setFirstProxy(u.getId());
                        Long  userCount  = userService.findUserCount(user,null,null);
                        proxyUserVo.setUsersNum(Math.toIntExact(userCount));

                    }else if (u.getProxyRole() == CommonConst.NUMBER_2){
                        user.setSecondProxy(u.getId());
                        Long  userCount  = userService.findUserCount(user,null,null);
                        proxyUserVo.setUsersNum(Math.toIntExact(userCount));
                    }else {
                        user.setThirdProxy(u.getId());
                        Long  userCount  = userService.findUserCount(user,null,null);
                        proxyUserVo.setUsersNum(Math.toIntExact(userCount));
                    }
                    userVoList.add(proxyUserVo);
                });
            }

            pageResultVO.setContent(userVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("?????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("findUsersNum")
    public ResponseEntity findUsersNum(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        User user = new User();
        Long  userCount;
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            user.setFirstProxy(id);
            userCount  = userService.findUserCount(user,null,null);

        }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
            user.setSecondProxy(id);
            userCount  = userService.findUserCount(user,null,null);
        }else {
            user.setThirdProxy(id);
            userCount  = userService.findUserCount(user,null,null);
        }
        return ResponseUtil.success(userCount);
    }

    @ApiOperation("????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "??????", required = true),
            @ApiImplicitParam(name = "nickName", value = "????????????", required = false),
            @ApiImplicitParam(name = "id", value = "?????????id", required = false),
            @ApiImplicitParam(name = "tag", value = "tag 1:?????? 2:????????? 3:??????", required = true),
    })
    @PostMapping("saveProxyUser")
    @Transactional
    public ResponseEntity saveProxyUser(String userName, String nickName,Long id,Integer tag){
        if (LoginUtil.checkNull(userName,tag)){
            return ResponseUtil.custom("???????????????");
        }
        if (!userName.matches(RegexEnum.PROXYACCOUNT.getRegex())){
            return ResponseUtil.custom("?????????????????????2-15?????????????????????");
        }
        if (!LoginUtil.checkNull(nickName) && !nickName.matches(RegexEnum.NAME.getRegex())){
            return ResponseUtil.custom("???????????????1~20??????????????????");
        }
        ProxyUser byUserName = proxyUserService.findByUserName(userName);
        if (!LoginUtil.checkNull(byUserName)){
            return ResponseUtil.custom("??????????????????");
        }

        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setUserName(userName);
        proxyUser.setNickName(nickName);
        //????????????
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        proxyUser.setPassWord(bcryptPassword);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setProxyUsersNum(CommonConst.NUMBER_0);
        if (tag == CommonConst.NUMBER_1){
            proxyUser.setProxyRole(CommonConst.NUMBER_1);
        }else if(tag == CommonConst.NUMBER_2){
            if (LoginUtil.checkNull(id)){
                return ResponseUtil.custom("???????????????");
            }
            ProxyUser proxy = proxyUserService.findById(id);
            if (LoginUtil.checkNull(proxy) || proxy.getIsDelete() == CommonConst.NUMBER_2 || proxy.getUserFlag() == CommonConst.NUMBER_2){
                return ResponseUtil.custom("?????????????????????");
            }
            if (proxy.getProxyRole() != CommonConst.NUMBER_1){
                return ResponseUtil.custom("??????????????????");
            }
            proxyUser.setProxyRole(CommonConst.NUMBER_2);
            proxyUser.setFirstProxy(proxy.getId());
        }else {
            if (LoginUtil.checkNull(id)){
                return ResponseUtil.custom("???????????????");
            }
            ProxyUser proxy = proxyUserService.findById(id);
            if (LoginUtil.checkNull(proxy) || proxy.getIsDelete() == CommonConst.NUMBER_2 || proxy.getUserFlag() == CommonConst.NUMBER_2){
                return ResponseUtil.custom("?????????????????????");
            }
            if (proxy.getProxyRole() != CommonConst.NUMBER_2){
                return ResponseUtil.custom("????????????????????????");
            }
            proxyUser.setProxyCode(CommonUtil.getProxyCode());
            proxyUser.setProxyRole(CommonConst.NUMBER_3);
            proxyUser.setFirstProxy(proxy.getFirstProxy());
            proxyUser.setSecondProxy(proxy.getId());
        }

        ProxyUser saveProxyUser = proxyUserService.save(proxyUser);
        if (LoginUtil.checkNull(saveProxyUser)){
            return ResponseUtil.custom("??????????????????");
        }else if (saveProxyUser.getProxyRole() == CommonConst.NUMBER_1){
            saveProxyUser.setFirstProxy(saveProxyUser.getId());
            proxyUserService.save(saveProxyUser);
        }else if(saveProxyUser.getProxyRole() == CommonConst.NUMBER_2){
            saveProxyUser.setSecondProxy(saveProxyUser.getId());
            proxyUserService.save(saveProxyUser);
            proxyUserService.addProxyUsersNum(saveProxyUser.getFirstProxy());
            this.createrProxyCommission(saveProxyUser,saveProxyUser.getProxyRole());
        }else {
            this.createrProxyCommission(saveProxyUser,saveProxyUser.getProxyRole());
            proxyUserService.addProxyUsersNum(saveProxyUser.getFirstProxy());
            proxyUserService.addProxyUsersNum(saveProxyUser.getSecondProxy());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", userName);
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }
    private void createrProxyCommission(ProxyUser saveProxyUser,Integer proxyRole){
        ProxyCommission proxyCommission = new ProxyCommission();
        proxyCommission.setProxyUserId(saveProxyUser.getId());
        if (proxyRole == CommonConst.NUMBER_3){
            ProxyCommission secondCommission = proxyCommissionService.findByProxyUserId(saveProxyUser.getSecondProxy());
            proxyCommission.setSecondProxy(saveProxyUser.getSecondProxy());
            proxyCommission.setFirstCommission((secondCommission == null || secondCommission.getFirstCommission() == null)? BigDecimal.ZERO:secondCommission.getFirstCommission());
        }
        proxyCommissionService.save(proxyCommission);
    }
    @ApiOperation("?????????????????????????????????")
    @GetMapping("getFirstProxy")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "proxyRole", value = "proxyRole(?????????1 ?????????2)", required = true),
    })
    @NoAuthorization
    public ResponseEntity<ProxyUser> getFirstProxy(Integer proxyRole){
        if (LoginUtil.checkNull(proxyRole)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setProxyRole(proxyRole);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        return ResponseUtil.success(proxyUserList);
    }
    @ApiOperation("????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("resetPasswords")
    public ResponseEntity resetPasswords(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        //????????????
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        byId.setPassWord(bcryptPassword);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", byId.getUserName());
        jsonObject.put("password", password);
        proxyUserService.save(byId);
        return ResponseUtil.success(jsonObject);
    }

    @ApiOperation("????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("updateUserFlag")
    public ResponseEntity updateUserFlag(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getUserFlag() == CommonConst.NUMBER_1){
            byId.setUserFlag(CommonConst.NUMBER_2);
        }else {
            byId.setUserFlag(CommonConst.NUMBER_1);
        }
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
    @ApiOperation("??????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("upgrade")
    @Transactional
    public ResponseEntity upgrade(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_2){
            return ResponseUtil.custom("??????????????????????????????");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(byId.getId());
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList.stream().forEach(proxy -> {
            proxy.setFirstProxy(byId.getId());
            proxyUserService.save(proxy);
        });
        byId.setFirstProxy(byId.getId());
        byId.setProxyRole(CommonConst.NUMBER_1);
        byId.setSecondProxy(null);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
//    @ApiOperation("????????????")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "passivityId", value = "????????????id", required = true),
//            @ApiImplicitParam(name = "id", value = "??????id", required = true),
//    })
//    @GetMapping("transfer")
//    public ResponseEntity transfer(Long passivityId,Long id){
//        if (LoginUtil.checkNull(id,passivityId)){
//            return ResponseUtil.custom("???????????????");
//        }
//        ProxyUser passivity = proxyUserService.findById(passivityId);
//        ProxyUser byId = proxyUserService.findById(id);
//        if (LoginUtil.checkNull(passivity,byId)){
//            return ResponseUtil.custom("??????????????????");
//        }
//        if (passivity.getProxyRole() != CommonConst.NUMBER_1 || byId.getProxyRole() != CommonConst.NUMBER_1 ){
//            return ResponseUtil.custom("??????????????????????????????");
//        }
//        ProxyUser proxyUser = new ProxyUser();
//        proxyUser.setFirstProxy(passivity.getId());
//        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
//        proxyUserList.stream().filter(PUser -> PUser.getId() != passivityId).forEach(proxy -> {
//            proxy.setFirstProxy(byId.getId());
//            proxyUserService.save(proxy);
//        });
//        return ResponseUtil.success();
//    }
    @ApiOperation("?????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "?????????id", required = true),
    })
    @GetMapping("findProxyUserList")
    public ResponseEntity<ProxyUser> findProxyUserList(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setProxyRole(CommonConst.NUMBER_1);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList = proxyUserList == null?new ArrayList<>(): proxyUserList.stream().filter(PUser -> !PUser.getId().equals(id)).collect(Collectors.toList());
        return ResponseUtil.success(proxyUserList);
    }
    @ApiOperation("??????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("delete")
    @Transactional
    public ResponseEntity delete(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findProxyUserById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getIsDelete() == CommonConst.NUMBER_2){
            return ResponseUtil.custom("?????????????????????");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            ProxyUser proxyUser = new ProxyUser();
            proxyUser.setFirstProxy(byId.getId());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
            if(!LoginUtil.checkNull(proxyUsers) && proxyUsers.size() >= CommonConst.NUMBER_2){
                return ResponseUtil.custom("??????????????????????????????");
            }
        }else if (byId.getProxyRole() == CommonConst.NUMBER_2){
            ProxyUser proxyUser = new ProxyUser();
            proxyUser.setSecondProxy(byId.getId());
            proxyUser.setProxyRole(CommonConst.NUMBER_3);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
            if(!LoginUtil.checkNull(proxyUsers) && proxyUsers.size() >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("??????????????????????????????");
            }
            proxyUserService.subProxyUsersNum(byId.getFirstProxy());
        }else {
            User user = new User();
            user.setThirdProxy(id);
            List<User> userList = userService.findUserList(user, null, null);
            if ( !LoginUtil.checkNull(userList) && userList.size() >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("???????????????????????????");
            }
            proxyUserService.subProxyUsersNum(byId.getFirstProxy());
            proxyUserService.subProxyUsersNum(byId.getSecondProxy());
        }
        byId.setIsDelete(CommonConst.NUMBER_2);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }


    @ApiOperation("????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "????????????id(?????????id)", required = true),
            @ApiImplicitParam(name = "acceptId", value = "?????????id", required = true),
    })
    @GetMapping("transferUser")
    @Transactional
    public ResponseEntity transferUser(Long id,Long acceptId){
        if (LoginUtil.checkNull(id,acceptId)){
            return ResponseUtil.custom("???????????????");
        }
        if (id.toString().equals(acceptId.toString())){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        ProxyUser accept = proxyUserService.findById(acceptId);
        if (LoginUtil.checkNull(byId,accept)){
            return ResponseUtil.custom("??????????????????");
        }
        if ( byId.getProxyRole() != CommonConst.NUMBER_3 || accept.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("????????????????????????????????????");
        }
        if (!byId.getSecondProxy().equals(accept.getSecondProxy())){
            return ResponseUtil.custom("???????????????????????????");
        }
        log.info("admin??????????????????????????????{} ?????????{}",byId.getUserName(),accept.getUserName());
        try {
            return proxyHomePageReportBusiness.transferUser(id,acceptId,accept);
        }catch (Exception ex){
            return ResponseUtil.custom("??????????????????????????????");
        }
    }

    @ApiOperation("?????????????????????????????????")
    @GetMapping("getTransferUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "?????????id", required = true),
    })
    @NoAuthorization
    public ResponseEntity<ProxyUser> getTransferUser(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("????????????????????????????????????");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setSecondProxy(byId.getSecondProxy());
        proxyUser.setProxyRole(CommonConst.NUMBER_3);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList = proxyUserList.stream().filter(proxy -> !proxy.getId().equals(id)).collect(Collectors.toList());
        return ResponseUtil.success(proxyUserList);
    }

    @ApiOperation("?????????????????????????????????")
    @GetMapping("getTransferProxy")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "?????????id", required = true),
    })
    @NoAuthorization
    public ResponseEntity<ProxyUser> getTransferProxy(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_3){
            return ResponseUtil.custom("??????????????????????????????");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyUser.setProxyRole(CommonConst.NUMBER_1);
        }else {
            proxyUser.setProxyRole(CommonConst.NUMBER_2);
            proxyUser.setFirstProxy(byId.getFirstProxy());
        }
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList = proxyUserList.stream().filter(proxy -> !proxy.getId().equals(id)).collect(Collectors.toList());
        return ResponseUtil.success(proxyUserList);
    }

    @ApiOperation("????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "????????????id(?????????id)", required = true),
            @ApiImplicitParam(name = "acceptId", value = "?????????id", required = true),
    })
    @GetMapping("transferProxy")
    public ResponseEntity transferProxy(Long id,Long acceptId){
        if (LoginUtil.checkNull(id,acceptId) || id.toString().equals(acceptId.toString())){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        ProxyUser accept = proxyUserService.findById(acceptId);
        if (LoginUtil.checkNull(byId,accept)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getProxyRole() != accept.getProxyRole()){
            return ResponseUtil.custom("???????????????????????????");
        }
        log.info("admin??????????????????????????????{} ?????????{}",byId.getUserName(),accept.getUserName());
        try {
            return proxyHomePageReportBusiness.transferProxy(byId,accept);
        }catch (Exception ex){
            return ResponseUtil.custom("??????????????????????????????");
        }
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "????????????id(?????????id)", required = true),
        @ApiImplicitParam(name = "acceptId", value = "?????????id", required = true),
    })
    @GetMapping("transferProxyUp")
    public ResponseEntity transferProxyUp(Long id,Long acceptId){
        if (LoginUtil.checkNull(id,acceptId) || id.toString().equals(acceptId.toString())){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        ProxyUser accept = proxyUserService.findById(acceptId);
        if (LoginUtil.checkNull(byId,accept)){
            return ResponseUtil.custom("??????????????????");
        }
        if (!byId.getFirstProxy().equals(accept.getFirstProxy())){
            return ResponseUtil.custom("??????????????????????????????");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("????????????????????????");
        }
        if (accept.getProxyRole() != CommonConst.NUMBER_2){
            return ResponseUtil.custom("???????????????????????????");
        }
        log.info("admin????????????????????????????????????{} ?????????{}",byId.getUserName(),accept.getUserName());
        try {
            return proxyHomePageReportBusiness.transferProxyUp(byId,accept);
        }catch (Exception ex){
            return ResponseUtil.custom("??????????????????????????????");
        }
    }

    @ApiOperation("???????????????????????????????????????")
    @GetMapping("getTransferProxyUp")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "?????????id", required = true),
    })
    @NoAuthorization
    public ResponseEntity<ProxyUser> getTransferProxyUp(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("???????????????");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("??????????????????");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("??????????????????????????????");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setProxyRole(CommonConst.NUMBER_2);
        proxyUser.setFirstProxy(byId.getFirstProxy());
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList = proxyUserList.stream().filter(proxy -> !proxy.getId().equals(byId.getSecondProxy())).collect(Collectors.toList());
        return ResponseUtil.success(proxyUserList);
    }
}
