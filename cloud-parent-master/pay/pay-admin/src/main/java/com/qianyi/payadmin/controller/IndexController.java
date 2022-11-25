package com.qianyi.payadmin.controller;

import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class IndexController implements ErrorController {

    @Value("${server.port}")
    private String port;

    @GetMapping("port")
    public ResponseEntity getPort() {

        String data="this port:" + port;
        return ResponseUtil.success(data);
    }

}
