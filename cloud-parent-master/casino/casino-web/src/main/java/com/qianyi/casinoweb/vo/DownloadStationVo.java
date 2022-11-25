package com.qianyi.casinoweb.vo;

import com.qianyi.casinocore.model.DownloadStation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("app版本控制")
@Data
public class DownloadStationVo {

    @ApiModelProperty(value = "最新版本")
    private DownloadStation newest;

    @ApiModelProperty(value = "强制升级的最新版本")
    private DownloadStation forcedNewest;
}
