package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.LoginLogService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.BankcardsVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/userPegging")
@Api(tags = "客户中心")
public class UserPeggingController {

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;
    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     @param tag tag 反差类型 0 ip地址 1 银行卡号
     @param context 搜索内容
      * @return
     */
    @ApiOperation("客户反查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tag", value = "tag 反差类型 0 ip地址 1 银行卡号", required = true),
            @ApiImplicitParam(name = "context", value = "context 搜索内容", required = true),
    })
    @GetMapping("findUserPegging")
    public ResponseEntity<BankcardsVo> findUserPegging(Integer tag, String context){
        if (CasinoProxyUtil.checkNull(tag,context)){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag == CommonConst.NUMBER_0){//反查ip
            LoginLog loginLog = new LoginLog();
            loginLog.setIp(context);
            if (CasinoProxyUtil.setParameter(loginLog)){
                return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
            }
            loginLog.setType(CommonConst.NUMBER_2);
            List<LoginLog> registerList = loginLogService.findLoginLogList(loginLog);
            loginLog.setType(CommonConst.NUMBER_1);
            List<LoginLog> loginList = loginLogService.findLoginLogList(loginLog);
            if (loginList.size() > CommonConst.NUMBER_0){
                loginList = loginList.stream().filter(CommonUtil.distinctByKey(LoginLog::getAccount)).collect(Collectors.toList());
                if (!CasinoProxyUtil.checkNull(registerList)){
                    registerList.addAll(loginList);
                }else {
                    registerList = loginList;
                }
            }
            return ResponseUtil.success(registerList);
        }else if(tag == CommonConst.NUMBER_1){//反查银行卡号
            Bankcards bankcards = new Bankcards();
            if (CasinoProxyUtil.setParameter(bankcards)){
                return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
            }
            bankcards.setBankAccount(context);
            List<Bankcards> userBank = bankcardsService.findUserBank(bankcards);
            List<BankcardsVo> bankcardsVoList = new LinkedList<>();
            if(userBank != null && userBank.size() > 0){
                List<Long> userIds = userBank.stream().map(Bankcards::getUserId).collect(Collectors.toList());
                List<User> userList = userService.findAll(userIds);
                if(userList != null){
                    userBank.stream().forEach( bankcard->{
                        BankcardsVo vo = new BankcardsVo();
                        vo.setAddress(bankcard.getAddress());
                        vo.setBankAccount(bankcard.getBankAccount());
                        vo.setBankId(bankcard.getBankId());
                        vo.setRealName(bankcard.getRealName());
                        vo.setUserId(bankcard.getUserId());
                        vo.setDefaultCard(bankcard.getDefaultCard());
                        userList.stream().forEach(user->{
                            if (user.getId().equals(bankcard.getUserId())){
                                vo.setAccount(user.getAccount());
                            }
                        });
                        bankcardsVoList.add(vo);
                    });
                }
                userIds.clear();
                userList.clear();
            }
            userBank.clear();
            return ResponseUtil.success(bankcardsVoList);
        }
        return ResponseUtil.fail();
    }
}
