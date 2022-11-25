package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/bankcard")
@Api(tags = "用户中心")
public class BankCardsController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private UserService userService;
    @Autowired
    private PlatformConfigService platformConfigService;

    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ResponseBody
    public ResponseEntity<List<BankInfo>> bankList() {
        BankInfo bankInfo=new BankInfo();
        bankInfo.setDisable(0);
        return ResponseUtil.success(bankInfoService.findAll(bankInfo));
    }

//    @GetMapping("/boundList")
//    @ApiOperation("用户已绑定银行卡列表")
//    @ResponseBody
//    public ResponseEntity boundList() {
//        Long userId =  CasinoWebUtil.getAuthId();
//        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
//        return ResponseUtil.success(bankcardsList);
//    }

    @PostMapping("/bound")
    @ApiOperation("用户增加银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
            @ApiImplicitParam(name = "realName", value = "持卡人姓名")})
    @Transactional
    public ResponseEntity<Bankcards> bound(String bankId, String bankAccount, String address, String realName){
        String checkParamFroBound = Bankcards.checkParamFroBound(bankId, bankAccount, address);
        if (StringUtils.hasLength(checkParamFroBound)) {
            return ResponseUtil.custom(checkParamFroBound);
        }
        Long userId = CasinoWebUtil.getAuthId();
        Bankcards firstBankcard = bankcardsService.findBankCardsInByUserId(userId);
        if (firstBankcard == null && ObjectUtils.isEmpty(realName)) {
            return ResponseUtil.custom("持卡人不能为空");
        }
        if(isGreatThan6()){
            return ResponseUtil.custom("最多只能添加6张银行卡");
        }
        List<Bankcards> bankcardsList = bankcardsService.findByBankAccount(bankAccount);
        if (!CollectionUtils.isEmpty(bankcardsList)) {
            return ResponseUtil.custom("该卡号银行卡已被绑定");
        }
//        Bankcards checkBankcards = bankcardsService.findByUserIdAndBankAccount(userId, bankAccount);
//        if (checkBankcards != null) {
//            return ResponseUtil.custom("该卡号银行卡已添加,请勿重复添加");
//        }
        User user = userService.findById(userId);
        String userRealName = user.getRealName();
        Bankcards bankcards = boundCard(firstBankcard,bankId,bankAccount,address,realName,user);
        //银行卡绑定 同名只能绑定一个账号
        boolean bankcardRealNameSwitch = checkBankcardRealNameSwitch(bankcards.getRealName(), userId);
        if (!bankcardRealNameSwitch) {
            return ResponseUtil.custom("同一个持卡人只能绑定一个账号");
        }
        bankcards= bankcardsService.boundCard(bankcards);
        //把真实姓名保存到user
        if (!ObjectUtils.isEmpty(user.getRealName()) && !user.getRealName().equals(userRealName)) {
            userService.save(user);
        }
        return ResponseUtil.success(bankcards);
    }

    /**
     * 根据ID删除银行卡
     * @param id 银行卡id
     * @return
     */
    @GetMapping("/deleteBankCardById")
    @ApiOperation("根据ID删除银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", required = true),
    })
    public ResponseEntity deleteBankCardById(Long id) {
        if (CasinoWebUtil.checkNull(id)) {
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = bankcardsService.findById(id);
        if (bankcards == null) {
            return ResponseUtil.custom("当前银行卡不存在");
        }
        Long authId = CasinoWebUtil.getAuthId();
        if (!authId.equals(bankcards.getUserId())) {
            return ResponseUtil.custom("银行卡错误");
        }
        bankcardsService.delete(bankcards);
        //如果删除的是默认银行卡,重新再设置一张卡为默认卡
        if (!ObjectUtils.isEmpty(bankcards.getDefaultCard()) && bankcards.getDefaultCard() == 1) {
            List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(authId);
            if (!CollectionUtils.isEmpty(bankcardsList)) {
                Bankcards bankcards1 = bankcardsList.get(0);
                bankcards1.setDefaultCard(1);
                bankcardsService.boundCard(bankcards1);
            }
        }
        return ResponseUtil.success();
    }

    /**
     * 根据ID设置默认银行卡
     * @param id 银行卡id
     * @return
     */
    @PostMapping("/setDefaultBankCardById")
    @ApiOperation("根据ID设置默认银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", required = true),
    })
    public ResponseEntity setDefaultBankCardById(Long id) {
        if (CasinoWebUtil.checkNull(id)) {
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = bankcardsService.findById(id);
        if (bankcards == null) {
            return ResponseUtil.custom("当前银行卡不存在");
        }
        Long authId = CasinoWebUtil.getAuthId();
        if (!authId.equals(bankcards.getUserId())) {
            return ResponseUtil.custom("银行卡错误");
        }
        Bankcards defaultBankcards = findDefaultCardByUserId(authId);
        if (defaultBankcards != null) {
            if (defaultBankcards.getId().equals(bankcards.getId())) {
                return ResponseUtil.success();
            }
            defaultBankcards.setDefaultCard(0);
            bankcardsService.boundCard(defaultBankcards);
        }
        bankcards.setDefaultCard(1);
        bankcardsService.boundCard(bankcards);
        return ResponseUtil.success();
    }

    private Bankcards findDefaultCardByUserId(Long userId) {
        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        if (CollectionUtils.isEmpty(bankcardsList)) {
            return null;
        }
        for (Bankcards bankcards : bankcardsList) {
            if (bankcards.getDefaultCard() != null && bankcards.getDefaultCard() == 1) {
                return bankcards;
            }
        }
        return null;
    }

    private boolean isGreatThan6(){
        Long userId = CasinoWebUtil.getAuthId();
        int count = bankcardsService.countByUserId(userId);
        return count>=6;
    }

    private Bankcards boundCard(Bankcards firstBankcard,String bankId, String bankAccount, String address, String realName,User user){
        Date now = new Date();
        Bankcards bankcards = new Bankcards();
        bankcards.setUserId(user.getId());
        bankcards.setBankId(bankId);
        bankcards.setBankAccount(bankAccount);
        bankcards.setAddress(address);
        bankcards.setUpdateTime(now);
        bankcards.setCreateTime(now);
//        bankcards.setDisable(0);
        bankcards.setDefaultCard(isFirstCard(firstBankcard));
        setRealNameAndProxy(bankcards,realName,user);
        return bankcards;
    }

    private void setRealNameAndProxy(Bankcards bankcards,String realName,User user){
        bankcards.setFirstProxy(user.getFirstProxy());
        bankcards.setSecondProxy(user.getSecondProxy());
        bankcards.setThirdProxy(user.getThirdProxy());
        if(ObjectUtils.isEmpty(user.getRealName())){
            user.setRealName(realName);
            bankcards.setRealName(realName);
        }else{
            bankcards.setRealName(user.getRealName());
        }
    }

    private Integer isFirstCard(Bankcards bankcards){
        return bankcards==null?1:0;
    }

    /**
     * 银行卡绑定 同名只能绑定一个账号   默认开
     * @param realName
     * @param userId
     * @return
     */
    private boolean checkBankcardRealNameSwitch(String realName, Long userId) {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        boolean bankcardRealNameSwitch = PlatformConfig.checkBankcardRealNameSwitch(platformConfig);
        if (!bankcardRealNameSwitch) {
            return true;
        }
        List<Bankcards> checkRealNameList = bankcardsService.findByRealName(realName);
        if (CollectionUtils.isEmpty(checkRealNameList)) {
            return true;
        }
        //可能存在一个名字属于多个账号，只要有一个账号匹配的上就可以
        for (Bankcards checkRealName : checkRealNameList) {
            if (checkRealName.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
