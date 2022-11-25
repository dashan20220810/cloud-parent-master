package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.AccountChangeBackVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.AccountChangeService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "报表中心")
@RestController
@RequestMapping("accountChange")
public class AccountChangeController {
    @Autowired
    private AccountChangeService accountChangeService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageUtil messageUtil;
    /**
     * 分页查询用户账变
     *
     * @param orderNo 订单号
     * @param type 账变类型
     * @param account 会员账号
     * @return
     */
    @ApiOperation("分页查询用户账变")
    @GetMapping("/findAccountChangePage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
        @ApiImplicitParam(name = "type", value = "账变类型", required = false),
        @ApiImplicitParam(name = "account", value = "会员账号", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<AccountChangeBackVo> findAccountChangePage(Integer pageSize, Integer pageCode, String type, String account, String orderNo,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        AccountChange accountChange = new AccountChange();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            accountChange.setUserId(user.getId());
        }
        accountChange.setOrderNo(orderNo);
        String[] types = null;
        try {
            if (!LoginUtil.checkNull(type)){
                types = type.split(CommonConst.COMMA_SPLIT);
            }
        }catch (Exception ex){
            return ResponseUtil.custom("参数不合法");
        }
        Page<AccountChange> accountChangePage = accountChangeService.findAccountChangePage(pageable,types, accountChange,startDate,endDate);
        PageResultVO<AccountChangeBackVo> pageResultVO = new PageResultVO(accountChangePage);
        List<AccountChange> content = accountChangePage.getContent();
        if(content != null && content.size() > 0){
            List<AccountChangeBackVo> accountChangeVoList =new LinkedList<>();
            List<Long> userIds = content.stream().map(AccountChange::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(change ->{
                    AccountChangeBackVo accountChangeVo = new AccountChangeBackVo(change);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(change.getUserId())){
                            accountChangeVo.setAccount(user.getAccount());
                        }
                    });
                    accountChangeVoList.add(accountChangeVo);
                });
            }
            pageResultVO.setContent(accountChangeVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("查询账变类型")
    @GetMapping("/getData")
    @NoAuthorization
    public ResponseEntity getData(){
        AccountChangeEnum[] values = AccountChangeEnum.values();
        Map<Integer,String> map = new HashMap<>();
        for (AccountChangeEnum accountChangeEnum:values){
            map.put(accountChangeEnum.getType(),messageUtil.get(accountChangeEnum.getName()));
        }
        return ResponseUtil.success(map);
    }
}
