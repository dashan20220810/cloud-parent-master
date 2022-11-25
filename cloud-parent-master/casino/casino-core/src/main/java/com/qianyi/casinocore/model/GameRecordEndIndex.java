package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("报表最后获取下标")
public class GameRecordEndIndex extends BaseEntity{
    @ApiModelProperty("wm注单id")
    private Long GameRecordId;

    @ApiModelProperty("PG电子注单id")
    private Long PGMaxId;

    @ApiModelProperty("CQ9电子注单id")
    private Long CQ9MaxId;

    @ApiModelProperty("SABASPORT注单id")
    private Long SABASPORTMaxId;

    @ApiModelProperty("OBDJ注单id")
    private Long OBDJMaxId;

    @ApiModelProperty("OBTY注单id")
    private Long OBTYMaxId;
}
