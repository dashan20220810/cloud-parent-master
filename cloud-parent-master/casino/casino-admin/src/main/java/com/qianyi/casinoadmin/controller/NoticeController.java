package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.NoticeService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notice")
@Api(tags = "公告中心")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SysUserService sysUserService;
    @ApiOperation("新增公告")
    @PostMapping("/saveNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "内容", required = true),
            @ApiImplicitParam(name = "enTitle", value = "英文内容", required = true),
            @ApiImplicitParam(name = "isShelves", value = "是否上架 true false", required = true),
            @ApiImplicitParam(name = "url", value = "详情访问页", required = false),
            @ApiImplicitParam(name = "introduction", value = "简介", required = true),
            @ApiImplicitParam(name = "enIntroduction", value = "英文简介", required = true),
    })
    public ResponseEntity<Notice> saveNotice(String title,String enTitle,Boolean isShelves,String introduction,String url,String enIntroduction){
        Notice notice = new Notice();
        if (ObjectUtils.isEmpty(title) || ObjectUtils.isEmpty(enTitle) || ObjectUtils.isEmpty(introduction) || ObjectUtils.isEmpty(enIntroduction)) {
            return ResponseUtil.custom("必填项不允许为空");
        }
        notice.setTitle(title);
        notice.setIntroduction(introduction);
        notice.setIsShelves(isShelves);
        notice.setUrl(url);
        notice.setEnTitle(enTitle);
        notice.setEnIntroduction(enIntroduction);
        return this.saveNotice(notice);
    }

    private synchronized ResponseEntity saveNotice(Notice notice){
        List<Notice> byNoticeList = noticeService.findByNoticeList();
        if (byNoticeList != null && byNoticeList.size() >= CommonConst.NUMBER_20){
            return ResponseUtil.custom(CommonConst.THENUMBERISLIMITEDTO20);
        }
        noticeService. saveNotice(notice);
        return ResponseUtil.success();
    }
    @ApiOperation("上架下架活动")
    @GetMapping("/deleteNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
    })
    public ResponseEntity deleteNotice(Long id){
        Notice notice = noticeService.findNoticeById(id);
        if(notice == null){
            return ResponseUtil.custom("不存在该活动");
        }
        boolean isShelves = notice.getIsShelves();
        if(isShelves == true){
            notice.setIsShelves(false);
        }else{
            notice.setIsShelves(true);
        }
        noticeService.saveNotice(notice);
        return ResponseUtil.success();
    }
    @ApiOperation("修改公告")
    @PostMapping("/updateNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
            @ApiImplicitParam(name = "title", value = "内容", required = true),
            @ApiImplicitParam(name = "enTitle", value = "英文内容", required = true),
            @ApiImplicitParam(name = "isShelves", value = "是否上架 true false", required = true),
            @ApiImplicitParam(name = "url", value = "详情访问页", required = false),
            @ApiImplicitParam(name = "introduction", value = "简介", required = true),
            @ApiImplicitParam(name = "enIntroduction", value = "英文简介", required = true),
    })
    public ResponseEntity updateNotice(String title,String enTitle,Boolean isShelves,String introduction,String url,Long id,String enIntroduction){
        if (ObjectUtils.isEmpty(title) || ObjectUtils.isEmpty(enTitle) || ObjectUtils.isEmpty(introduction) || ObjectUtils.isEmpty(enIntroduction)) {
            return ResponseUtil.custom("必填项不允许为空");
        }
        Notice notice = noticeService.findNoticeById(id);
        if (notice == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        notice.setUrl(url);
        notice.setIsShelves(isShelves);
        notice.setIntroduction(introduction);
        notice.setTitle(title);
        notice.setEnTitle(enTitle);
        notice.setEnIntroduction(enIntroduction);
        noticeService.saveNotice(notice);
        return ResponseUtil.success();
    }

    @ApiOperation("查询所有")
    @GetMapping("/findNotice")
    public ResponseEntity findNotice(){
        List<Notice> noticeList = noticeService.findByNoticeList();
        if(noticeList != null && noticeList.size() > 0){
            List<String> updateBys = noticeList.stream().map(Notice::getUpdateBy).collect(Collectors.toList());
            List<SysUser> sysUsers = sysUserService.findAll(updateBys);
            noticeList.stream().forEach(notice -> {
                sysUsers.stream().forEach(sysUser -> {
                    if (sysUser.getId().toString().equals(notice.getUpdateBy() == null ? "" : notice.getUpdateBy())) {
                        notice.setUpdateBy(sysUser.getUserName());
                    }
                });
            });
        }
        return new ResponseEntity(ResponseCode.SUCCESS, noticeList);
    }


    @ApiOperation("删除公告")
    @PostMapping(value = "/delNotice",name = "删除公告")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公告Id", required = true)})
    public ResponseEntity delNotice(String id){
        if (LoginUtil.checkNull(id)){
            ResponseUtil.custom("参数不合法");
        }
        Long notId = Long.parseLong(id);
        Notice noticeInfo = noticeService.findNoticeById(notId);
        if (LoginUtil.checkNull(noticeInfo)){
            return ResponseUtil.custom("没有这个公告");
        }
        noticeService.deleteById(notId);
        return ResponseUtil.success();
    }
}
