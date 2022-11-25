package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.BankcardsDel;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsDelService;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.BankcardsVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bankcard")
@Api(tags = "资金中心")
public class BankCardsController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private UserService userService;

    @Autowired
    private BankcardsDelService bankcardsDelService;
    /**
     * 查询所有银行列表
     * @return
     */
    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "disable", value = "0:未禁用 1：禁用", required = false),
    })
    public ResponseEntity<BankInfo> bankList(Integer disable) {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setDisable(disable);
        return ResponseUtil.success(bankInfoService.findAll(bankInfo));
    }
    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
    })
    public ResponseEntity<BankcardsVo> boundList(Long userId) {
        List<BankcardsVo> bankcardsVoList = new LinkedList<>();
        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        if (!CasinoProxyUtil.checkNull(bankcardsList) && bankcardsList.size() > CommonConst.NUMBER_0){
            for (Bankcards bankcards:bankcardsList){
                BankcardsVo vo = new BankcardsVo(bankcards);
                bankcardsVoList.add(vo);
            }
        }
        List<BankcardsDel> byUserId = bankcardsDelService.findByUserId(userId);
        if (!CasinoProxyUtil.checkNull(byUserId) && byUserId.size() > CommonConst.NUMBER_0){
            for (BankcardsDel bankcards:byUserId){
                BankcardsVo vo = new BankcardsVo(bankcards);
                bankcardsVoList.add(vo);
            }
        }
        return ResponseUtil.success(bankcardsVoList);
    }

    /**
     * 可以灵活添加参数，满足后续添加的需求
     *
     * @param bankAccount
     * @param realName
     * @return
     */
    @GetMapping("/peggBankCard")
    @ApiOperation("银行卡/开户名反查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankAccount", value = "银行卡号", required = false),
            @ApiImplicitParam(name = "realName", value = "开户名", required = false),
    })
    public ResponseEntity peggBankCard(String bankAccount, String realName){
        if(CasinoProxyUtil.checkNull(bankAccount, realName)){
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = new Bankcards();
        bankcards.setBankAccount(bankAccount);
        bankcards.setRealName(realName);
        List<Bankcards> bankcardsList = bankcardsService.findUserBank(bankcards);
        return ResponseUtil.success(bankcardsList);
    }

    /**
     * 用户最多只能绑定6张银行卡
     *
     * @param userId
     * @param bankId
     * @param bankAccount
     * @param address
     * @param realName
     * @return
     */
//    @PostMapping("/bound")
//    @ApiOperation("用户增加银行卡")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
//            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
//            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
//            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
//            @ApiImplicitParam(name = "realName", value = "持卡人姓名",required = true)})
//    public ResponseEntity bound(Long userId, String bankId, String bankAccount, String address, String realName){
//        String checkParamFroBound = this.checkParamFroBound(realName, bankId, bankAccount, address);
//        if (!CasinoProxyUtil.checkNull(checkParamFroBound)) {
//            return ResponseUtil.custom(checkParamFroBound);
//        }
//        if (!realName.matches(RegexEnum.NAME.getRegex())){
//            return ResponseUtil.custom("持卡人请输入中文或字母");
//        }
//        //判断是否存在该用户
//        User user = userService.findById(userId);
//        if(user == null){
//            return ResponseUtil.custom("不存在该会员");
//        }
//        //判断用户输入的姓名是否一致
//        if (CasinoProxyUtil.checkNull(user.getRealName())){
//            user.setRealName(realName);
//            userService.save(user);
//        }else {
//            if (!realName.equals(user.getRealName())){
//                return ResponseUtil.custom("持卡人姓名错误");
//            }
//        }
//
//        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
//        if(bankcardsList.size() >= Constants.MAX_BANK_NUM){
//            return ResponseUtil.custom("最多只能绑定6张银行卡");
//        }
//        bankcardsList=bankcardsList.stream().filter(v ->v.getBankAccount().equals(bankAccount)).collect(Collectors.toList());
//        if(bankcardsList.size() > CommonConst.NUMBER_0){
//            return ResponseUtil.custom("已经绑定这张卡了");
//        }
//
//        Bankcards bankcards = boundCard(userId, bankId,bankAccount,address,realName);
//        boolean isSuccess= bankcardsService.boundCard(bankcards)==null?true:false;
//        return ResponseUtil.success(isSuccess);
//    }

    private String checkParamFroBound(String accountName,String bankId, String bankAccount,
                                      String address) {
        if(CasinoProxyUtil.checkNull(accountName)){
            return "持卡人不能为空";
        }
        if (bankId == null) {
            return "银行id不能为空";
        }
        if (CasinoProxyUtil.checkNull(address)) {
            return "开户地址不能为空";
        }
        if (CasinoProxyUtil.checkNull(bankAccount)) {
            return "银行账号不能为空";
        }
        if (bankAccount.length() > 20 || bankAccount.length() < 8) {
            return "长度只能在8~20位";
        }
        if (!bankAccount.matches(Constants.regex)) {
            return "银行账号只能输入数字";
        }
        return null;
    }

//    @PostMapping("/disable")
//    @ApiOperation("移除")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
//            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
//
//    })
//    @Transactional
//    public ResponseEntity disable(Long userId, Long bankId){
//        if(CasinoProxyUtil.checkNull(userId,bankId)){
//            return ResponseUtil.custom("参数错误");
//        }
//        //查询银行卡
//        Bankcards bank = bankcardsService.findById(bankId);
//        if(CasinoProxyUtil.checkNull(bank)){
//            return ResponseUtil.custom("该银行卡已解绑");
//        }
//        BankcardsDel bankcardsDel = new BankcardsDel(bank);
//        bankcardsDelService.save(bankcardsDel);
//        bankcardsService.delBankcards(bank);
//        return ResponseUtil.success();
//    }


    private Bankcards boundCard(Long userId, String bankId, String bankAccount, String address, String realName){
        Bankcards firstBankcard = bankcardsService.findBankCardsInByUserId(userId);
        Bankcards bankcards = new Bankcards();
        bankcards.setUserId(userId);
        bankcards.setBankId(bankId);
        bankcards.setBankAccount(bankAccount);
        bankcards.setAddress(address);
        bankcards.setRealName(realName);
        bankcards.setDefaultCard(isFirstCard(firstBankcard));
        return bankcards;
    }

    private String getRealName(Bankcards bankcards, String realName){
        return bankcards==null?realName:bankcards.getRealName();
    }

    private Integer isFirstCard(Bankcards bankcards){
        return bankcards==null?1:0;
    }




}
