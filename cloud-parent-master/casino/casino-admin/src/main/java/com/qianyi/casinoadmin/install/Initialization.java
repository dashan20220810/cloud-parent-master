package com.qianyi.casinoadmin.install;

import cn.hutool.log.Log;
import com.qianyi.casinoadmin.install.file.*;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
@Order(1)
public class Initialization implements CommandLineRunner {
    @Autowired
    private PlatformConfigFile platformConfigFile;
    @Autowired
    private ProxyRebateConfigFile proxyRebateConfigFile;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private RebateConfigService rebateConfigService;
    @Autowired
    private SysPermissionConfigFile sysPermissionConfigFile;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private PictureInitialize pictureInitialize;
    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private CustomerConfigureService customerConfigureService;

    @Autowired
    private PictureService pictureService;
    @Autowired
    private InitializationSuperRole initializationSuperRole;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Value("${project.username:admin}")
    private String userNames;

    @Value("${project.password:123456}")
    private String password;


    @Autowired
    NewPermissions newPermissions;

    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;

    @Autowired
    private GameRecordReportNewService gameRecordReportService;

    @Autowired
    private ChargeOrderService chargeOrderService;
    @Autowired
    private WithdrawOrderService withdrawOrderService;
    @Autowired
    private AccountChangeService accountChangeService;
    @Autowired
    private RebateConfigurationService rebateConfigurationService;
    @Autowired
    private ExtractPointsConfigService extractPointsConfigService;

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化数据开始============================================》");
        //       this.saveBanner();
        this.runPlatformConfig();
        this.saveBankInfo();
        this.runProxyRebateConfig();
        // 初始化权限相关。
        this.initPermission();
        // this.runSysPermissionConfig();
        // this.addPermissionConfig();
        this.runAddSysUser();
        //客户中心配置初始化
        this.saveCustomerConfigureInfo();
        this.saveGameRecordEndIndex();

        this. saveReturnCommissionInfo();
        this.initializationTotalPlatformQuota();

        this.initProxyWashCodeConfig();

