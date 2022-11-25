package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.BankcardsVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private PlatformConfigService platformConfigService;
    /**
     * 查询所有银行列表
     * @return
     */
    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "disable", value = "0:未禁用 1：禁用", required = false),
    })
    @NoAuthorization
    public ResponseEntity<BankInfo> bankList(Integer disable) {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setDisable(disable);
        List<BankInfo> bankInfoServiceAll = bankInfoService.findAll(bankInfo);
        if(bankInfoServiceAll != null){
            //绝对路径
            PlatformConfig platformConfig= platformConfigService.findFirst();
            String uploadUrl = platformConfig.getReadUploadUrl();
            if(uploadUrl==null) {
                return ResponseUtil.custom("请先配置图片服务器访问地址");
            }
            bankInfoServiceAll.forEach(bankInfoServiceInfo ->{
                if (bankInfoServiceInfo.getBankLogo()!=null ){
                    bankInfoServiceInfo.setBankLogo(uploadUrl+bankInfoServiceInfo.getBankLogo());
                }

            });
        }
        return ResponseUtil.success(bankInfoServiceAll);
    }
    /**
     * 新增银行
     * @param bankName 银行名称
     * @param remark 备注
     * @return
     */
    @ApiOperation("新增银行")
    @NoAuthorization
    @PostMapping(value = "/saveBankInfo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增银行")
    public ResponseEntity saveBankInfo(@RequestPart(value = "bankLogo银行图标",required=false) MultipartFile file, @RequestParam(value = "银行名称") String bankName,
                                       @RequestParam(value = "备注",required=false)String remark){
        if (LoginUtil.checkNull(bankName)){
            return ResponseUtil.custom("参数不合法");
        }
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankType(CommonConst.NUMBER_0);//默认银行卡
        bankInfo.setDisable(CommonConst.NUMBER_0);//默认禁用
        return this.saveAndUpdate(file,bankName,remark,bankInfo);
    }
    /**
     * 修改银行信息
     * @param bankName 银行名称
     * @param remark 备注
     * @param id 银行id
     * @return
     */
    @ApiOperation("修改银行")
    @PostMapping(value = "/updateBankInfo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "修改银行")
    public ResponseEntity updateBankInfo(@RequestPart(value = "bankLogo银行图标",required=false) MultipartFile file,
                                         @RequestParam(value = "银行名称") String bankName,@RequestParam(value = "备注",required=false)String remark,
                                         @RequestParam(value = "银行id")  Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        BankInfo bankInfo = bankInfoService.findById(id);
        if (LoginUtil.checkNull(bankInfo)){
            return ResponseUtil.custom("没有这个银行");
        }
        return this.saveAndUpdate(file,bankName,remark,bankInfo);

    }
    private ResponseEntity saveAndUpdate(MultipartFile file,String bankName,String remark,BankInfo bankInfo){
        bankInfo.setBankName(bankName);
        bankInfo.setRemark(remark);
        try {
            String fileUrl = null;
            if (file != null){
                PlatformConfig platformConfig= platformConfigService.findFirst();
                String uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl==null) {
                    return ResponseUtil.custom("请先配置图片服务器上传地址");
                }
               fileUrl = UploadAndDownloadUtil.fileUpload(file, uploadUrl);
            }
            bankInfo.setBankLogo(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        try {
            bankInfoService.saveBankInfo(bankInfo);
        }catch (Exception e){
            return ResponseUtil.custom("重复银行名称");
        }
        return ResponseUtil.success();
    }

    @ApiOperation("修改银行状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行id", required = true),
    })
    @PostMapping("updateBankStatus")
    public ResponseEntity updateBankStatus(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        BankInfo bankInfo = bankInfoService.findById(id);
        if (LoginUtil.checkNull(bankInfo)){
            return ResponseUtil.custom("没有这个银行");
        }
        if (bankInfo.getDisable() == CommonConst.NUMBER_1){
            bankInfo.setDisable(CommonConst.NUMBER_0);
        }else{
            bankInfo.setDisable(CommonConst.NUMBER_1);
        }
        bankInfoService.saveBankInfo(bankInfo);
        return ResponseUtil.success();
    }
    /**
     * 删除银行信息
     * @param id 银行id
     * @return
     */
    @GetMapping("/deleteBankInfo")
    @ApiOperation("删除银行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行id", required = true),
    })
    public ResponseEntity deleteBankInfo(Long id) {
        BankInfo bankInfo = bankInfoService.findById(id);
        if (LoginUtil.checkNull(bankInfo)){
            return ResponseUtil.custom("没有这个银行");
        }
        bankInfoService.deleteBankInfo(id);
        return ResponseUtil.success();
    }

    @GetMapping("/unboundBankName")
    @ApiOperation("解绑银行卡名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
    })
    @NoAuthorization
    public ResponseEntity unboundBankName(Long userId) {
        List<Bankcards> byUserId = bankcardsService.findBankcardsByUserId(userId);
        List<BankcardsDel> byUserIdList = bankcardsDelService.findByUserId(userId);
        if (LoginUtil.checkNull(byUserId) && LoginUtil.checkNull(byUserIdList)){
            return ResponseUtil.custom("该用户未绑定银行卡");
        }
        if(!LoginUtil.checkNull(byUserId) && byUserId.size() > CommonConst.NUMBER_0){
           return ResponseUtil.custom("操作失败、用户有未解绑的银行卡");
        }
        User byId = userService.findById(userId);
        byId.setRealName(null);
        userService.save(byId);
        return ResponseUtil.success("解绑银行卡实名认证成功");
    }

    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
    })
    public ResponseEntity<BankcardsVo> boundList(Long userId) {
        List<BankcardsVo> bankcardsVoList = new LinkedList<>();
        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        if (!LoginUtil.checkNull(bankcardsList) && bankcardsList.size() > CommonConst.NUMBER_0){
            for (Bankcards bankcards:bankcardsList){
                BankcardsVo vo = new BankcardsVo(bankcards);
                bankcardsVoList.add(vo);
            }
        }
        List<BankcardsDel> byUserId = bankcardsDelService.findByUserId(userId);
        if (!LoginUtil.checkNull(byUserId) && byUserId.size() > CommonConst.NUMBER_0){
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
        if(LoginUtil.checkNull(bankAccount, realName)){
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
    @PostMapping("/bound")
    @ApiOperation("用户增加银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
            @ApiImplicitParam(name = "realName", value = "持卡人姓名",required = true)})
    public ResponseEntity bound(Long userId, String bankId, String bankAccount, String address, String realName){
        String checkParamFroBound = this.checkParamFroBound(realName, bankId, bankAccount, address);
        if (!LoginUtil.checkNull(checkParamFroBound)) {
            return ResponseUtil.custom(checkParamFroBound);
        }
        if (!realName.matches(RegexEnum.NAME.getRegex())){
            return ResponseUtil.custom("持卡人请输入中文或字母");
        }
        //判断是否存在该用户
        User user = userService.findById(userId);
        if(user == null){
            return ResponseUtil.custom("不存在该会员");
        }
        boolean bankcardRealNameSwitch = checkBankcardRealNameSwitch(realName, userId);
        if (!bankcardRealNameSwitch) {
            return ResponseUtil.custom("同一个持卡人只能绑定一个账号");
        }
        //判断用户输入的姓名是否一致
        if (LoginUtil.checkNull(user.getRealName())){
            user.setRealName(realName);
            userService.save(user);
        }else {
            if (!realName.equals(user.getRealName())){
                return ResponseUtil.custom("持卡人姓名错误");
            }
        }

        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        if(bankcardsList.size() >= Constants.MAX_BANK_NUM){
            return ResponseUtil.custom("最多只能绑定6张银行卡");
        }
        bankcardsList=bankcardsList.stream().filter(v ->v.getBankAccount().equals(bankAccount)).collect(Collectors.toList());
        if(bankcardsList.size() > CommonConst.NUMBER_0){
            return ResponseUtil.custom("已经绑定这张卡了");
        }

        Bankcards bankcards = boundCard(userId, bankId,bankAccount,address,realName);
        boolean isSuccess= bankcardsService.boundCard(bankcards)==null?true:false;
        return ResponseUtil.success(isSuccess);
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

    private String checkParamFroBound(String accountName,String bankId, String bankAccount,
                                      String address) {
        if(LoginUtil.checkNull(accountName)){
            return "持卡人不能为空";
        }
        if (bankId == null) {
            return "银行id不能为空";
        }
        if (LoginUtil.checkNull(address)) {
            return "开户地址不能为空";
        }
        if (LoginUtil.checkNull(bankAccount)) {
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

    @PostMapping("/disable")
    @ApiOperation("移除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),

    })
    @Transactional
    public ResponseEntity disable(Long userId, Long bankId){
        if(LoginUtil.checkNull(userId,bankId)){
            return ResponseUtil.custom("参数错误");
        }
        //查询银行卡
        Bankcards bank = bankcardsService.findById(bankId);
        if(LoginUtil.checkNull(bank)){
            return ResponseUtil.custom("该银行卡已解绑");
        }
        BankcardsDel bankcardsDel = new BankcardsDel(bank);
        bankcardsDelService.save(bankcardsDel);
        bankcardsService.delBankcards(bank);
        return ResponseUtil.success();
    }


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
