package com.qianyi.casinoweb.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@ApiModel("公告/活动")
public class NoticeVo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
    @CreatedBy
    private String createBy;
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;
    @LastModifiedBy
    private String updateBy;
    @ApiModelProperty("内容")
    private String title;
    @ApiModelProperty("简介")
    private String introduction;
    @ApiModelProperty("详情访问页")
    private String url;
    @ApiModelProperty("是否上架")
    private Boolean isShelves = true;
}
