package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.service.PictureService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/picture")
@Api(tags = "公告中心")
public class PictureController {
    @Autowired
    private PictureService pictureService;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PlatformConfigService platformConfigService;

    private static List PCNo = new ArrayList();
    private static List AppNo = new ArrayList();
    static {
        PCNo.add(CommonConst.NUMBER_6);
        PCNo.add(CommonConst.NUMBER_7);
        PCNo.add(CommonConst.NUMBER_8);
        PCNo.add(CommonConst.NUMBER_9);
        PCNo.add(CommonConst.NUMBER_10);
        AppNo.add(CommonConst.NUMBER_1);
        AppNo.add(CommonConst.NUMBER_2);
        AppNo.add(CommonConst.NUMBER_3);
        AppNo.add(CommonConst.NUMBER_4);
        AppNo.add(CommonConst.NUMBER_5);
    }
    @ApiOperation("新增修改PC端轮播图")
    @PostMapping(value = "/savePCPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增PC端轮播图")
    public ResponseEntity savePCPicture(@RequestPart(value = "file", required = false) MultipartFile file,@RequestParam(value = "序号6-10") Integer no,
                                        @RequestParam(value = "备注", required = false) String remark){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(CommonConst.NUMBER_1);//PC端 1
        if (!PCNo.contains(no)){
            return ResponseUtil.custom("序号只能设置6-10");
        }
        return this.savePicture(file,no,lunboPic, remark);
    }


    @ApiOperation("新增修改移动端轮播图")
    @PostMapping(value = "/saveAppPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增移动端轮播图")
    public ResponseEntity saveAppPicture(@RequestPart(value = "file", required = false) MultipartFile file,@RequestParam(value = "序号1-5") Integer no,
            @RequestParam(value = "备注", required = false) String remark){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(CommonConst.NUMBER_2);//APP端 2
        if (!AppNo.contains(no)){
            return ResponseUtil.custom("序号只能设置1-5");
        }
        return this.savePicture(file,no,lunboPic, remark);
    }
    public ResponseEntity savePicture(MultipartFile file,Integer no,LunboPic lunboPic, String remark){
        LunboPic byNo = pictureService.findByNo(no);
        if (!LoginUtil.checkNull(byNo)){
            lunboPic.setId(byNo.getId());
        }
        lunboPic.setNo(no);
        lunboPic.setRemark(remark + "");
        try {
            if(file == null){
                lunboPic.setUrl(null);
            }else{
                PlatformConfig platformConfig= platformConfigService.findFirst();
                String uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl==null) {
                    return ResponseUtil.custom("请先配置图片服务器上传地址");
                }
                    String fileUrl = UploadAndDownloadUtil.fileUpload(file, uploadUrl);
                    lunboPic.setUrl(fileUrl);

            }
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        pictureService.save(lunboPic);
        return ResponseUtil.success();
    }


    @ApiOperation("查找Banner")
    @GetMapping("/findByBannerList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "theShowEnd", value = "1 web 2 app", required = false),
    })
    public ResponseEntity<LunboPic> findByBannerList(Integer theShowEnd){
        Specification<LunboPic> condition = this.getCondition(theShowEnd);
        Sort sort=Sort.by("id").ascending();
        List<LunboPic> byLunboPicList = pictureService.findByLunboPicList(condition, sort);
        if(byLunboPicList!=null){
            PlatformConfig platformConfig= platformConfigService.findFirst();
            String uploadUrl = platformConfig.getReadUploadUrl();
            if(uploadUrl==null) {
                return ResponseUtil.custom("请先配置图片服务器访问地址");
            }
            byLunboPicList.forEach(byLunboPicInfo ->{
                if (byLunboPicInfo.getUrl()!=null && uploadUrl!=null){
                    byLunboPicInfo.setUrl(uploadUrl+byLunboPicInfo.getUrl());
                }else {
                    byLunboPicInfo.setUrl(null);
                }
            });
        }
        return ResponseUtil.success(byLunboPicList);
    }

    private Specification<LunboPic> getCondition(Integer theShowEnd) {
        Specification<LunboPic> specification = new Specification<LunboPic>() {
            List<Predicate> list = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<LunboPic> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                if (theShowEnd != null) {
                    list.add(cb.equal(root.get("theShowEnd").as(Integer.class), theShowEnd));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
