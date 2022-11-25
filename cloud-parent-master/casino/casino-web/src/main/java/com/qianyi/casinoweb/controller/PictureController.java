package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.service.PictureService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("pic")
@Api(tags = "轮播图")
public class PictureController {

    @Autowired
    PictureService pictureService;

    @ApiOperation("轮播图.返回的URL为相对路径。需加上项目域名访问 ")
    @GetMapping("lunbo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "终端类型： 1.pc端（默认）  2.移动端", required = false),
    })
    @NoAuthentication
    public ResponseEntity<List<LunboPic>> lunbo(Integer type) {
        if (type == null) {
            type = 1;
        }
        List<LunboPic> list = pictureService.findByTheShowEnd(type);
        return ResponseUtil.success(list);
    }
}
