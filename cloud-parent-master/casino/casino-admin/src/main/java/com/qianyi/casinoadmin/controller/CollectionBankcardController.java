package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.CollectionBankcardVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户谷歌验证登录
 */
@Api(tags = "资金中心")
@RestController
@RequestMapping("collection")
public class CollectionBankcardController {

    @Autowired
    private CollectionBankcardService collectionBankcardService;
    
    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("bankList")
    @ApiOperation("收款银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "disable", value = "0:未禁用 1：禁用", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行类型", required = false),
    })
    public ResponseEntity<CollectionBankcardVo> bankList(Integer pageSize, Integer pageCode,Integer disable,String bankId) {
        Sort sort = Sort.by("sortId").ascending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        CollectionBankcard collectionBankcard = new CollectionBankcard();
        collectionBankcard.setDisable(disable);
        collectionBankcard.setBankId(bankId);
        Page<CollectionBankcard> collectionBandPage = collectionBankcardService.getCollectionBandPage(collectionBankcard, pageable);
        PageResultVO<CollectionBankcardVo> pageResultVO =new PageResultVO(collectionBandPage);
        List<CollectionBankcard> content = collectionBandPage.getContent();
        if(content != null && content.size() > 0){
            List<CollectionBankcardVo> collectionBankcardList = new LinkedList<>();
            List<String> collect = content.stream().map(CollectionBankcard::getBankId).collect(Collectors.toList());
            List<BankInfo> bankInfos = bankInfoService.findAll(collect);
            List<String> updateBys = content.stream().map(CollectionBankcard::getUpdateBy).collect(Collectors.toList());
            List<SysUser> sysUsers = sysUserService.findAll(updateBys);
            if(bankInfos != null){
                content.stream().forEach(collectionBank ->{
                    CollectionBankcardVo collectionBankcardVo = new CollectionBankcardVo(collectionBank);
                    bankInfos.stream().forEach(bank->{
                        if (bank.getId().toString().equals(collectionBank.getBankId())){
                            collectionBankcardVo.setBankName(bank.getBankName());
                        }
                    });
                     sysUsers.stream().forEach(sysUser->{
                        if (sysUser.getId().toString().equals(collectionBank.getUpdateBy() == null?"":collectionBank.getUpdateBy())){
                            collectionBankcardVo.setUpdateBy(sysUser.getUserName());
                        }
                    });
                    collectionBankcardList.add(collectionBankcardVo);
                });
            }
            pageResultVO.setContent(collectionBankcardList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("新增收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankNo", value = "银行账号", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "accountName", value = "开户名", required = true),
            @ApiImplicitParam(name = "sortFlag", value = "是否置顶 1：置顶，0：不置顶", required = true),
    })
    @PostMapping("saveBankInfo")
    public ResponseEntity saveBankInfo(String bankNo, String bankId, String accountName, Integer sortFlag){
        List<CollectionBankcard> byBankNo = collectionBankcardService.findByBankNo(bankNo);
        if (LoginUtil.checkNull(bankNo,bankId,accountName)) {
            return ResponseUtil.custom("银行账号不能为空");
        }
        bankNo = bankNo.trim();
        if (bankNo.length() > 20 || bankNo.length() < 8 || !bankNo.matches(Constants.regex)) {
            return ResponseUtil.custom("长度只能在8~20位的数字");
        }
        if (!accountName.matches(RegexEnum.NAME.getRegex())){
            return ResponseUtil.custom("持卡人姓名格式错误");
        }
        if (!LoginUtil.checkNull(byBankNo) && byBankNo.size() > CommonConst.NUMBER_0){
            return ResponseUtil.custom("银行卡已存在");
        }
        Sort so = Sort.by("sortId").ascending();
        List<CollectionBankcard> collectionBankcardList = collectionBankcardService.findAllSort(so);

        CollectionBankcard bankcard = new CollectionBankcard();
        bankcard.setBankNo(bankNo);
        bankcard.setBankId(bankId);
        bankcard.setAccountName(accountName);
        bankcard.setDisable(CommonConst.NUMBER_1);//新增默认禁用
        if(collectionBankcardList == null || collectionBankcardList.isEmpty()){
            bankcard.setSortId(CommonConst.NUMBER_1);
            collectionBankcardService.save(bankcard);
            return ResponseUtil.success(bankcard);
        }
        bankcard.setSortId(collectionBankcardList.size() + 1);
        collectionBankcardService.save(bankcard);
        collectionBankcardList.add(bankcard);
        if(sortFlag == CommonConst.NUMBER_1){//置顶操作
            if(collectionBankcardList.get(CommonConst.NUMBER_0).getSortId() == null
                    || collectionBankcardList.get(CommonConst.NUMBER_0).getSortId() != CommonConst.NUMBER_1){
                setCollectionBankcardSoet(collectionBankcardList);
            }
            saveBankInfoNumber3(collectionBankcardList, bankcard.getId());
        }
        return ResponseUtil.success(bankcard);
    }

    @ApiOperation("修改收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "bankNo", value = "银行账号", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = false),
            @ApiImplicitParam(name = "accountName", value = "开户名", required = false)
    })
    @PostMapping("updateBankInfo")
    public ResponseEntity updateBankInfo(Long id, String bankNo, String bankId, String accountName){
        CollectionBankcard collectionBankcard = collectionBankcardService.findById(id);
        if(collectionBankcard == null){
            return ResponseUtil.custom("银行卡不存在");
        }
        if(!LoginUtil.checkNull(bankNo)){
            List<CollectionBankcard> byBankNo = collectionBankcardService.findByBankNo(bankNo);
            if (!LoginUtil.checkNull(byBankNo) && byBankNo.size() > CommonConst.NUMBER_0
                    && !byBankNo.get(CommonConst.NUMBER_0).getBankNo().equals(collectionBankcard.getBankNo())){
                return ResponseUtil.custom("银行卡已存在");
            }
            if (bankNo.length() > 20 || bankNo.length() < 8 || !bankNo.matches(Constants.regex)) {
                return ResponseUtil.custom("长度只能在8~20位的数字");
            }
            collectionBankcard.setBankNo(bankNo);
        }
        if(!LoginUtil.checkNull(bankId)){
            collectionBankcard.setBankId(bankId);
        }
        if(!LoginUtil.checkNull(accountName)){
            if (!accountName.matches(RegexEnum.NAME.getRegex())){
                return ResponseUtil.custom("持卡人请输入中文或字母");
            }
            collectionBankcard.setAccountName(accountName);
        }

        collectionBankcardService.save(collectionBankcard);
        return ResponseUtil.success(collectionBankcard);
    }

    @NoAuthentication
    @GetMapping("sortBankInfo")
    @ApiOperation("收款银行卡排序")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "sort", value = "排序 1：上移，2：下移动，3置顶", required = true)
    })
    public ResponseEntity sortBankInfo(Long id, Integer sort){
        Sort so = Sort.by("sortId").ascending();
        List<CollectionBankcard> collectionBankcardList = collectionBankcardService.findAllSort(so);

        //判断第一次操作排序字段为空时情况
        if(collectionBankcardList.get(CommonConst.NUMBER_0).getSortId() == null
                || collectionBankcardList.get(CommonConst.NUMBER_0).getSortId() != CommonConst.NUMBER_1){
            setCollectionBankcardSoet(collectionBankcardList);
        }

        //置顶操作
        if(sort == CommonConst.NUMBER_3){
            saveBankInfoNumber3(collectionBankcardList, id);
        }
        //上移
        if(sort == CommonConst.NUMBER_1){
            saveBankInfoNumber1(collectionBankcardList, id);
        }
        //下移
        if(sort == CommonConst.NUMBER_2){
            saveBankInfoNumber2(collectionBankcardList, id);
        }
        return ResponseUtil.success();
    }

    /**
     * 初始化值
     *
     * @param collectionBankcardList
     */
    private void setCollectionBankcardSoet(List<CollectionBankcard> collectionBankcardList) {
        int sort = 1;
        for (CollectionBankcard collectionBankcard : collectionBankcardList) {
            collectionBankcard.setSortId(sort);
            sort++;
        }
        collectionBankcardService.saveAll(collectionBankcardList);
    }

    /**
     * 下移
     *
     * @param collectionBankcardList
     * @param id
     */
    private void saveBankInfoNumber2(List<CollectionBankcard> collectionBankcardList, Long id) {
        for (int i = 0; i < collectionBankcardList.size(); i++) {
            if(collectionBankcardList.get(i).getId() == id){
                if((i + 1) < collectionBankcardList.size()){
                    int sortUp = collectionBankcardList.get(i).getSortId() + 1;
                    int sortdow = collectionBankcardList.get(i + 1).getSortId() - 1;
                    List<CollectionBankcard> collectionBankcards = new ArrayList<>();
                    collectionBankcardList.get(i).setSortId(sortUp);
                    collectionBankcardList.get(i + 1).setSortId(sortdow);
                    collectionBankcards.add(collectionBankcardList.get(i));
                    collectionBankcards.add(collectionBankcardList.get(i + 1));
                    collectionBankcardService.saveAll(collectionBankcards);
                }
            }
        }
    }

    /**
     * 上移动操作
     *
     * @param collectionBankcardList
     * @param id
     */
    private void saveBankInfoNumber1(List<CollectionBankcard> collectionBankcardList, Long id) {
        for (int i = 0; i < collectionBankcardList.size(); i++) {
            if(collectionBankcardList.get(i).getId() == id){
                if(collectionBankcardList.get(i).getSortId() != CommonConst.NUMBER_1){
                    int sortUp = collectionBankcardList.get(i).getSortId() - 1;
                    int sortdow = collectionBankcardList.get(i - 1).getSortId() + 1;
                    List<CollectionBankcard> collectionBankcards = new ArrayList<>();
                    collectionBankcardList.get(i).setSortId(sortUp);
                    collectionBankcardList.get(i - 1).setSortId(sortdow);
                    collectionBankcards.add(collectionBankcardList.get(i));
                    collectionBankcards.add(collectionBankcardList.get(i - 1));
                    collectionBankcardService.saveAll(collectionBankcards);
                }
            }
        }
    }

    /**
     * 置顶操作
     *
     * @param collectionBankcardList
     * @param id
     */
    private void saveBankInfoNumber3(List<CollectionBankcard> collectionBankcardList, Long id) {
        int sort = 2;
        for (CollectionBankcard collectionBankcard : collectionBankcardList) {
            if(collectionBankcard.getId() == id){
                collectionBankcard.setSortId(CommonConst.NUMBER_1);
            }else{
                collectionBankcard.setSortId(sort);
                sort ++;
            }
        }
        collectionBankcardService.saveAll(collectionBankcardList);
    }

    @GetMapping("deleteBankInfo")
    @ApiOperation("删除收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true)
    })
    public ResponseEntity deleteBankInfo(Long id){
        CollectionBankcard collectionBankcard = collectionBankcardService.findById(id);
        if(LoginUtil.checkNull(collectionBankcard)){
            return ResponseUtil.custom("银行卡不存在");
        }
        if (collectionBankcard.getDisable() == CommonConst.NUMBER_0){
            return ResponseUtil.custom("开启状态不能删除");
        }
        Sort so = Sort.by("sortId").ascending();
        List<CollectionBankcard> collectionBankcardList = collectionBankcardService.findAllSort(so);
        if(collectionBankcardList.get(CommonConst.NUMBER_0).getSortId() == null
                || collectionBankcardList.get(CommonConst.NUMBER_0).getSortId() != CommonConst.NUMBER_1){
            setCollectionBankcardSoet(collectionBankcardList);
        }
        List<CollectionBankcard> collectionBankcards = new ArrayList<>();
        int sort = CommonConst.NUMBER_0;
        for (CollectionBankcard bankcard : collectionBankcards) {
            if(id == bankcard.getId()){
                sort = bankcard.getSortId();
            }
        }
        if(sort == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        for (int i = 0; i < collectionBankcardList.size(); i++) {
            Integer sortId = collectionBankcardList.get(i).getSortId();
            if(sort < sortId){
                collectionBankcardList.get(i).setSortId(collectionBankcardList.get(i).getSortId() - 1);
                collectionBankcards.add(collectionBankcardList.get(i));
            }
        }

        collectionBankcardService.delete(collectionBankcard);
        collectionBankcardService.saveAll(collectionBankcards);
        return ResponseUtil.success();
    }

    @ApiOperation("修改银行卡状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true)
    })
    @PostMapping("updateDisable")
    public ResponseEntity updateDisable(Long id){
        CollectionBankcard collectionBankcard = collectionBankcardService.findById(id);
        if(LoginUtil.checkNull(collectionBankcard)){
            return ResponseUtil.custom("银行卡不存在");
        }
        return this.checkNumber(collectionBankcard);
    }

    public synchronized ResponseEntity checkNumber( CollectionBankcard collectionBankcard){
        if(collectionBankcard.getDisable() == Constants.BANK_OPEN){
            collectionBankcard.setDisable(Constants.BANK_CLOSE);
        }else{
            CollectionBankcard collection = new CollectionBankcard();
            collection.setDisable(Constants.BANK_OPEN);
            List<CollectionBankcard> all = collectionBankcardService.findAll(collection);
            if (all.size() >= CommonConst.NUMBER_4){
                return ResponseUtil.custom("收款卡最多上架4张");
            }
            collectionBankcard.setDisable(Constants.BANK_OPEN);
        }
        collectionBankcardService.save(collectionBankcard);
        return ResponseUtil.success(collectionBankcard);
    }
}
