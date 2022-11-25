package com.qianyi.casinoadmin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.DownloadStation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DownloadStationVo implements Serializable {

    private static final long serialVersionUID = -6875698563249678979L;

    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "版本名称")
    private String name;
    @ApiModelProperty(value = "下载地址")
    private String downloadUrl;
    @ApiModelProperty(value = "版本号")
    private String versionNumber;
    @ApiModelProperty(value = "终端类型,1：安卓，2：ios")
    private Integer terminalType;
    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "强制更新,1：是，2：否")
    private Integer isForced;
    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("最后修改人")
    private String updateBy;
    public DownloadStationVo(){

    }
    public DownloadStationVo(DownloadStation downloadStation){
      this.id = downloadStation.getId();
      this.name = downloadStation.getName();
      this.downloadUrl = downloadStation.getDownloadUrl();
      this.versionNumber = downloadStation.getVersionNumber();
      this.terminalType = downloadStation.getTerminalType();
      this.remark = downloadStation.getRemark();
      this.isForced = downloadStation.getIsForced();
      this.createBy = downloadStation.getCreateBy();
      this.createTime = downloadStation.getCreateTime();
      this.updateTime = downloadStation.getUpdateTime();
      this.updateBy = downloadStation.getUpdateBy();
    }
}
