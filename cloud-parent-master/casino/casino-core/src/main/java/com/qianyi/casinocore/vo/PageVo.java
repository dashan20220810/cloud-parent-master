package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PageVo {
    @ApiModelProperty(hidden=true)
    private int pageNo = 1; // 页码
    @ApiModelProperty(hidden=true)
    private int pageSize = 10; //每页显示的记录数，默认是10
    public PageVo(){

    }
    public PageVo(Integer pageNo, Integer pageSize){
        if (pageSize == null || pageNo == null) {
            pageNo=1;
            pageSize=10;
        }

        if (pageNo < 1 || pageSize < 1) {
            pageNo=1;
            pageSize=10;
        }

        if(pageSize > 100){
            pageSize = 100;
        }
    }
}
