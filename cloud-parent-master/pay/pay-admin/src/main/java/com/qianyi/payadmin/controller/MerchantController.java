package com.qianyi.payadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.payadmin.util.PayUtil;
import com.qianyi.paycore.model.Merchant;
import com.qianyi.paycore.model.User;
import com.qianyi.paycore.service.MerchantService;
import com.qianyi.paycore.service.UserService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商户中心")
@RestController
@RequestMapping("merchant")
@Slf4j
public class MerchantController {

    @Autowired
    UserService userService;
    @Autowired
    MerchantService merchantService;

    @PostMapping("add")
    @ApiOperation("添加商户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "商户名称", required = true),
            @ApiImplicitParam(name = "password", value = "商户密码", required = true),
            @ApiImplicitParam(name = "telegram", value = "telegram 联系方式", required = false),
    })

   @ApiResponses({
           @ApiResponse(code = 0, message = "id:主键id,merchantNo:商户号")
   })
    public ResponseEntity add(String name, String password, String telegram) {

        //非空校验
        if (PayUtil.checkNull(name, password)) {
            return ResponseUtil.parameterNotNull();
        }

        //操作人校验
        Long userId = PayUtil.getAuthId();
        User user = userService.findById(userId);

        if(!User.checkUser(user)){
            return ResponseUtil.custom("此帐号不可操作");
        }

        //保存信息
        Merchant merchant = new Merchant();
        merchant.setOwnedId(userId);
        merchant.setName(name);

        String bcryptPassword=PayUtil.bcrypt(password);
        merchant.setPassword(bcryptPassword);

        if (!PayUtil.checkNull(telegram)) {
            merchant.setTelegram(telegram);
        }

        //商户号，惟一 凭证
        String no = getMerchantNo();
        merchant.setNo(no);

        Merchant merchant1=merchantService.save(merchant);

        JSONObject json = new JSONObject();
        json.put("id", merchant1.getId());
        json.put("merchantNo", merchant1.getNo());
        return ResponseUtil.success(json);
    }

    //生成商户号(yyyyMMddHHmmss+三位随机数)
    private String getMerchantNo() {
        String today = DateUtil.today("yyyyMMddHHmmss");

        String random = CommonUtil.random(3);
        return today + random;
    }
}
