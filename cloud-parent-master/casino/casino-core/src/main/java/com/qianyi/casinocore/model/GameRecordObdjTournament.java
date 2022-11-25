package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="game_record_obdj_tournament",uniqueConstraints={@UniqueConstraint(columnNames={"tournamentId"})})
@ApiModel("OB电竞游戏记录锦标赛")
public class GameRecordObdjTournament extends BaseEntity{

    @ApiModelProperty(value = "联赛ID")
    private Long tournamentId;

    @ApiModelProperty(value = "联赛名称")
    private String tournamentName;
}
