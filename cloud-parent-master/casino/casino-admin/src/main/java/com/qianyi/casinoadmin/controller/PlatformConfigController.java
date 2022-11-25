package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.install.Initialization;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.*;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.CustomerConfigureService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("platformConfig")
@Api(tags = "运营中心")
@Slf4j
public class PlatformConfigController {

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private Initialization initialization;

    @Autowired
    private BankInfoService bankInfoService;
    @Autowired
    private CustomerConfigureService customerConfigureService;

    @Autowired
    private MessageUtil messageUtil;

    @ApiOperation("玩家推广返佣配置查询")
    @GetMapping("/findCommission")
    public ResponseEntity<UserCommissionVo> findAll(){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        UserCommissionVo userCommissionVo = null;
        for (PlatformConfig platformConfig : platformConfigList) {
            userCommissionVo = UserCommissionVo.builder()
                    .name(messageUtil.get("玩家推广返佣配置"))
                    .id(platformConfig.getId())
                    .firstCommission(platformConfig.getFirstCommission())
                    .secondCommission(platformConfig.getSecondCommission())
                    .thirdCommission(platformConfig.getThirdCommission())
                    .build();
        }
        return new ResponseEntity(ResponseCode.SUCCESS, userCommissionVo);
    }
    @ApiOperation("编辑玩家推广返佣配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "firstCommission", value = "一级代理返佣", required = true),
            @ApiImplicitParam(name = "secondCommission", value = "二级代理返佣", required = true),
            @ApiImplicitParam(name = "thirdCommission", value = "三级代理返佣", required = true)
    })
    @PostMapping("/updateCommission")
    public ResponseEntity<UserCommissionVo> update(BigDecimal firstCommission, BigDecimal secondCommission, BigDecimal thirdCommission){
        if (LoginUtil.checkNull(firstCommission,secondCommission,thirdCommission)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(!LoginUtil.checkNull(platformConfig)){
            platformConfig.setFirstCommission(firstCommission);
            platformConfig.setSecondCommission(secondCommission);
            platformConfig.setThirdCommission(thirdCommission);
            platformConfig.setCommissionUpdate(new Date());
            platformConfigService.save(platformConfig);
        }else{
            PlatformConfig platform = new PlatformConfig();
            platform.setFirstCommission(firstCommission);
            platform.setSecondCommission(secondCommission);
            platform.setThirdCommission(thirdCommission);
            platform.setCommissionUpdate(new Date());
            platformConfigService.save(platform);
        }
        return new ResponseEntity(ResponseCode.SUCCESS);
    }

    @ApiOperation("查询域名配置")
    @GetMapping("/findDomainName")
    public ResponseEntity<DomainNameVo> findDomainNameVo(){
        PlatformConfig first = platformConfigService.findFirst();
        DomainNameVo domainNameVo = new DomainNameVo();
        if (!LoginUtil.checkNull(first)){
            domainNameVo.setId(first.getId());
            domainNameVo.setName(messageUtil.get("域名配置"));
            domainNameVo.setDomainNameConfiguration(first.getDomainNameConfiguration());
            domainNameVo.setProxyConfiguration(first.getProxyConfiguration());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, domainNameVo);
    }

    @ApiOperation("编辑域名配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "domainNameConfiguration", value = "首页域名配置", required = true),
            @ApiImplicitParam(name = "proxyConfiguration", value = "推广注册域名配置", required = true),
    })
    @PostMapping("/updateDomainName")
    public ResponseEntity updateDomainName(Long id,String domainNameConfiguration, String proxyConfiguration){
        if (LoginUtil.checkNull(id,domainNameConfiguration, proxyConfiguration)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setDomainNameConfiguration(domainNameConfiguration);
        first.setProxyConfiguration(proxyConfiguration);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }

    @ApiOperation("查询注册开关配置")
    @GetMapping("/findRegisterSwitch")
    public ResponseEntity<RegisterSwitchVo> findRegisterSwitchVo(){
        PlatformConfig first = platformConfigService.findFirst();
        RegisterSwitchVo registerSwitchVo = new RegisterSwitchVo();
        if (!LoginUtil.checkNull(first)){
            registerSwitchVo.setId(first.getId());
            registerSwitchVo.setName(messageUtil.get("注册开关"));
            registerSwitchVo.setRegisterSwitch(first.getRegisterSwitch());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, registerSwitchVo);
    }

    @ApiOperation("编辑注册开关配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "registerSwitch", value = "注册开关配置", required = true),
    })
    @PostMapping("/updateRegisterSwitch")
    public ResponseEntity updateRegisterSwitch(Long id,Integer registerSwitch){
        if (LoginUtil.checkNull(id,registerSwitch)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setRegisterSwitch(registerSwitch);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


    /**
     * 短信风险警戒线查询
     * @return
     */
    @ApiOperation("短信余额警戒线查询")
    @GetMapping("/findMessageBalance")
    @NoAuthorization
    public ResponseEntity<PlatformConfig> findMessageBalance(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        SendMessageVo sendMessageVo = new SendMessageVo();
        BigDecimal sendMessageWarning = platformConfig.getSendMessageWarning();
        sendMessageVo.setSendMessageWarning(sendMessageWarning);
        return ResponseUtil.success(sendMessageVo);
    }

    /**
     * 编辑短信余额风险警戒线
     * @param sendMessageWarning 短信余额风险警戒值
     * @return
     */
    @ApiOperation("编辑短信余额风险警戒线")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sendMessageWarning", value = "短信余额风险警戒值", required = false),
    })
    @PostMapping("/updateMessageBalance")
    public ResponseEntity uodateMessageBalance(BigDecimal sendMessageWarning){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }

        if (!LoginUtil.checkNull(sendMessageWarning)){
            platformConfig.setSendMessageWarning(sendMessageWarning);
            if (sendMessageWarning.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("金额不能大于99999999");
            }
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    @ApiOperation("查询人人代直属下级最大个数")
    @GetMapping("/findDirectly")
    public ResponseEntity<DirectlyVo> findDirectly(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        DirectlyVo directlyVo = new DirectlyVo();
        directlyVo.setName("人人代直属下级最大个数");
        directlyVo.setDirectlyUnderTheLower((platformConfig == null || platformConfig.getDirectlyUnderTheLower() ==null)? CommonConst.NUMBER_0:platformConfig.getDirectlyUnderTheLower());
        return ResponseUtil.success(directlyVo);
    }

    @ApiOperation("修改人人代直属下级最大个数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "directlyUnderTheLower", value = "人人代直属下级最大个数", required = true),
    })
    @PostMapping("/updateDirectly")
    public ResponseEntity updateDirectly(Integer directlyUnderTheLower){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setDirectlyUnderTheLower(directlyUnderTheLower);
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }


    @ApiOperation("查询推广链接码")
    @GetMapping("/findPromotionCode")
    public ResponseEntity findPromotionCode(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getCompanyInviteCode());
    }

    @ApiOperation("查询客服脚本的代号")
    @GetMapping("/findCustomerCode")
    public ResponseEntity findCustomerCode(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getCustomerCode());
    }

    @ApiOperation("修改客服脚本的代号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "customerCode", value = "客服脚本的代号", required = true),
    })
    @PostMapping("/updateCustomerCode")
    public ResponseEntity updateCustomerCode(String customerCode){
        if (LoginUtil.checkNull(customerCode)){
            return ResponseUtil.custom("参数必填");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setCustomerCode(customerCode);
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }
//    @ApiOperation("修改推广链接码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "promotionCode", value = "推广链接", required = true),
//    })
//    @PostMapping("/updatePromotionCode")
//    public ResponseEntity updatePromotionCode(String promotionCode){
//        if (LoginUtil.checkNull(promotionCode)){
//            return ResponseUtil.custom("参数必填");
//        }
//        String regex = "^[0-9a-zA-Z]{3,20}$";
//        if (!promotionCode.matches(regex)){
//            return ResponseUtil.custom("必须输入长度3-20位的数字或者字母");
//        }
//        PlatformConfig platformConfig = platformConfigService.findFirst();
//        if (LoginUtil.checkNull(platformConfig)){
//            platformConfig = new PlatformConfig();
//        }
//        platformConfig.setCompanyInviteCode(promotionCode);
//        platformConfigService.save(platformConfig);
//        return ResponseUtil.success();
//    }

    @ApiOperation("查询web项目域名配置")
    @GetMapping("/findWebConfiguration")
    public ResponseEntity findWebConfiguration(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getWebConfiguration());
    }

    @ApiOperation("修改web项目域名配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "webConfiguration", value = "web项目域名配置", required = true),
    })
    @PostMapping("/updateWebConfiguration")
    public ResponseEntity updateWebConfiguration(String webConfiguration){
        if (LoginUtil.checkNull(webConfiguration)){
            return ResponseUtil.custom("参数必填");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setWebConfiguration(webConfiguration);
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }


    /**
     * 图片服务器地址查询
     * @return
     */
    @ApiOperation("图片服务器地址查询")
    @GetMapping("/findUploadUrl")
    public ResponseEntity findUploadUrl(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getUploadUrl());
    }

    /**
     * 修改图片服务器地址
     * @param uploadUrl 图片服务器地址
     * @return
     */
    @ApiOperation("修改图片服务器地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploadUrl", value = "图片服务器地址", required = true),
    })
    @PostMapping("/updateUploadUrl")
    public ResponseEntity updateUploadUrl(String uploadUrl){
        if (LoginUtil.checkNull(uploadUrl)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        if(first.getUploadUrl()!=null && first.getUploadUrl().equals(uploadUrl)){
            return ResponseUtil.custom("当前地址相同,请勿重复修改");
        }
        first.setUploadUrl(uploadUrl);
        platformConfigService.save(first);
        //初始化银行卡图片
        bankInfoService.deleteBankInfoAll();
        initialization.saveBankInfo();
        //初始化客服中心图标
        customerConfigureService.deleteCustomerConfigureAll();
        initialization.saveCustomerConfigureInfo();
        return ResponseUtil.success();
    }




    /**
     * 读取图片服务器地址查询
     * @return
     */
    @ApiOperation("访问图片服务器地址查询")
    @GetMapping("/findReadUploadUrl")
    public ResponseEntity findReadUploadUrl(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getReadUploadUrl());
    }

    /**
     * 修改读取图片服务器地址
     * @param uploadUrl 图片服务器地址
     * @return
     */
    @ApiOperation("修改访问图片服务器地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploadUrl", value = "图片服务器地址", required = true),
    })
    @PostMapping("/updateReadUploadUrl")
    public ResponseEntity updateReadUploadUrl(String uploadUrl){
        if (LoginUtil.checkNull(uploadUrl)){
            return ResponseUtil.custom("参数错误");
        }

        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        if(first.getReadUploadUrl()!=null && first.getReadUploadUrl().equals(uploadUrl)){
            return ResponseUtil.custom("当前地址相同,请勿重复修改");
        }
        first.setReadUploadUrl(uploadUrl);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


    /**
     * 编辑logo图(PC)
     * @param file
     * @return
     */
    @ApiOperation("编辑logo图(PC)")
    @PostMapping(value = "/saveLogoPicturePc",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑logo图(PC)")
    public ResponseEntity saveLogoPicturePc(@RequestPart(value = "file", required = false) MultipartFile file){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        try {
            String uploadUrl = platformConfig.getUploadUrl();
            if(uploadUrl==null) {
                return ResponseUtil.custom("请先配置图片服务器上传地址");
            }
            String fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
            platformConfig.setLogImageUrlPc(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    /**
     * logo图查询(PC)
     * @return
     */
    @ApiOperation("logo图查询(PC)")
    @GetMapping("/findLogoPicturePc")
    public ResponseEntity findLogoPicturePc(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(platformConfig.getReadUploadUrl()==null) {
            return ResponseUtil.custom("请先配置图片服务器访问地址");
        }
        return ResponseUtil.success(platformConfig.getLogImageUrlPc()==null?null:platformConfig.getReadUploadUrl()+platformConfig.getLogImageUrlPc());
    }



    /**
     * 编辑logo图(APP)
     * @param file
     * @return
     */
    @ApiOperation("编辑logo图(APP)")
    @PostMapping(value = "/savePCPictureApp",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑logo图(APP)")
    public ResponseEntity savePCPictureApp(@RequestPart(value = "file", required = false) MultipartFile file){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        try {
            String uploadUrl = platformConfig.getUploadUrl();
            if(uploadUrl==null) {
                return ResponseUtil.custom("请先配置图片服务器上传地址");
            }
            String fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
            platformConfig.setLogImageUrlApp(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    /**
     * logo图查询(APP)
     * @return
     */
    @ApiOperation("logo图查询(APP)")
    @GetMapping("/findLogoPictureApp")
    public ResponseEntity findLogoPictureApp(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(platformConfig.getReadUploadUrl()==null) {
            return ResponseUtil.custom("请先配置图片服务器访问地址");
        }
        return ResponseUtil.success(platformConfig.getLogImageUrlApp()==null?null:platformConfig.getReadUploadUrl()+platformConfig.getLogImageUrlApp());
    }



    /**
     * 编辑logo图(APP登录注册页)
     * @param file
     * @return
     */
    @ApiOperation("编辑logo图(APP登录注册页)")
    @PostMapping(value = "/saveLoginRegisterLogoPictureApp",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑logo图(APP登录注册页)")
    public ResponseEntity saveLoginRegisterLogoPictureApp(@RequestPart(value = "file", required = false) MultipartFile file){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        try {
            String uploadUrl = platformConfig.getUploadUrl();
            if(uploadUrl==null) {
                return ResponseUtil.custom("请先配置图片服务器上传地址");
            }
            String fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
            platformConfig.setLoginRegisterLogImageUrlApp(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    /**
     * logo图查询(APP登录注册页)
     * @return
     */
    @ApiOperation("logo图查询(APP登录注册页)")
    @GetMapping("/findLoginRegisterLogoPictureApp")
    public ResponseEntity findLoginRegisterLogoPictureApp(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(platformConfig.getReadUploadUrl()==null) {
            return ResponseUtil.custom("请先配置图片服务器访问地址");
        }
        return ResponseUtil.success(platformConfig.getLoginRegisterLogImageUrlApp()==null?null:platformConfig.getReadUploadUrl()+platformConfig.getLoginRegisterLogImageUrlApp());
    }




    /**
     * 新增网站icon
     * @param file
     * @return
     */
    @ApiOperation("编辑网站icon")
    @PostMapping(value = "/saveWebsiteIcon",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑网站icon")
    public ResponseEntity saveWebsiteIcon(@RequestPart(value = "file", required = false) MultipartFile file){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        try {
            String uploadUrl = platformConfig.getUploadUrl();
            if(uploadUrl==null) {
                return ResponseUtil.custom("请先配置图片服务器上传地址");
            }
            String fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
            platformConfig.setWebsiteIcon(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }


    /**
     * 网站icon查询
     * @return
     */
    @ApiOperation("网站icon查看")
    @GetMapping("/findWebsiteIcon")
    public ResponseEntity findWebsiteIcon(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(platformConfig.getReadUploadUrl()==null) {
            return ResponseUtil.custom("请先配置图片服务器访问地址");
        }
        return ResponseUtil.success(platformConfig.getWebsiteIcon()==null?null:platformConfig.getReadUploadUrl()+platformConfig.getWebsiteIcon());
    }


    /**
     * 修改金钱符号
     * @return
     */
    @ApiOperation("编辑金钱符号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "moneySymbol", value = "金钱符号", required = true),
    })
    @PostMapping("/updateMoneySymbol")
    public ResponseEntity updateMoneySymbol(String moneySymbol){
        if (LoginUtil.checkNull(moneySymbol)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setMoneySymbol(moneySymbol);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


    /**
     * 金钱符号查询
     * @return
     */
    @ApiOperation("金钱符号查询")
    @GetMapping("/findMoneySymbol")
    public ResponseEntity findMoneySymbol(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?null:platformConfig.getMoneySymbol());
    }





    /**
     * 查询人人代开关 0:关闭，1:开启
     * @return
     */
    @ApiOperation("查询人人代开关")
    @GetMapping("/findPeopleProxySwitch")
    public ResponseEntity findPeopleProxySwitch(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null? Constants.close:platformConfig.getPeopleProxySwitch());
    }


    /**
     * 修改人人代开关
     * @return
     */
    @ApiOperation("编辑人人代开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "proxySwitch", value = "编辑人人代开关(0:关闭，1:开启)", required = true),
    })
    @PostMapping("/updatePeopleProxySwitch")
    public ResponseEntity updatePeopleProxySwitch(Integer proxySwitch){
        if (LoginUtil.checkNull(proxySwitch)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setPeopleProxySwitch(proxySwitch);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }

    /**
     * 查银行卡绑定同名只能绑定一个账号校验开关 0:关闭，1:开启
     * @return
     */
    @ApiOperation("查询银行卡账号校验开关")
    @GetMapping("/findBankcardRealNameSwitch")
    public ResponseEntity findBankcardRealNameSwitch(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null? Constants.close:platformConfig.getBankcardRealNameSwitch());
    }


    /**
     * 编辑银行卡账号校验开关
     * @return
     */
    @ApiOperation("编辑银行卡账号校验开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankcardRealNameSwitch", value = "银行卡绑定同名只能绑定一个账号校验开关", required = true),
    })
    @PostMapping("/updateBankcardRealNameSwitch")
    public ResponseEntity updateBankcardRealNameSwitch(Integer bankcardRealNameSwitch){
        if (LoginUtil.checkNull(bankcardRealNameSwitch)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setBankcardRealNameSwitch(bankcardRealNameSwitch);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }

    /**
     * 查询平台维护开关
     * @return
     */
    @ApiOperation("查询平台维护开关")
    @GetMapping("/findPlatformMaintenance")
    public ResponseEntity<MaintenanceVo> findPlatformMaintenance(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        MaintenanceVo maintenanceVo = new MaintenanceVo();
        if(LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
            platformConfig.setPlatformMaintenance(CommonConst.NUMBER_1);
        }
        if(!LoginUtil.checkNull(platformConfig.getMaintenanceEnd())){
            if (new Date().compareTo(platformConfig.getMaintenanceEnd()) > CommonConst.NUMBER_0 && platformConfig.getPlatformMaintenance() == CommonConst.NUMBER_1){
                platformConfig.setPlatformMaintenance(CommonConst.NUMBER_0);
                platformConfig.setMaintenanceStart(null);
                platformConfig.setMaintenanceEnd(null);
                platformConfigService.save(platformConfig);
            }
        }
        maintenanceVo.setPlatformMaintenance(platformConfig.getPlatformMaintenance());
        maintenanceVo.setMaintenanceEnd(platformConfig.getMaintenanceEnd());
        maintenanceVo.setMaintenanceStart(platformConfig.getMaintenanceStart());
        return ResponseUtil.success(maintenanceVo);
    }

    /**
     * 修改平台维护开关
     * @return
     */
    @ApiOperation("修改平台维护开关")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "platformMaintenance", value = "平台维护开关 0:维护 1:开启", required = true),
        @ApiImplicitParam(name = "maintenanceStart", value = "维护起始时间", required = false),
        @ApiImplicitParam(name = "maintenanceEnd", value = "维护结束时间", required = false),
    })
    @GetMapping("/updatePlatformMaintenance")
    public ResponseEntity updatePlatformMaintenance(Integer platformMaintenance,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date maintenanceStart,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date maintenanceEnd){
        if(LoginUtil.checkNull(platformMaintenance)){
            return ResponseUtil.custom("参数不合法");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        if (platformMaintenance == CommonConst.NUMBER_0){
            platformConfig.setPlatformMaintenance(platformMaintenance);
            platformConfig.setMaintenanceStart(null);
            platformConfig.setMaintenanceEnd(null);
        }else if (platformMaintenance == CommonConst.NUMBER_1){
            if (LoginUtil.checkNull(maintenanceStart,maintenanceEnd)){
                return ResponseUtil.custom("参数不合法");
            }
            if (maintenanceStart.compareTo(maintenanceEnd) > CommonConst.NUMBER_0){
                return ResponseUtil.custom("参数不合法");
            }
            platformConfig.setMaintenanceStart(maintenanceStart);
            platformConfig.setMaintenanceEnd(maintenanceEnd);
            platformConfig.setPlatformMaintenance(platformMaintenance);
        }else {
            return ResponseUtil.custom("参数不合法");
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();

    }

    @ApiOperation("后台查询平台总余额")
    @GetMapping("/queryTotalPlatformQuotaInfo")
    @NoAuthorization
    public  ResponseEntity queryTotalPlatformQuotaInfo(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null ?BigDecimal.ZERO:platformConfig.getTotalPlatformQuota().setScale(2, RoundingMode.HALF_UP));
    }


    @ApiOperation("查询验证码开关")
    @GetMapping("/findVerificationCode")
    public ResponseEntity findVerificationCode(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null? Constants.open:platformConfig.getVerificationCode());
    }

    @ApiOperation("修改验证码开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "verificationCode", value = "编辑验证码开关(0:关闭，1:开启)", required = true),
    })
    @PostMapping("/updateVerificationCode")
    public ResponseEntity updateVerificationCode(Integer verificationCode){
        if (LoginUtil.checkNull(verificationCode)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setVerificationCode(verificationCode);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


}
