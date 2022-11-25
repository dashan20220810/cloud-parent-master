package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.model.CustomerConfigure;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.CustomerConfigureService;
import com.qianyi.casinocore.service.CustomerService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/customer")
@Api(tags = "客服中心")
public class CustomerController {
    @Autowired
    private CustomerService customerService;


    @Autowired
    private CustomerConfigureService customerConfigureService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private MessageUtil messageUtil;
    /**
     * 新增和修改客服联系方式
     * @param qq 客服qq号
     * @param telegram 客服telegram号
     * @param skype 客服skype号
     * @param whatsApp 客服whatsApp号
     * @param facebook 客服facebook号
     * @param onlineUrl 客服onlineUrl号
     * @return
     */
    @ApiOperation("新增和修改客服联系方式")
    @PostMapping("/updateKeyCustomer")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "qq", value = "客服qq号", required = false),
            @ApiImplicitParam(name = "telegram", value = "客服telegram号", required = false),
            @ApiImplicitParam(name = "skype", value = "客服skype号", required = false),
            @ApiImplicitParam(name = "whatsApp", value = "客服whatsApp号", required = false),
            @ApiImplicitParam(name = "facebook", value = "客服facebook号", required = false),
            @ApiImplicitParam(name = "onlineUrl", value = "客服onlineUrl号", required = false),
            @ApiImplicitParam(name = "wechat", value = "客服wechat号", required = false),
            @ApiImplicitParam(name = "meiqia", value = "客服meiqia号", required = false),
            @ApiImplicitParam(name = "telephone", value = "手机号", required = false),
    })
    public ResponseEntity updateKeyCustomer(String qq,String telegram,String skype,String whatsApp,String facebook,String onlineUrl,
                                            String wechat,String meiqia,String telephone){
        Customer customer = customerService.findFirst();
        if (LoginUtil.checkNull(customer)){
            customer = new Customer();
        }
        if (!CommonUtil.checkNull(qq)){
            if (!qq.matches(RegexEnum.NUMBER_OR_LETTER.getRegex())) {
                return ResponseUtil.custom("qq格式错误");
            }
            customer.setQq(qq);
        }
        if (!CommonUtil.checkNull(telegram)){
            customer.setTelegram(telegram);
        }
        if (!CommonUtil.checkNull(skype)){
            customer.setSkype(skype);
        }
        if (!CommonUtil.checkNull(whatsApp)){
            customer.setWhatsApp(whatsApp);
        }
        if (!CommonUtil.checkNull(facebook)){
            customer.setFacebook(facebook);
        }
        if (!CommonUtil.checkNull(onlineUrl)){
            customer.setOnlineUrl(onlineUrl);
        }
        if (!CommonUtil.checkNull(wechat)){
            if (!wechat.matches(RegexEnum.WEBCHAT.getRegex())) {
                return ResponseUtil.custom("微信号格式错误");
            }
            customer.setWechat(wechat);
        }
        if (!CommonUtil.checkNull(meiqia)){
            customer.setMeiqia(meiqia);
        }
        if (!CommonUtil.checkNull(telephone)){
            if (!telephone.matches(RegexEnum.PHONE.getRegex())) {
                return ResponseUtil.custom("手机号格式错误");
            }
            customer.setTelephone(telephone);
        }
        customerService.save(customer);
        return ResponseUtil.success();
    }
    @ApiOperation("查询客服联系方式")
    @GetMapping("/findCustomer")
    public ResponseEntity<Customer> findCustomer(){
        Customer customer = customerService.findFirst();
        return ResponseUtil.success(customer);
    }


    /**
     * 查询客服中心配置列表
     * @return
     */
    @ApiOperation("查询客服中心配置列表")
    @GetMapping("/findCustomerList")
    public ResponseEntity<CustomerConfigure> findCustomerList() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        String readUploadUrl = platformConfig.getReadUploadUrl();
        List<CustomerConfigure> customer = customerConfigureService.findAll();
        if(readUploadUrl==null) {
            return ResponseUtil.custom("请先配置图片服务器访问地址");
        }
        customer.forEach(info -> {
            info.setAppIconUrl(readUploadUrl + info.getAppIconUrl());
            info.setPcIconUrl(readUploadUrl + info.getPcIconUrl());
            info.setCustomer(messageUtil.get(info.getCustomer()));
        });
        return ResponseUtil.success(customer);
    }


    /**
     * 编辑客服中心配置
     * @param id
     * @param customerAccount 客服账号
     * @param state 状态(1:启用,0:停用)
     * @param appIconFile app图标
     * @param pcIconFile pc图标
     * @return
     */
    @ApiOperation("编辑客服中心配置")
    @PostMapping(value = "/updateKeyCustomerConfigure",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑客服中心配置")
    public ResponseEntity updateKeyCustomerConfigure(  @RequestParam(value = "id", required = true)Long id,
                                                       @RequestParam(value = "客服账号", required = false)String customerAccount,
                                                       @RequestParam(value = "状态(1:启用,0:停用)", required = false)Integer state,
                                                       @RequestParam(value = "修改状态(1:状态修改,0:保存)", required = false)Integer type,
                                            @RequestPart(value = "app图标", required = false) MultipartFile appIconFile,
                                            @RequestPart(value = "pc图标", required = false) MultipartFile pcIconFile){
        CustomerConfigure customerConfigure = customerConfigureService.getById(id);
        if (type==Constants.yes){
            if (state!=null) {
                if (state==Constants.open){
                    //判断是否有3个开启状态
                    int quantity = customerConfigureService.countCustomerConfigure(Constants.open);
                    if (quantity==3){
                        return ResponseUtil.custom("客服平台最多启用3个(手机号,onlineUrl号不包含在内)");
                    }
                    if(LoginUtil.checkNull(customerConfigure.getCustomerAccount())){
                        return ResponseUtil.custom("请先保存配置信息");
                    }
                }
                customerConfigure.setState(state);
            }
        }else {
            String uploadUrl=null;
            if (pcIconFile!=null||appIconFile!=null) {
                PlatformConfig platformConfig= platformConfigService.findFirst();
                uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl==null) {
                    return ResponseUtil.custom("请先配置图片服务器上传地址");
                }
            }
            if (appIconFile!=null){
                savePicture(appIconFile,customerConfigure, Constants.yes,uploadUrl);
            }
            if (pcIconFile!=null) {
                savePicture(pcIconFile, customerConfigure, Constants.no,uploadUrl);
            }
            if (LoginUtil.checkNull(customerAccount)){
                //判断状态是否为启用
                if(customerConfigure.getState()==Constants.open){
                    customerConfigure.setState(Constants.close);
                }
                customerConfigure.setCustomerAccount(customerAccount);
            }else {
                customerConfigure.setCustomerAccount(customerAccount);
            }
        }
        customerConfigureService.save(customerConfigure);
        return ResponseUtil.success();
    }


    public ResponseEntity savePicture(MultipartFile file,CustomerConfigure customerConfigure,Integer mark, String uploadUrl ){
        try {
            String fileUrl = UploadAndDownloadUtil.fileUpload(file, uploadUrl);
            if(mark== Constants.yes){
                customerConfigure.setAppIconUrl(fileUrl);
            }
            if(mark== Constants.no){
                customerConfigure.setPcIconUrl(fileUrl);
            }
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        return ResponseUtil.success();
    }
}