        this.saveExtractPointsConfig();
    }
    //新增代理抽点配置
    private void saveExtractPointsConfig(){
        try {
            log.info("初始化OBDJ代理抽点配置");
            List<ExtractPointsConfig> obdj = extractPointsConfigService.findByPlatform(Constants.PLATFORM_OBDJ);
            log.info("OBDJ代理抽点配置{}",obdj);
            if (obdj == null || obdj.size() == 0){
                log.info("OBDJ代理抽点配置进来了");
                ExtractPointsConfig extractPointsConfig = new ExtractPointsConfig();
                extractPointsConfig.setGameId(Constants.PLATFORM_OBDJ);
                extractPointsConfig.setGameName("OB电竞");
                extractPointsConfig.setPlatform(Constants.PLATFORM_OBDJ);
                extractPointsConfig.setGameEnName(Constants.PLATFORM_OBDJ);
                extractPointsConfig.setRate(BigDecimal.ZERO);
                extractPointsConfig.setState(1);
                extractPointsConfig.setCreateBy("0");
                extractPointsConfig.setUpdateBy("0");
                extractPointsConfigService.save(extractPointsConfig);
                log.info("OBDJ代理抽点配置保存成功{}",extractPointsConfig);
            }

            log.info("初始化OBTY代理抽点配置");
            List<ExtractPointsConfig> obty = extractPointsConfigService.findByPlatform(Constants.PLATFORM_OBTY);
            log.info("OBTY代理抽点配置{}",obty);
            if (obty == null || obty.size() == 0){
                ExtractPointsConfig extractPointsConfig = new ExtractPointsConfig();
                extractPointsConfig.setGameId(Constants.PLATFORM_OBTY);
                extractPointsConfig.setGameName("OB体育");
                extractPointsConfig.setPlatform(Constants.PLATFORM_OBTY);
                extractPointsConfig.setGameEnName(Constants.PLATFORM_OBTY);
                extractPointsConfig.setRate(BigDecimal.ZERO);
                extractPointsConfig.setState(1);
                extractPointsConfig.setCreateBy("0");
                extractPointsConfig.setUpdateBy("0");
                extractPointsConfigService.save(extractPointsConfig);
                log.info("OBTY代理抽点配置保存成功{}",extractPointsConfig);
            }

            log.info("初始化SABASPORT代理抽点配置");
            List<ExtractPointsConfig> sbty = extractPointsConfigService.findByPlatform(Constants.PLATFORM_SABASPORT);
            log.info("SABASPORT代理抽点配置{}",sbty);
            if (sbty == null || sbty.size() == 0){
                ExtractPointsConfig extractPointsConfig = new ExtractPointsConfig();
                extractPointsConfig.setGameId(Constants.PLATFORM_SABASPORT);
                extractPointsConfig.setGameName("沙巴体育");
                extractPointsConfig.setPlatform(Constants.PLATFORM_SABASPORT);
                extractPointsConfig.setGameEnName(Constants.PLATFORM_SABASPORT);
                extractPointsConfig.setRate(BigDecimal.ZERO);
                extractPointsConfig.setState(1);
                extractPointsConfig.setCreateBy("0");
                extractPointsConfig.setUpdateBy("0");
                extractPointsConfigService.save(extractPointsConfig);
                log.info("SABASPORT代理抽点配置保存成功{}",extractPointsConfig);
            }
        }catch (Exception ex){
            log.error("初始化代理抽点配置失败{}",ex);
        }
    }

    public void initProxyWashCodeConfig(){
        try {
            log.info("初始化全局返利比例配置");
            RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(0L,Constants.OVERALL_TYPE);
            if (LoginUtil.checkNull(byThirdProxy)){
                RebateConfiguration proxyWashCodeConfig = new RebateConfiguration();
                proxyWashCodeConfig.setCQ9Rate(new BigDecimal(0.9));
                proxyWashCodeConfig.setWMRate(new BigDecimal(0.9));
                proxyWashCodeConfig.setPGRate(new BigDecimal(0.9));
                proxyWashCodeConfig.setOBDJRate(new BigDecimal(0.9));
                proxyWashCodeConfig.setOBTYRate(new BigDecimal(0.9));
                proxyWashCodeConfig.setSABASPORTRate(new BigDecimal(0.9));
                proxyWashCodeConfig.setUserId(0L);
                proxyWashCodeConfig.setType(Constants.OVERALL_TYPE);
                rebateConfigurationService.save(proxyWashCodeConfig);
            }
        }catch (Exception ex){
            log.error("初始化全局返利比例配置失败{}",ex);
        }


    }

    public void initPermission(){
        newPermissions.init();
    }

    public  void initializationTotalPlatformQuota(){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        if (platformConfig!=null && (platformConfig.getHistoricalDataId()==null || platformConfig.getHistoricalDataId()!=1)){
            BigDecimal amount=new BigDecimal(5000000);
            BigDecimal charge = chargeOrderService.sumChargeAmount();
            List<AccountChange> codeWashingList = accountChangeService.findByType(0);
            BigDecimal codeWashing = codeWashingList.stream().map(AccountChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            List<AccountChange> renrenLoanList = accountChangeService.findByType(9);
            BigDecimal renrenLoan = renrenLoanList.stream().map(AccountChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal add = charge.add(codeWashing).add(renrenLoan);
            amount= amount.subtract(add);
            //下分
            BigDecimal withdraw = withdrawOrderService.sumWithdrawMoney();
            //提现
            BigDecimal practicalAmount = withdrawOrderService.sumPracticalAmount();
            amount= amount.add(withdraw).add(practicalAmount);

            platformConfig.setTotalPlatformQuota(amount);
            platformConfig.setHistoricalDataId(1);
            platformConfigService.save(platformConfig);

        }
    }
    private void saveGameRecordEndIndex() {
        GameRecordEndIndex first = gameRecordEndIndexService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new GameRecordEndIndex();
            first.setGameRecordId(0L);
            first.setPGMaxId(0L);
            first.setCQ9MaxId(0L);
            first.setOBDJMaxId(0L);
            first.setOBTYMaxId(0L);
            first.setSABASPORTMaxId(0L);
            gameRecordEndIndexService.save(first);
        }else {
            first.setGameRecordId(first.getGameRecordId()==null?0L:first.getGameRecordId());
            first.setPGMaxId(first.getPGMaxId()==null?0L:first.getPGMaxId());
            first.setCQ9MaxId(first.getCQ9MaxId()==null?0L:first.getCQ9MaxId());
            first.setOBDJMaxId(first.getOBDJMaxId()==null?0L:first.getOBDJMaxId());
            first.setOBTYMaxId(first.getOBTYMaxId()==null?0L:first.getOBTYMaxId());
            first.setSABASPORTMaxId(first.getSABASPORTMaxId()==null?0L:first.getSABASPORTMaxId());
            gameRecordEndIndexService.save(first);
        }
    }

    /**
     * 添加新的权限脚本
     */
    private void addPermissionConfig() {
        sysPermissionConfigFile.addPermissionConfig();
        newPermissions.addNewPermission();//新增加权限在此方法里面写 (废弃)
    }

    private void saveBanner(){
        List<LunboPic> PCLunboPics = pictureService.findByTheShowEnd(CommonConst.NUMBER_1);
        if (LoginUtil.checkNull(PCLunboPics) || PCLunboPics.size() == CommonConst.NUMBER_0){
            Map<String, Integer> PCMap = pictureInitialize.PCbanner;
            PCMap.forEach((key, value) -> {
                MultipartFile file = pictureInitialize.single(key);
                this.saveBanner(file,value,CommonConst.NUMBER_1);
            });
        }
        List<LunboPic> APPLunboPics = pictureService.findByTheShowEnd(CommonConst.NUMBER_2);
        if (LoginUtil.checkNull(APPLunboPics) || APPLunboPics.size() == CommonConst.NUMBER_0){
            Map<String, Integer> APPMap = pictureInitialize.APPbanner;
            APPMap.forEach((key, value) -> {
                MultipartFile file = pictureInitialize.single(key);
                this.saveBanner(file,value,CommonConst.NUMBER_2);
            });
        }
    }

    private void saveBanner(MultipartFile file,Integer no,Integer theShowEnd){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(theShowEnd);
        lunboPic.setNo(no);
        try {
            if(file == null){
                lunboPic.setUrl(null);
            }else{
                PlatformConfig platformConfig= platformConfigService.findFirst();
                String uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl!=null) {
                    String fileUrl = UploadAndDownloadUtil.fileUpload(file, uploadUrl);
                    lunboPic.setUrl(fileUrl);
                }
            }
        } catch (Exception e) {
            log.error("初始化轮播图出错");
        }
        pictureService.save(lunboPic);
    }
    public void saveBankInfo(){
        List<BankInfo> all = bankInfoService.findAll();
        if (!LoginUtil.checkNull(all) && all.size() > CommonConst.NUMBER_0)
            return;
        Map<String, String> bank = pictureInitialize.bank;
        bank.forEach((key, value) -> {
            MultipartFile file = pictureInitialize.single(key);
            this.saveBankInfo(file,value);
        });
    }
    private void saveBankInfo(MultipartFile file,String bankName){
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName(bankName);
        bankInfo.setBankType(CommonConst.NUMBER_0);//默认银行卡
        bankInfo.setDisable(CommonConst.NUMBER_0);//默启用用
        try {
            String fileUrl = null;
            if (file != null){
                PlatformConfig platformConfig= platformConfigService.findFirst();
                String uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl!=null){
                    fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
                }
            }
            bankInfo.setBankLogo(fileUrl);
        } catch (Exception e) {
            bankInfo.setBankLogo("");
        }
        try {
            bankInfoService.saveBankInfo(bankInfo);
        }catch (Exception e){
            log.error("重复银行名称");
        }
    }

    //客服中心配置
    public void saveCustomerConfigureInfo(){
        List<CustomerConfigure> all = customerConfigureService.findAll();
        if (!LoginUtil.checkNull(all) && all.size() > CommonConst.NUMBER_0)
            return;
        //先判断是否有配置图片服务器
        PlatformConfig platformConfig= platformConfigService.findFirst();
        String uploadUrl = platformConfig.getUploadUrl();
        if(uploadUrl!=null){
            List<CustomerConfigure> customer = pictureInitialize.customer;
            customer.forEach(customerInfo ->{
                saveCustomerInfo(customerInfo,uploadUrl);
            });
        }
    }

    //返佣配置
    public void saveReturnCommissionInfo(){
        try {
            log.info("初始全局代理返佣等级配置");
            RebateConfig gameTypeTemp = rebateConfigService.findGameType(1);
            if (gameTypeTemp==null){
                RebateConfig all = rebateConfigService.findFirst();
                if (all!=null && all.getGameType()==null){
                    all.setGameType(1);
                    rebateConfigService.save(all);
                }
            }
            RebateConfig gameType = rebateConfigService.findGameType(2);
            if(gameType==null){
                addRebateConfig(2);
            }
            RebateConfig gameType2 = rebateConfigService.findGameType(3);
            if(gameType2==null){
                addRebateConfig(3);
            }
            RebateConfig obdj = rebateConfigService.findGameType(4);
            log.info("全局代理返佣等级配置obdj{}",obdj);
            if(obdj==null){
                addRebateConfig(4);
            }
            RebateConfig obty = rebateConfigService.findGameType(5);
            log.info("全局代理返佣等级配置obty{}",obty);
            if(obty==null){
                addRebateConfig(5);
            }
            RebateConfig sbty = rebateConfigService.findGameType(6);
            log.info("全局代理返佣等级配置sbty{}",sbty);
            if(sbty==null){
                addRebateConfig(6);
            }
        }catch (Exception ex){
            log.error("初始全局代理返佣等级配置失败{}",ex);
        }

    }

    public void addRebateConfig(Integer gameType){
        RebateConfig rebateConfig=new RebateConfig();
        rebateConfig.setGameType(gameType);

        rebateConfig.setFirstMoney(0);
        rebateConfig.setFirstAmountLine(new BigDecimal(10));
        rebateConfig.setFirstProfit(new BigDecimal(10));

        rebateConfig.setSecondMoney(5);
        rebateConfig.setSecondAmountLine(new BigDecimal(5));
        rebateConfig.setSecondProfit(new BigDecimal(12));

        rebateConfig.setThirdMoney(20);
        rebateConfig.setThirdAmountLine(new BigDecimal(20));
        rebateConfig.setThirdProfit(new BigDecimal(14));

        rebateConfig.setFourMoney(50);
        rebateConfig.setFourAmountLine(new BigDecimal(50));
        rebateConfig.setFourProfit(new BigDecimal(16));

        rebateConfig.setFiveMoney(100);
        rebateConfig.setFiveAmountLine(new BigDecimal(100));
        rebateConfig.setFiveProfit(new BigDecimal(18));

        rebateConfig.setSixMoney(200);
        rebateConfig.setSixAmountLine(new BigDecimal(200));
        rebateConfig.setSixProfit(new BigDecimal(23));

        rebateConfig.setSevenMoney(400);
        rebateConfig.setSevenAmountLine(new BigDecimal(400));
        rebateConfig.setSevenProfit(new BigDecimal(25));
        rebateConfig.setEightMoney(800);
        rebateConfig.setEightAmountLine(new BigDecimal(800));
        rebateConfig.setEightProfit(new BigDecimal(30));
        rebateConfigService.save(rebateConfig);
    }

    private void saveCustomerInfo(CustomerConfigure customerInfo,String uploadUrl){
        CustomerConfigure customerConfigureInfo = new CustomerConfigure();
        customerConfigureInfo.setState(customerInfo.getState());
        customerConfigureInfo.setCustomer(customerInfo.getCustomer());
        customerConfigureInfo.setCustomerMark(customerInfo.getCustomerMark());
        customerConfigureInfo.setCustomerAccount(customerInfo.getCustomerAccount());
        if (!CommonUtil.checkNull(customerInfo.getAppIconUrl())){
            MultipartFile appIconUrl = pictureInitialize.single(customerInfo.getAppIconUrl());
            try {
                String appFileUrl = UploadAndDownloadUtil.fileUpload(appIconUrl,uploadUrl);
                customerConfigureInfo.setAppIconUrl(appFileUrl);
            } catch (Exception e) {
                customerConfigureInfo.setAppIconUrl("");
                customerConfigureInfo.setPcIconUrl("");
            }
        }
        if (!CommonUtil.checkNull(customerInfo.getPcIconUrl())){
            MultipartFile pcIconUrl = pictureInitialize.single(customerInfo.getPcIconUrl());
            try {
                String pcFileUrl = UploadAndDownloadUtil.fileUpload(pcIconUrl,uploadUrl);
                customerConfigureInfo.setPcIconUrl(pcFileUrl);
            } catch (Exception e) {
                customerConfigureInfo.setAppIconUrl("");
                customerConfigureInfo.setPcIconUrl("");
            }
        }
        try {
            customerConfigureService.save(customerConfigureInfo);
        }catch (Exception e){
            log.error("初始化客服图标异常");
        }
    }


    //系统超级管理员角色
    private void runAddSysUser() {
        //创建出系统超级管理员角色
        initializationSuperRole.saveSuperRole();
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("系统超级管理员");
        List<SysRole> sysRoleList = sysRoleService.findbyRoleName(sysRole);
        if(sysRoleList.isEmpty() || sysRoleList.size() == 0){
            return;
        }
        SysRole role = sysRoleList.get(0);
        String[] split = userNames.split(",");
        for (String name : split) {
            SysUser sys = sysUserService.findByUserName(name);
            if(sys != null){
                SysUserRole sysUserRole = sysUserRoleService.findbySysUserId(sys.getId());
                if(sysUserRole == null){
                    sysUserRole = new SysUserRole();
                }
                sysUserRole.setSysUserId(sys.getId());
                sysUserRole.setSysRoleId(role.getId());
                sysUserRoleService.save(sysUserRole);
                continue;
            }
            //加密
            String bcryptPassword = LoginUtil.bcrypt(password);
            SysUser sysUser = new SysUser();
            sysUser.setUserName(name);
            sysUser.setNickName(name);
            sysUser.setPassWord(bcryptPassword);
            sysUser.setUserFlag(Constants.open);
            sysUserService.save(sysUser);
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setSysUserId(sysUser.getId());
            sysUserRole.setSysRoleId(role.getId());
            sysUserRoleService.save(sysUserRole);
        }
    }

    private void runSysPermissionConfig() {
        sysPermissionConfigFile.getPermissionConfig();
    }

    private void runProxyRebateConfig(){
        List<RebateConfig> all = rebateConfigService.findAll();
        if (LoginUtil.checkNull(all) || all.size() == CommonConst.NUMBER_0){
            RebateConfig first = new RebateConfig();
            first.setFirstMoney(proxyRebateConfigFile.getFirstMoney());
            first.setFirstAmountLine(proxyRebateConfigFile.getFirstMoneyLine());
            first.setFirstProfit(proxyRebateConfigFile.getFirstProfit());
            first.setSecondMoney(proxyRebateConfigFile.getSecondMoney());
            first.setSecondAmountLine(proxyRebateConfigFile.getSecondMoneyLine());
            first.setSecondProfit(proxyRebateConfigFile.getSecondProfit());
            first.setThirdMoney(proxyRebateConfigFile.getThirdMoney());
            first.setThirdAmountLine(proxyRebateConfigFile.getThirdMoneyLine());
            first.setThirdProfit(proxyRebateConfigFile.getThirdProfit());
            first.setFourMoney(proxyRebateConfigFile.getFourMoney());
            first.setFourAmountLine(proxyRebateConfigFile.getFourMoneyLine());
            first.setFourProfit(proxyRebateConfigFile.getFourProfit());
            first.setFiveMoney(proxyRebateConfigFile.getFiveMoney());
            first.setFiveAmountLine(proxyRebateConfigFile.getFiveMoneyLine());
            first.setFiveProfit(proxyRebateConfigFile.getFiveProfit());
            first.setSixMoney(proxyRebateConfigFile.getSixMoney());
            first.setSixAmountLine(proxyRebateConfigFile.getSixMoneyLine());
            first.setSixProfit(proxyRebateConfigFile.getSixProfit());
            first.setSevenMoney(proxyRebateConfigFile.getSevenMoney());
            first.setSevenAmountLine(proxyRebateConfigFile.getSevenMoneyLine());
            first.setSevenProfit(proxyRebateConfigFile.getSevenProfit());
            first.setEightMoney(proxyRebateConfigFile.getEightMoney());
            first.setEightAmountLine(proxyRebateConfigFile.getEightMoneyLine());
            first.setEightProfit(proxyRebateConfigFile.getEightProfit());
            rebateConfigService.save(first);
        }
    }
    private void runPlatformConfig(){
        List<PlatformConfig> all = platformConfigService.findAll();
        if (LoginUtil.checkNull(all) || all.size()== CommonConst.NUMBER_0){
            PlatformConfig platformConfig = new PlatformConfig();
            platformConfig.setClearCodeNum(platformConfigFile.getClearCodeNum());
            platformConfig.setBetRate(platformConfigFile.getBetRate());
            platformConfig.setChargeMinMoney(platformConfigFile.getChargeMinMoney());
            platformConfig.setChargeMaxMoney(platformConfigFile.getChargeMaxMoney());
            platformConfig.setChargeServiceMoney(platformConfigFile.getChargeServiceMoney());
            platformConfig.setChargeRate(platformConfigFile.getChargeRate());
            platformConfig.setWithdrawMinMoney(platformConfigFile.getWithdrawMinMoney());
            platformConfig.setWithdrawMaxMoney(platformConfigFile.getWithdrawMaxMoney());
            platformConfig.setWithdrawServiceMoney(platformConfigFile.getWithdrawServiceMoney());
            platformConfig.setWithdrawRate(platformConfigFile.getWithdrawRate());
            platformConfig.setIpMaxNum(platformConfigFile.getIpMaxNum());
            platformConfig.setWmMoney(platformConfigFile.getWmMoney());
            platformConfig.setWmMoneyWarning(platformConfigFile.getWmMoneyWarning());
            platformConfig.setFirstCommission(platformConfigFile.getFirstCommission());
            platformConfig.setSecondCommission(platformConfigFile.getSecondCommission());
            platformConfig.setThirdCommission(platformConfigFile.getThirdCommission());
            platformConfig.setDomainNameConfiguration(platformConfigFile.getDomainNameConfiguration());
            platformConfig.setRegisterSwitch(platformConfigFile.getRegisterSwitch());
            platformConfig.setProxyConfiguration(platformConfigFile.getProxyConfiguration());
            platformConfig.setSendMessageWarning(platformConfigFile.getSendMessageWarning());
            platformConfig.setDirectlyUnderTheLower(platformConfigFile.getDirectlyUnderTheLower());
            platformConfig.setCompanyInviteCode(platformConfigFile.getCompanyInviteCode());
            platformConfig.setCustomerCode(platformConfigFile.getCustomerCode());
            platformConfig.setUploadUrl(platformConfigFile.getUploadUrl());
            platformConfig.setReadUploadUrl(platformConfigFile.getReadUploadUrl());
            platformConfig.setMoneySymbol(platformConfigFile.getMoneySymbol());
            platformConfig.setPeopleProxySwitch(platformConfigFile.getPeopleProxySwitch());
            platformConfig.setBankcardRealNameSwitch(platformConfigFile.getBankcardRealNameSwitch());
            platformConfig.setTotalPlatformQuota(platformConfigFile.getTotalPlatformQuota());
            platformConfig.setVerificationCode(platformConfigFile.getVerificationCode());
            platformConfigService.save(platformConfig);
        }else {
            PlatformConfig platformConfig = all.get(CommonConst.NUMBER_0);
            String companyInviteCode = platformConfig.getCompanyInviteCode();
            if (companyInviteCode == null || !companyInviteCode.equals("999")){
                platformConfig.setCompanyInviteCode("999");
            }
            if (LoginUtil.checkNull(platformConfig.getCustomerCode())){
                platformConfig.setCustomerCode("21141305");
            }
            if (LoginUtil.checkNull(platformConfig.getBankcardRealNameSwitch())){
                platformConfig.setBankcardRealNameSwitch(platformConfigFile.getBankcardRealNameSwitch());
            }
            if (LoginUtil.checkNull(platformConfig.getPeopleProxySwitch())){
                platformConfig.setPeopleProxySwitch(platformConfigFile.getPeopleProxySwitch());
            }
            if(LoginUtil.checkNull(platformConfig.getPlatformMaintenance())){
                platformConfig.setPlatformMaintenance(CommonConst.NUMBER_0);
            }
            if(LoginUtil.checkNull(platformConfig.getVerificationCode())){
                platformConfig.setVerificationCode(CommonConst.NUMBER_1);
            }
            platformConfigService.save(platformConfig);
        }
    }
}
