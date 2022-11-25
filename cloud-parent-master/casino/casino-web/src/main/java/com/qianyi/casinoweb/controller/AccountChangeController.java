package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.service.AccountChangeService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "资金中心")
@RestController
@RequestMapping("accountChange")
public class AccountChangeController {
    @Autowired
    private AccountChangeService accountChangeService;

    @ApiOperation("用户资金详情")
    @GetMapping("/accountChangeList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "date", value = "时间：全部：不传值，0：今天，1：昨天，2：一个月内", required = false)
    })
    public ResponseEntity<Page<AccountChange>> findAccountChangePage(Integer pageSize, Integer pageCode, String date) {
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoWebUtil.setPageable(pageCode, pageSize, sort);
        String startTime = null;
        String endTime = null;
        if ("0".equals(date)) {
            startTime = DateUtil.getStartTime(0);
            endTime = DateUtil.getEndTime(0);
        } else if ("1".equals(date)) {
            startTime = DateUtil.getStartTime(-1);
            endTime = DateUtil.getEndTime(-1);
        } else if ("2".equals(date)) {
            startTime = DateUtil.getMonthAgoStartTime(-1);
            endTime = DateUtil.getEndTime(0);
        }
        Long userId = CasinoWebUtil.getAuthId();
        Specification<AccountChange> condition = this.getCondition(userId,startTime, endTime);
        Page<AccountChange> accountChangePage = accountChangeService.findAccountChange(condition, pageable);
        List<AccountChange> content = accountChangePage.getContent();
        if (!CollectionUtils.isEmpty(content)) {
            for (AccountChange change : content) {
                change.setAmount(change.getAmount().abs());//前端金额显示正数
            }
        }
        return ResponseUtil.success(accountChangePage);
    }

    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param
     * @return
     */
    private Specification<AccountChange> getCondition(Long userId, String startTime, String endTime) {
        Specification<AccountChange> specification = new Specification<AccountChange>() {
            @Override
            public Predicate toPredicate(Root<AccountChange> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                list.add(cb.equal(root.get("userId").as(Long.class), userId));
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("createTime").as(String.class), startTime, endTime));
                }
                int[] types = {AccountChangeEnum.WASH_CODE.getType(), AccountChangeEnum.WM_IN.getType(), AccountChangeEnum.RECOVERY.getType(),
                        AccountChangeEnum.SHARE_PROFIT.getType(), AccountChangeEnum.PG_CQ9_IN.getType(), AccountChangeEnum.PG_CQ9_OUT.getType(),
                        AccountChangeEnum.OBDJ_IN.getType(), AccountChangeEnum.OBDJ_OUT.getType(), AccountChangeEnum.OBTY_IN.getType(),AccountChangeEnum.OBTY_OUT.getType(),
                        AccountChangeEnum.SABASPORT_IN.getType(),AccountChangeEnum.SABASPORT_OUT.getType()};
                CriteriaBuilder.In<Object> in = cb.in(root.get("type"));
                for (int type : types) {
                    in.value(type);
                }
                list.add(cb.and(cb.and(in)));
                predicate = cb.and(list.toArray(new Predicate[list.size()]));

                return predicate;
            }
        };
        return specification;
    }
}
