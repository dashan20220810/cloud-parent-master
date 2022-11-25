package com.qianyi.casinoweb.vo;

import com.qianyi.casinocore.model.GameRecordGoldenF;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@ApiModel("GoldenF的Response对象内容")
@Data
public class GameRecordObj {

    private String actionResult;

    private List<GameRecordGoldenF> betlogs = new ArrayList<>();

    private Integer total;

    private Long startTime;

    private Long endTime;

    private Integer page;

    private Integer pageCount;
}
