package com.qianyi.casinoproxy.controller.jiceng;

import com.alibaba.fastjson.JSONObject;
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
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulecommon.util.MessageUtil;
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

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "基层代理代理中心")
@RestController
@RequestMapping("proxyUser/jiceng")
public class ThridProxyUserController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageUtil messageUtil;

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
        proxyUser.setId(authId);
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable, proxyUser, startDate, endDate);
        PageResultVO<ProxyUserVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<ProxyUser> proxyUserList = proxyUserPage.getContent();
        if(proxyUserList != null && proxyUserList.size() > 0){
            List<ProxyUserVo> userVoList = new LinkedList();
            List<Long> ids = new ArrayList<>();
            ids.add(proxyUserList.get(CommonConst.NUMBER_0).getSecondProxy());
            ids.add(proxyUserList.get(CommonConst.NUMBER_0).getFirstProxy());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(ids);
            List<Long> proxyIds = proxyUserList.stream().map(ProxyUser::getId).collect(Collectors.toList());
            List<ProxyCommission> proxyCommissions = proxyCommissionService.findProxyUser(proxyIds);
            if (proxyUsers != null){
                proxyUserList.stream().forEach(u -> {
                    ProxyUserVo proxyUserVo = new ProxyUserVo(u);
                    proxyUsers.stream().forEach(proxy->{
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getSecondProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getFirstProxy().equals(proxy.getId())){
                            proxyUserVo.setFirstProxyAccount(proxy.getUserName());
                        }
                    });
                    proxyCommissions.stream().forEach(proxyCommission->{
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getId().equals(proxyCommission.getProxyUserId())){
                            String commissionRatio = MessageFormat.format(messageUtil.get(CommonConst.JICENG),
                                    proxyCommission.getThirdCommission() == null?"0":proxyCommission.getThirdCommission().multiply(CommonConst.BIGDECIMAL_100));
                            proxyUserVo.setCommissionRatio(commissionRatio);
                        }

                    });
                    User user = new User();
                    user.setThirdProxy(u.getId());
                    Long  userCount  = userService.findUserCount(user,null,null);
                    proxyUserVo.setUsersNum(Math.toIntExact(userCount));
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
//    @ApiOperation("添加代理")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userName", value = "账号", required = true),
//            @ApiImplicitParam(name = "nickName", value = "用户昵称", required = true),
//    })
//    @PostMapping("saveProxyUser")
//    @Transactional
//    public ResponseEntity saveProxyUser(String userName, String nickName){
//        if (CasinoProxyUtil.checkNull(userName,nickName)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        if (!userName.matches(RegexEnum.ACCOUNT.getRegex())){
//            return ResponseUtil.custom("账号请输入数字或字母");
//        }
//        if (!nickName.matches(RegexEnum.NAME.getRegex())){
//            return ResponseUtil.custom("昵称请输入1~20位中文或字母");
//        }
//        ProxyUser byUserName = proxyUserService.findByUserName(userName);
//        if (!CasinoProxyUtil.checkNull(byUserName)){
//            return ResponseUtil.custom("代理账号重复");
//        }
//        Long authId = CasinoProxyUtil.getAuthId();
//        ProxyUser byId = proxyUserService.findById(authId);
//        if (byId.getProxyRole() == CommonConst.NUMBER_3){
//            return ResponseUtil.custom("基层代理不能创建代理");
//        }
//        Integer proxyRole = byId.getProxyRole() + CommonConst.NUMBER_1;
//        ProxyUser proxyUser = new ProxyUser();
//        if (byId.getProxyRole() == CommonConst.NUMBER_1){
//            proxyUser.setFirstProxy(byId.getId());
//        }else {
//            proxyUser.setFirstProxy(byId.getFirstProxy());
//            proxyUser.setSecondProxy(byId.getId());
//            proxyUser.setProxyCode(LoginUtil.getProxyCode());
//        }
//        proxyUser.setUserName(userName);
//        proxyUser.setNickName(nickName);
//        //随机生成
//        String password = PasswordUtil.getRandomPwd();
//        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
//        proxyUser.setPassWord(bcryptPassword);
//        proxyUser.setProxyRole(proxyRole);
//        proxyUser.setUserFlag(CommonConst.NUMBER_1);
//        proxyUser.setIsDelete(CommonConst.NUMBER_1);
//        ProxyUser saveProxyUser = proxyUserService.save(proxyUser);
//        if (saveProxyUser.getProxyRole() == CommonConst.NUMBER_2 && !CasinoProxyUtil.checkNull(saveProxyUser)){
//            saveProxyUser.setSecondProxy(saveProxyUser.getId());
//            proxyUserService.save(saveProxyUser);
//            this.createrProxyCommission(saveProxyUser,saveProxyUser.getProxyRole());
//        }else if(!CasinoProxyUtil.checkNull(saveProxyUser)){
//            this.createrProxyCommission(saveProxyUser,saveProxyUser.getProxyRole());
//        }
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("account", userName);
//        jsonObject.put("password", password);
//        return ResponseUtil.success(jsonObject);
//    }
//
//    private void createrProxyCommission(ProxyUser saveProxyUser,Integer proxyRole){
//        ProxyCommission proxyCommission = new ProxyCommission();
//        proxyCommission.setProxyUserId(saveProxyUser.getId());
//        if (proxyRole == CommonConst.NUMBER_3){
//            ProxyCommission secondCommission = proxyCommissionService.findByProxyUserId(saveProxyUser.getSecondProxy());
//            proxyCommission.setSecondProxy(saveProxyUser.getSecondProxy());
//            proxyCommission.setFirstCommission((secondCommission == null || secondCommission.getFirstCommission() == null)?BigDecimal.ZERO:secondCommission.getFirstCommission());
//        }
//        proxyCommissionService.save(proxyCommission);
//    }
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
    @ApiOperation("删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("delete")
    public ResponseEntity delete(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if ( byId.getProxyRole() == CommonConst.NUMBER_1 ){
            return ResponseUtil.custom("总代不能删除");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(byId.getId());
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        if (proxyUserList != null && proxyUserList.size() > CommonConst.NUMBER_1){
            return ResponseUtil.custom("该代理的下级仍未转移");
        }
        byId.setIsDelete(CommonConst.NUMBER_2);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
//    @ApiOperation("查询单个代理分成比")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "点击列id", required = true),
//    })
//    @GetMapping("findProxyCommission")
//    public ResponseEntity findProxyCommission(Long id){
//        if (CasinoProxyUtil.checkNull(id)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        String commission = "";
//        ProxyUser proxyUser = proxyUserService.findById(id);
//        Long authId = CasinoProxyUtil.getAuthId();
//        ProxyUser byId = proxyUserService.findById(authId);
//        if (CasinoProxyUtil.checkNull(byId,proxyUser)){
//            return ResponseUtil.custom("没有这个代理");
//        }
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
//            return ResponseUtil.custom("不能直接设置总代");
//        }
//        if (byId.getProxyRole() == CommonConst.NUMBER_3 ||(byId.getProxyRole() == CommonConst.NUMBER_2 && proxyUser.getProxyRole() == CommonConst.NUMBER_2)){
//            return ResponseUtil.custom("没有权限");
//        }
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
//            commission = MessageFormat.format(messageUtil.get(METHOD_FIRST_FORMAT),"");
//            return ResponseUtil.success(commission);
//        }
//        ProxyCommission byProxyUserId = proxyCommissionService.findByProxyUserId(proxyUser.getId());
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_3){
//            if (CasinoProxyUtil.checkNull(byProxyUserId) || CasinoProxyUtil.checkNull(byProxyUserId.getFirstCommission())){
//                return ResponseUtil.success("总代未设置");
//            }else{
//                BigDecimal multiply = byProxyUserId.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100);
//                commission = MessageFormat.format(messageUtil.get(METHOD_SECOND_FORMAT)+METHOD_PERCENTAGE,CommonConst.BIGDECIMAL_100.subtract(multiply));
//                return ResponseUtil.success(commission);
//            }
//        }
//        return ResponseUtil.success(commission);
//    }

//    @ApiOperation("单个修改代理分成比")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "点击列id", required = true),
//            @ApiImplicitParam(name = "bigDecimal", value = "返佣百分比，100传1，50传0.5", required = true),
//    })
//    @PostMapping("updateProxyCommission")
//    @Transactional
//    public ResponseEntity updateProxyCommission(Long id, String bigDecimal){
//        if (DateUtil.verifyTime()){
//            return ResponseUtil.custom("0点到1点不能修改该配置");
//        }
//        if (CasinoProxyUtil.checkNull(bigDecimal,id)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        BigDecimal money = CommonUtil.checkMoney(bigDecimal);
//        if(money.compareTo(BigDecimal.ZERO)<CommonConst.NUMBER_1){
//            return ResponseUtil.custom("返佣百分比类型错误");
//        }
//        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_1)) >= CommonConst.NUMBER_1){
//            return ResponseUtil.custom("超过最高占成比!");
//        }
//        ProxyUser proxyUser = proxyUserService.findById(id);
//        if (CasinoProxyUtil.checkNull(proxyUser)){
//            return ResponseUtil.custom("没有这个代理");
//        }
//        Long authId = CasinoProxyUtil.getAuthId();
//        ProxyUser byId = proxyUserService.findById(authId);
//        if (byId.getProxyRole() == CommonConst.NUMBER_3 ||(byId.getProxyRole() == CommonConst.NUMBER_2 && proxyUser.getProxyRole() == CommonConst.NUMBER_2)){
//            return ResponseUtil.custom("没有权限");
//        }
//        ProxyCommission byProxyUserId = proxyCommissionService.findByProxyUserId(proxyUser.getId());
//        if (CasinoProxyUtil.checkNull(byProxyUserId)){
//            byProxyUserId = new ProxyCommission();
//            byProxyUserId.setProxyUserId(proxyUser.getId());
//        }
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
//            BigDecimal total = money.add(byProxyUserId.getSecondCommission()).add(byProxyUserId.getThirdCommission());
//            if (total.compareTo(new BigDecimal(CommonConst.NUMBER_1)) >= CommonConst.NUMBER_1){
//                return ResponseUtil.custom("超过最高占成比!");
//            }
//            List<ProxyCommission> bySecondProxy = proxyCommissionService.findBySecondProxy(proxyUser.getId());
//            if (!CasinoProxyUtil.checkNull(bySecondProxy) && bySecondProxy.size() > CommonConst.NUMBER_0){
//                for (ProxyCommission p:bySecondProxy){
//                    BigDecimal add = p.getSecondCommission().add(money);
//                    if (add.compareTo(new BigDecimal(CommonConst.NUMBER_1)) >= CommonConst.NUMBER_1){
//                        return ResponseUtil.custom("超过最高占成比!");
//                    }
//                    p.setFirstCommission(money);
//                    p.setSecondCommission(new BigDecimal(CommonConst.NUMBER_1).subtract(add));
//                    p.setThirdCommission(BigDecimal.ZERO);
//                }
//                bySecondProxy.stream().forEach(proxyCommission ->{
//                    proxyCommissionService.save(proxyCommission);
//                });
//            }
//            byProxyUserId.setFirstCommission(money);
//        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_3){
//            ProxyCommission byProxyUserId1 = proxyCommissionService.findByProxyUserId(proxyUser.getSecondProxy());
//            if (CasinoProxyUtil.checkNull(byProxyUserId1) || CasinoProxyUtil.checkNull(byProxyUserId1.getFirstCommission()) || byProxyUserId1.getFirstCommission().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_1){
//                return ResponseUtil.custom("请先设置总代");
//            }
//            byProxyUserId.setSecondProxy(proxyUser.getSecondProxy());
//            byProxyUserId.setSecondCommission(money);
//            byProxyUserId.setFirstCommission(byProxyUserId1.getFirstCommission());
//            money = money.add(byProxyUserId.getFirstCommission());
//            if (money.compareTo(new BigDecimal(CommonConst.NUMBER_1)) >= CommonConst.NUMBER_1){
//                return ResponseUtil.custom("超过最高占成比!");
//            }
//            byProxyUserId.setThirdCommission(new BigDecimal(CommonConst.NUMBER_1).subtract(money));
//        }else {
//            return ResponseUtil.custom("不能直接设置总代");
//        }
//        ProxyCommission save = proxyCommissionService.save(byProxyUserId);
//        return ResponseUtil.success(save);
//    }
}
