package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.business.ChargeBusiness;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.CollectionBankcardVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/charge")
@Api(tags = "银行卡线下充值")
public class ChargeController {

    @Autowired
    private ChargeBusiness chargeBusiness;
    @Autowired
    private CollectionBankcardService collectionBankcardService;
    @Autowired
    private BankInfoService bankInfoService;

    @GetMapping("/collect_bankcards")
    @ApiOperation("收款银行卡列表")
    @ResponseBody
    public ResponseEntity<List<CollectionBankcardVo>> bankList() {
        List<CollectionBankcard> bankcardList = collectionBankcardService.findByDisableOrderBySortIdAsc(0);
        if (CollectionUtils.isEmpty(bankcardList)) {
            return ResponseUtil.success();
        }
        List<CollectionBankcardVo> list = new ArrayList<>();
        CollectionBankcardVo vo = null;
        for (CollectionBankcard bankcard : bankcardList) {
            vo = new CollectionBankcardVo();
            BeanUtils.copyProperties(bankcard, vo);
            String bankId = bankcard.getBankId();
            BankInfo bankInfo = null;
            if (!ObjectUtils.isEmpty(bankId)) {
                bankInfo = bankInfoService.findById(Long.parseLong(bankId));
            }
            if (bankInfo != null) {
                vo.setBankName(bankInfo.getBankName());
            }
            list.add(vo);
        }
        return ResponseUtil.success(list);
    }

    @PostMapping("/submit")
    @ApiOperation("提交充值")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chargeAmount", value = "充值金额", required = true),
            @ApiImplicitParam(name = "remitType", value = "汇款方式，银行卡1，支付宝2，微信3,该字段暂时弃用，后端固定成银行卡", required = false),
            @ApiImplicitParam(name = "remitterName", value = "汇款人", required = true),
            @ApiImplicitParam(name = "bankcardId", value = "收款银行卡ID", required = true),
    })
    public ResponseEntity submitCharge(String chargeAmount,Integer remitType,String remitterName,Long bankcardId){
        if (CasinoWebUtil.checkNull(chargeAmount,remitterName,bankcardId)) {
            return ResponseUtil.parameterNotNull();
        }
        //根据产品呀要求，前端暂时注释掉汇款方式，前端发起的充值默认就是银行卡充值
        remitType = Constants.remitType_bank;
        ResponseEntity responseEntity = chargeBusiness.submitOrder(chargeAmount, remitType, remitterName,bankcardId, CasinoWebUtil.getAuthId());
        return responseEntity;
    }
}
