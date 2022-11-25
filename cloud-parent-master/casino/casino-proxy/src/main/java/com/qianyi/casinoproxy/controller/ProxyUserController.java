package com.qianyi.casinoproxy.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ProxyHomePageReportBusiness;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyCommissionService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PasswordUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.ProxyUserVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
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

@Api(tags = "代理中心")
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
    private MessageUtil messageUtil;

    @Autowired
    private ProxyHomePageReportBusiness proxyHomePageReportBusiness;

    private static final String METHOD_SECOND_FORMAT = "可设置占成比";

    private static final String METHOD_PERCENTAGE = ":0%-{0}%";

    private static final String METHOD_FIRST_FORMAT = "总代(自己){0}";

    /**
     * 分页查询代理
     *
     * @param proxyRole 订单号
     * @param userFlag 账变类型
     * @param tag 1：含下级 0：不包含
     * @return
     */
    @ApiOperation("分页查询代理")
    @GetMapping("/findProxyUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userFlag", value = "1：正常 2：锁定", required = false),
            @ApiImplicitParam(name = "isDelete", value = "1：正常 2：删除", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = true),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    public ResponseEntity<ProxyUserVo> findProxyUser(Integer pageSize, Integer pageCode,Integer proxyRole,Integer userFlag,Integer tag,String userName,
                                                     Integer isDelete,
                                        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(tag)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser proxyUser = new ProxyUser();
        Sort sort=Sort.by("proxyRole").ascending();
        sort = sort.and(Sort.by("id").descending());
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setUserFlag(userFlag);
        if (CasinoProxyUtil.checkNull(isDelete)){
            proxyUser.setIsDelete(CommonConst.NUMBER_1);
        }else {
            proxyUser.setIsDelete(isDelete);
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        proxyUser.setId(authId);
        if (tag == CommonConst.NUMBER_1 && CasinoProxyUtil.checkNull(userName)){
            if (byId.getProxyRole() == CommonConst.NUMBER_1){
                proxyUser.setFirstProxy(authId);
                proxyUser.setId(null);
            }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
                proxyUser.setSecondProxy(authId);
                proxyUser.setId(null);
            }
        }else if(tag == CommonConst.NUMBER_1 && !CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (byUserName.getProxyRole() == byId.getProxyRole()){//级别相同
                if (byUserName.getId() == authId){//自己查自己
                    if (byUserName.getProxyRole() == CommonConst.NUMBER_1){//自己总代查自己
                        proxyUser.setFirstProxy(authId);
                        proxyUser.setId(null);
                    }else if(byId.getProxyRole() == CommonConst.NUMBER_2){//自己区域查自己
                        proxyUser.setSecondProxy(authId);
                        proxyUser.setId(null);
                    }
                }else {
                    return ResponseUtil.success(new PageResultVO());
                }
            }else if(byUserName.getProxyRole() > byId.getProxyRole()){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2 && byUserName.getFirstProxy().equals(authId)){//1级搜2级
                    proxyUser.setSecondProxy(byUserName.getId());
                    proxyUser.setId(null);
                }else if (byUserName.getProxyRole() == CommonConst.NUMBER_3 && ((byUserName.getSecondProxy()!=null&&byUserName.getSecondProxy().equals(authId)) ||byUserName.getFirstProxy().equals(authId))){//2级搜3级 1级搜3级
                    proxyUser.setUserName(userName);
                    proxyUser.setId(null);
                }else {//没有权限
                    return ResponseUtil.success(new PageResultVO());
                }
            }else {
                return ResponseUtil.success(new PageResultVO());
            }
        }else if(tag == CommonConst.NUMBER_0 && !CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (((byUserName.getSecondProxy()!=null&&byUserName.getSecondProxy().equals(authId)) ||byUserName.getFirstProxy().equals(authId)) || byUserName.getId().equals(authId)){
                proxyUser.setUserName(userName);
                proxyUser.setId(null);
            }else {
                return ResponseUtil.success(new PageResultVO());
            }
        }else {
            return ResponseUtil.success(new PageResultVO());
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable, proxyUser, startDate, endDate);
        PageResultVO<ProxyUserVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<ProxyUser> proxyUserList = proxyUserPage.getContent();
        if(proxyUserList != null && proxyUserList.size() > 0){
            List<ProxyUserVo> userVoList = new LinkedList();
            List<Long> firstProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() != CommonConst.NUMBER_1).map(ProxyUser::getFirstProxy).collect(Collectors.toList());
            List<Long> secondProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() == CommonConst.NUMBER_3).map(ProxyUser::getSecondProxy).collect(Collectors.toList());
            firstProxyIds.addAll(secondProxyIds);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(firstProxyIds);
            List<Long> proxyIds = proxyUserList.stream().map(ProxyUser::getId).collect(Collectors.toList());
            List<ProxyCommission> proxyCommissions = proxyCommissionService.findProxyUser(proxyIds);
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

    /**
     * 重置谷歌验证码
     *
     * @param id
     * @return
     */
    @ApiOperation("重置谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
    })
    @GetMapping("resetGaKey")
    public ResponseEntity resetGaKey(Long id) {
        if(CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数错误");
        }
        ProxyUser proxyUser = proxyUserService.findById(id);
        if(proxyUser == null){
            return ResponseUtil.custom("账号不存在");
        }
        proxyUser.setGaBind(Constants.open + "");
        proxyUser.setGaKey(null);
        proxyUserService.save(proxyUser);
        return ResponseUtil.success();
    }
    @ApiOperation("查询详情页面直属玩家数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("findUsersNum")
    public ResponseEntity findUsersNum(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
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
    @ApiOperation("修改下级代理数")
    @GetMapping("proxyUsersNum")
    @Transactional
    public ResponseEntity proxyUsersNum(){
        ProxyUser proxyUser = new ProxyUser();
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        for (ProxyUser p:proxyUserList){
            if (p.getProxyRole() == CommonConst.NUMBER_3){
                continue;
            }
            if (p.getProxyRole() == CommonConst.NUMBER_2){
                ProxyUser proxyUser1 = new ProxyUser();
                proxyUser1.setIsDelete(CommonConst.NUMBER_1);
                proxyUser1.setSecondProxy(p.getId());
                proxyUser1.setProxyRole(CommonConst.NUMBER_3);
                List<ProxyUser> proxyUserList1 = proxyUserService.findProxyUserList(proxyUser1);
                p.setProxyUsersNum(proxyUserList1 == null? CommonConst.NUMBER_0:proxyUserList1.size());
                proxyUserService.save(p);
            }
            if (p.getProxyRole() == CommonConst.NUMBER_1){

                ProxyUser proxyUser2 = new ProxyUser();
                proxyUser2.setIsDelete(CommonConst.NUMBER_1);
                proxyUser2.setFirstProxy(p.getId());
                proxyUser2.setProxyRole(CommonConst.NUMBER_3);
                List<ProxyUser> proxyUserList1 = proxyUserService.findProxyUserList(proxyUser2);
                ProxyUser proxyUser3 = new ProxyUser();
                proxyUser3.setFirstProxy(p.getId());
                proxyUser3.setProxyRole(CommonConst.NUMBER_2);
                proxyUser3.setIsDelete(CommonConst.NUMBER_1);
                List<ProxyUser> proxyUserList2 = proxyUserService.findProxyUserList(proxyUser3);
                Integer sum = proxyUserList1.size() + proxyUserList2.size();
                p.setProxyUsersNum(sum);
                proxyUserService.save(p);
            }
        }
        return ResponseUtil.success();
    }

    @ApiOperation("添加代理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = true),
            @ApiImplicitParam(name = "nickName", value = "用户昵称", required = false),
    })
    @PostMapping("saveProxyUser")
    @Transactional
    public ResponseEntity saveProxyUser(String userName, String nickName){
        if (CasinoProxyUtil.checkNull(userName)){
            return ResponseUtil.custom("参数不合法");
        }
        if (!userName.matches(RegexEnum.PROXYACCOUNT.getRegex())){
            return ResponseUtil.custom("代理账号必须是2-15位的字母或数字");
        }
        if (!CasinoProxyUtil.checkNull(nickName) && !nickName.matches(RegexEnum.NAME.getRegex())){
            return ResponseUtil.custom("昵称请输入1~20位中文或字母");
        }
        ProxyUser byUserName = proxyUserService.findByUserName(userName);
        if (!CasinoProxyUtil.checkNull(byUserName)){
            return ResponseUtil.custom("代理账号重复");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (byId.getProxyRole() == CommonConst.NUMBER_3){
            return ResponseUtil.custom("基层代理不能创建代理");
        }
        Integer proxyRole = byId.getProxyRole() + CommonConst.NUMBER_1;
        ProxyUser proxyUser = new ProxyUser();
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyUser.setFirstProxy(byId.getId());
        }else {
            proxyUser.setFirstProxy(byId.getFirstProxy());
            proxyUser.setSecondProxy(byId.getId());
            proxyUser.setProxyCode(com.qianyi.casinocore.util.CommonUtil.getProxyCode());
        }
        proxyUser.setUserName(userName);
        proxyUser.setNickName(nickName);
        //随机生成
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
        proxyUser.setPassWord(bcryptPassword);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setProxyUsersNum(CommonConst.NUMBER_0);
        ProxyUser saveProxyUser = proxyUserService.save(proxyUser);
        if (CasinoProxyUtil.checkNull(saveProxyUser)){
            return ResponseUtil.custom("添加代理失败");
        }else if (saveProxyUser.getProxyRole() == CommonConst.NUMBER_2 ){
            saveProxyUser.setSecondProxy(saveProxyUser.getId());
            proxyUserService.save(saveProxyUser);
            proxyUserService.addProxyUsersNum(saveProxyUser.getFirstProxy());
            this.createrProxyCommission(saveProxyUser,saveProxyUser.getProxyRole());
        }else{
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
            proxyCommission.setFirstCommission((secondCommission == null || secondCommission.getFirstCommission() == null)?BigDecimal.ZERO:secondCommission.getFirstCommission());
        }
        proxyCommissionService.save(proxyCommission);
    }
    @ApiOperation("重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("resetPasswords")
    public ResponseEntity resetPasswords(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (CasinoProxyUtil.getAuthId().equals(byId.getId())){
            return ResponseUtil.custom("不能重置自己的密码");
        }
        //随机生成
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
        byId.setPassWord(bcryptPassword);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", byId.getUserName());
        jsonObject.put("password", password);
        proxyUserService.save(byId);
        return ResponseUtil.success(jsonObject);
    }
    @ApiOperation("锁定解锁")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("updateUserFlag")
    public ResponseEntity updateUserFlag(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getUserFlag() == CommonConst.NUMBER_1){
            byId.setUserFlag(CommonConst.NUMBER_2);
        }else {
            byId.setUserFlag(CommonConst.NUMBER_1);
        }
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
//    @ApiOperation("升级")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "id", required = true),
//    })
//    @GetMapping("upgrade")
//    public ResponseEntity upgrade(Long id){
//        if (CasinoProxyUtil.checkNull(id)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        ProxyUser byId = proxyUserService.findById(id);
//        if (CasinoProxyUtil.checkNull(byId)){
//            return ResponseUtil.custom("没有这个代理");
//        }
//        if (byId.getProxyRole() != CommonConst.NUMBER_3){
//            return ResponseUtil.custom("只有基层代理可以升级");
//        }
//        byId.setProxyRole(CommonConst.NUMBER_2);
//        byId.setSecondProxy(byId.getId());
//        proxyUserService.save(byId);
//        return ResponseUtil.success();
//    }
//    @ApiOperation("转移下级")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "passivityId", value = "被转移的id", required = true),
//            @ApiImplicitParam(name = "id", value = "接受id", required = true),
//    })
//    @GetMapping("transfer")
//    public ResponseEntity transfer(Long passivityId,Long id){
//        if (CasinoProxyUtil.checkNull(id,passivityId)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        if (passivityId == id){
//            return ResponseUtil.custom("参数不合法");
//        }
//        ProxyUser passivity = proxyUserService.findById(passivityId);
//        ProxyUser byId = proxyUserService.findById(id);
//        if (CasinoProxyUtil.checkNull(passivity,byId)){
//            return ResponseUtil.custom("没有这个代理");
//        }
//        if (passivity.getProxyRole() != CommonConst.NUMBER_2 || byId.getProxyRole() != CommonConst.NUMBER_2 ){
//            return ResponseUtil.custom("只有区域代理可以转移下级");
//        }
//        ProxyUser proxyUser = new ProxyUser();
//        proxyUser.setSecondProxy(passivity.getId());
//        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
//        proxyUserList.stream().filter(PUser -> PUser.getId() != passivityId).forEach(proxy -> {
//            proxy.setSecondProxy(byId.getId());
//            proxyUserService.save(proxy);
//        });
//        return ResponseUtil.success();
//    }
    @ApiOperation("获取下拉框数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
    })
    @GetMapping("findProxyUserList")
    public ResponseEntity<ProxyUser> findProxyUserList(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (byId.getProxyRole() != CommonConst.NUMBER_1){
            return ResponseUtil.custom("没有权限");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setProxyRole(CommonConst.NUMBER_2);
        proxyUser.setFirstProxy(byId.getId());
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList = proxyUserList == null?new ArrayList<>(): proxyUserList.stream().filter(PUser -> !PUser.getId().equals(id)).collect(Collectors.toList());
        return ResponseUtil.success(proxyUserList);
    }

    @ApiOperation("查询单个代理分成比")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "点击列id", required = true),
    })
    @GetMapping("findProxyCommission")
    public ResponseEntity findProxyCommission(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        String commission = "";
        ProxyUser proxyUser = proxyUserService.findById(id);
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (CasinoProxyUtil.checkNull(byId,proxyUser)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
            return ResponseUtil.custom("不能直接设置总代");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_3 ||(byId.getProxyRole() == CommonConst.NUMBER_2 && proxyUser.getProxyRole() == CommonConst.NUMBER_2)){
            return ResponseUtil.custom("没有权限");
        }
        ProxyCommission byProxyUserId = proxyCommissionService.findByProxyUserId(proxyUser.getId());
        JSONObject jsonObject = new JSONObject();
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
            commission = MessageFormat.format(messageUtil.get(METHOD_FIRST_FORMAT),"");
            jsonObject.put("commission",commission);
            jsonObject.put("data",(byProxyUserId==null&&byProxyUserId.getFirstCommission()==null)?CommonConst.NUMBER_0:byProxyUserId.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100));
            return ResponseUtil.success(jsonObject);
        }else {
            if (CasinoProxyUtil.checkNull(byProxyUserId) || CasinoProxyUtil.checkNull(byProxyUserId.getFirstCommission())){
                jsonObject.put("commission","总代未设置");
                jsonObject.put("data",CommonConst.NUMBER_0);
                return ResponseUtil.success(jsonObject);
            }else{
                BigDecimal multiply = byProxyUserId.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100);
                commission = MessageFormat.format(messageUtil.get(METHOD_SECOND_FORMAT)+METHOD_PERCENTAGE,CommonConst.BIGDECIMAL_100.subtract(multiply));
                jsonObject.put("commission",commission);
                jsonObject.put("data",(byProxyUserId==null&&byProxyUserId.getSecondCommission()==null)?CommonConst.NUMBER_0:byProxyUserId.getSecondCommission().multiply(CommonConst.BIGDECIMAL_100));
                return ResponseUtil.success(jsonObject);
            }
        }
    }

    @ApiOperation("单个修改代理分成比")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "点击列id", required = true),
            @ApiImplicitParam(name = "bigDecimal", value = "返佣百分比，100传1，50传0.5", required = true),
    })
    @PostMapping("updateProxyCommission")
    @Transactional
    public ResponseEntity updateProxyCommission(Long id, String bigDecimal){
        if (DateUtil.verifyTime()){
            return ResponseUtil.custom("0点到1点不能修改该配置");
        }
        if (CasinoProxyUtil.checkNull(bigDecimal,id)){
            return ResponseUtil.custom("参数不合法");
        }
        BigDecimal money = CommonUtil.checkMoney(bigDecimal);
        if(money.compareTo(BigDecimal.ZERO)<CommonConst.NUMBER_1){
            return ResponseUtil.custom("返佣百分比类型错误");
        }
        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_1)) >= CommonConst.NUMBER_1){
            return ResponseUtil.custom("超过最高占成比");
        }
        ProxyUser proxyUser = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(proxyUser)){
            return ResponseUtil.custom("没有这个代理");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (byId.getProxyRole() == CommonConst.NUMBER_3 ||(byId.getProxyRole() == CommonConst.NUMBER_2 && proxyUser.getProxyRole() == CommonConst.NUMBER_2)){
            return ResponseUtil.custom("没有权限");
        }
        ProxyCommission byProxyUserId = proxyCommissionService.findByProxyUserId(proxyUser.getId());
        if (CasinoProxyUtil.checkNull(byProxyUserId)){
            byProxyUserId = new ProxyCommission();
            byProxyUserId.setProxyUserId(proxyUser.getId());
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
            List<ProxyCommission> bySecondProxy = proxyCommissionService.findBySecondProxy(proxyUser.getId());
            if (!CasinoProxyUtil.checkNull(bySecondProxy) && bySecondProxy.size() > CommonConst.NUMBER_0){
                //减少总代分成，直接加区域代理分成
                if(money.compareTo(byProxyUserId.getFirstCommission()) == -1){
                    for (ProxyCommission p:bySecondProxy){
                        BigDecimal subtract = byProxyUserId.getFirstCommission().subtract(money);
                        p.setFirstCommission(money);
                        if ((money.add(p.getSecondCommission())).compareTo(CommonConst.BIGDECIMAL_1) >= CommonConst.NUMBER_0){
                            p.setSecondCommission(CommonConst.BIGDECIMAL_1.subtract(money));
                            p.setThirdCommission(BigDecimal.ZERO);
                        }else {
                            p.setSecondCommission(p.getSecondCommission().add(subtract));
                            p.setThirdCommission(CommonConst.BIGDECIMAL_1.subtract(money).subtract(p.getSecondCommission()));
                        }
                    }
                    //增加总代分成，直接减区域代理分成，不够在减基层代理
                }else if (money.compareTo(byProxyUserId.getFirstCommission()) == CommonConst.NUMBER_1){
                    for (ProxyCommission p:bySecondProxy){
                        BigDecimal subtract = money.subtract(byProxyUserId.getFirstCommission());
                        p.setFirstCommission(money);
                        if (p.getSecondCommission().compareTo(subtract) >= CommonConst.NUMBER_0){
                            p.setSecondCommission(p.getSecondCommission().subtract(subtract));
                            p.setThirdCommission(CommonConst.BIGDECIMAL_1.subtract(money).subtract(p.getSecondCommission()));
                        }else {
                            p.setSecondCommission(BigDecimal.ZERO);
                            p.setThirdCommission(CommonConst.BIGDECIMAL_1.subtract(money).subtract(p.getSecondCommission()));
                        }
                    }
                }else if (money.compareTo(byProxyUserId.getFirstCommission()) == CommonConst.NUMBER_0){
                    return ResponseUtil.success(byProxyUserId);
                }
                bySecondProxy.stream().forEach(proxyCommission ->{
                    proxyCommissionService.save(proxyCommission);
                });
            }
            byProxyUserId.setFirstCommission(money);
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_3){
            ProxyCommission byProxyUserId1 = proxyCommissionService.findByProxyUserId(proxyUser.getSecondProxy());
            if (CasinoProxyUtil.checkNull(byProxyUserId1) || CasinoProxyUtil.checkNull(byProxyUserId1.getFirstCommission()) || byProxyUserId1.getFirstCommission().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_1){
                return ResponseUtil.custom("请先设置总代");
            }
            byProxyUserId.setSecondProxy(proxyUser.getSecondProxy());
            byProxyUserId.setSecondCommission(money);
            byProxyUserId.setFirstCommission(byProxyUserId1.getFirstCommission());
            money = money.add(byProxyUserId.getFirstCommission());
            if (money.compareTo(new BigDecimal(CommonConst.NUMBER_1)) >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("超过最高占成比");
            }
            byProxyUserId.setThirdCommission(new BigDecimal(CommonConst.NUMBER_1).subtract(money));
        }else {
            return ResponseUtil.custom("不能直接设置总代");
        }
        ProxyCommission save = proxyCommissionService.save(byProxyUserId);
        return ResponseUtil.success(save);
    }

    @ApiOperation("删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("delete")
    @Transactional
    public ResponseEntity delete(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findProxyUserById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getIsDelete() == CommonConst.NUMBER_2){
            return ResponseUtil.custom("该代理已经删除");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            ProxyUser proxyUser = new ProxyUser();
            proxyUser.setFirstProxy(byId.getId());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
            if(!CasinoProxyUtil.checkNull(proxyUsers) && proxyUsers.size() >= CommonConst.NUMBER_2){
                return ResponseUtil.custom("该代理的下级仍未转移");
            }
        }else if (byId.getProxyRole() == CommonConst.NUMBER_2){
            ProxyUser proxyUser = new ProxyUser();
            proxyUser.setSecondProxy(byId.getId());
            proxyUser.setProxyRole(CommonConst.NUMBER_3);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
            if(!CasinoProxyUtil.checkNull(proxyUsers) && proxyUsers.size() >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("该代理的下级仍未转移");
            }
            proxyUserService.subProxyUsersNum(byId.getFirstProxy());
        }else {
            User user = new User();
            user.setThirdProxy(id);
            List<User> userList = userService.findUserList(user, null, null);
            if ( !CasinoProxyUtil.checkNull(userList) && userList.size() >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("该代理会员尚未转移");
            }
            proxyUserService.subProxyUsersNum(byId.getFirstProxy());
            proxyUserService.subProxyUsersNum(byId.getSecondProxy());
        }
        byId.setIsDelete(CommonConst.NUMBER_2);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }


    @ApiOperation("转移会员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "被转移者id(选中列id)", required = true),
            @ApiImplicitParam(name = "acceptId", value = "接受者id", required = true),
    })
    @GetMapping("transferUser")
    @Transactional
    public ResponseEntity transferUser(Long id,Long acceptId){
        if (CasinoProxyUtil.checkNull(id,acceptId)){
            return ResponseUtil.custom("参数不合法");
        }
        if (id.toString().equals(acceptId.toString())){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        ProxyUser accept = proxyUserService.findById(acceptId);
        if (CasinoProxyUtil.checkNull(byId,accept)){
            return ResponseUtil.custom("没有这个代理");
        }
        if ( byId.getProxyRole() != CommonConst.NUMBER_3 || accept.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("只有基层代理可以转移会员");
        }
        if (!byId.getSecondProxy().equals(accept.getSecondProxy())){
            return ResponseUtil.custom("不能跨区域代理转移");
        }
        log.info("proxy开始转移会员被转移者{} 接受者{}",byId.getUserName(),accept.getUserName());
        try {
            return proxyHomePageReportBusiness.transferUser(id,acceptId,accept);
        }catch (Exception ex){
            return ResponseUtil.custom("转移失败请联系管理员");
        }
    }

    @ApiOperation("转移会员获取下拉框数据")
    @GetMapping("getTransferUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "选中列id", required = true),
    })
    public ResponseEntity<ProxyUser> getTransferUser(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("只有基层代理可以转移会员");
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

    @ApiOperation("转移代理获取下拉框数据")
    @GetMapping("getTransferProxy")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "选中列id", required = true),
    })
    public ResponseEntity<ProxyUser> getTransferProxy(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_3){
            return ResponseUtil.custom("基层代理不能转移下级");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            return ResponseUtil.custom("没有权限");
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

    @ApiOperation("转移代理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "被转移者id(选中列id)", required = true),
            @ApiImplicitParam(name = "acceptId", value = "接受者id", required = true),
    })
    @GetMapping("transferProxy")
    public ResponseEntity transferProxy(Long id,Long acceptId){
        if (CasinoProxyUtil.checkNull(id,acceptId) || id.toString().equals(acceptId.toString())){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        ProxyUser accept = proxyUserService.findById(acceptId);
        if (CasinoProxyUtil.checkNull(byId,accept)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getProxyRole() != accept.getProxyRole()){
            return ResponseUtil.custom("只有同级才可以互转");
        }
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            return ResponseUtil.custom("参数不合法");
        }
        log.info("proxy开始转移代理被转移者{} 接受者{}",byId.getUserName(),accept.getUserName());
        try {
            return proxyHomePageReportBusiness.transferProxy(byId,accept);
        }catch (Exception ex){
            return ResponseUtil.custom("转移失败请联系管理员");
        }
    }
}
