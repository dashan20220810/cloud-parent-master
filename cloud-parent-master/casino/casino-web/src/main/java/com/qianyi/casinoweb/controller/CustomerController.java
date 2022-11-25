package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.CustomerConfigure;
import com.qianyi.casinocore.service.CustomerConfigureService;
import com.qianyi.casinoweb.vo.CustomerVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("customer")
@Api(tags = "客服中心")
public class CustomerController {

    @Autowired
    private CustomerConfigureService customerConfigureService;

    @GetMapping("contact")
    @ApiOperation("客服联系方式")
    @NoAuthentication
    public ResponseEntity<CustomerVo> contact() {
        List<CustomerConfigure> list = customerConfigureService.findByState(Constants.open);
        CustomerVo vo = new CustomerVo();
        Iterator<CustomerConfigure> integerIterator = list.iterator();
        while(integerIterator.hasNext()) {
            CustomerConfigure customerConfigure = integerIterator.next();
            if (customerConfigure.getCustomerMark() == 5) {
                vo.setTelephone(customerConfigure);
                integerIterator.remove();
            }
            if (customerConfigure.getCustomerMark() == 6) {
                vo.setOnlineUrl(customerConfigure);
                integerIterator.remove();
            }
            if (customerConfigure.getCustomerMark() == 7) {
                vo.setWebChat(customerConfigure);
                integerIterator.remove();
            }
        }
        vo.setCustomerList(list);
        return ResponseUtil.success(vo);
    }
}
