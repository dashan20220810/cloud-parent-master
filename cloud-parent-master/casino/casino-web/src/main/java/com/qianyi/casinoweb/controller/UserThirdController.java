package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserThirdRepository;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/userThird")
@Api(tags = "三方账户")
@Slf4j
public class UserThirdController {

    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private UserThirdRepository userThirdRepository;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/compareAccount")
    @ApiOperation("比较三方数据库和redis账户不一致的用户")
    @NoAuthentication
    public ResponseEntity compareAccount() {
        Map<String, Object> notMatch = new HashMap<>();
        //比对所有wm账号
        List<Map<String, Object>> wmNotMatchList = new ArrayList<>();
        Set<String> wmKeys = redisUtil.getKeysByPrex("userThird::findByAccount::*");
        for (String key : wmKeys) {
            Map<String, Object> map = new HashMap<>();
            String account = key.substring(key.lastIndexOf("::") + 2, key.length());
            UserThird third = userThirdService.findByAccount(account);
            if (ObjectUtils.isEmpty(third)) {
                continue;
            }
            UserThird userThird = userThirdRepository.findByAccount(third.getAccount());
            if (ObjectUtils.isEmpty(userThird) || !third.getId().equals(userThird.getId())) {
                map.put("db", userThird);
                map.put("cache", third);
                wmNotMatchList.add(map);
            }
        }
        notMatch.put(Constants.PLATFORM_WM_BIG, wmNotMatchList);
        log.error("WM不匹配的账号:{}",JSON.toJSONString(wmNotMatchList));

        //比对所有goldenf账号
        List<Map<String, Object>> goldenfNotMatchList = new ArrayList<>();
        Set<String> goldenfKeys = redisUtil.getKeysByPrex("userThird::findByGoldenfAccount::*");
        for (String key : goldenfKeys) {
            Map<String, Object> map = new HashMap<>();
            String account = key.substring(key.lastIndexOf("::") + 2, key.length());
            UserThird third = userThirdService.findByGoldenfAccount(account);
            if (ObjectUtils.isEmpty(third)) {
                continue;
            }
            UserThird userThird = userThirdRepository.findByGoldenfAccount(third.getGoldenfAccount());
            if (ObjectUtils.isEmpty(userThird) || !third.getId().equals(userThird.getId())) {
                map.put("db", userThird);
                map.put("cache", third);
                goldenfNotMatchList.add(map);
            }
        }
        notMatch.put(Constants.PLATFORM_PG_CQ9, goldenfNotMatchList);
        log.error("goldenF不匹配的账号:{}",JSON.toJSONString(goldenfNotMatchList));

        //比对所有obdj账号
        List<Map<String, Object>> obdjNotMatchList = new ArrayList<>();
        Set<String> obdjKeys = redisUtil.getKeysByPrex("userThird::findByObdjAccount::*");
        for (String key : obdjKeys) {
            Map<String, Object> map = new HashMap<>();
            String account = key.substring(key.lastIndexOf("::") + 2, key.length());
            UserThird third = userThirdService.findByObdjAccount(account);
            if (ObjectUtils.isEmpty(third)) {
                continue;
            }
            UserThird userThird = userThirdRepository.findByObdjAccount(third.getObdjAccount());
            if (ObjectUtils.isEmpty(userThird) || !third.getId().equals(userThird.getId())) {
                map.put("db", userThird);
                map.put("cache", third);
                obdjNotMatchList.add(map);
            }
        }
        notMatch.put(Constants.PLATFORM_OBDJ, obdjNotMatchList);
        log.error("OBDJ不匹配的账号:{}",JSON.toJSONString(obdjNotMatchList));

        //比对所有obty账号
        List<Map<String, Object>> obtyNotMatchList = new ArrayList<>();
        Set<String> obtyKeys = redisUtil.getKeysByPrex("userThird::findByObtyAccount::*");
        for (String key : obtyKeys) {
            Map<String, Object> map = new HashMap<>();
            String account = key.substring(key.lastIndexOf("::") + 2, key.length());
            UserThird third = userThirdService.findByObtyAccount(account);
            if (ObjectUtils.isEmpty(third)) {
                continue;
            }
            UserThird userThird = userThirdRepository.findByObtyAccount(third.getObtyAccount());
            if (ObjectUtils.isEmpty(userThird) || !third.getId().equals(userThird.getId())) {
                map.put("db", userThird);
                map.put("cache", third);
                obtyNotMatchList.add(map);
            }
        }
        notMatch.put(Constants.PLATFORM_OBTY, obtyNotMatchList);
        log.error("OBTY不匹配的账号:{}",JSON.toJSONString(obtyNotMatchList));

        //比对所有findByUserId账号
        List<Map<String, Object>> findByUserIdNotMatchList = new ArrayList<>();
        Set<String> findByUserIdKeys = redisUtil.getKeysByPrex("userThird::*");
        for (String key : findByUserIdKeys) {
            Map<String, Object> map = new HashMap<>();
            if (key.contains("findByAccount") || key.contains("findByGoldenfAccount") || key.contains("findByObdjAccount") || key.contains("findByObtyAccount")) {
                continue;
            }
            String account = key.substring(key.lastIndexOf("::") + 2, key.length());
            if (ObjectUtils.isEmpty(account)){
                continue;
            }
            Pattern pattern = Pattern.compile("[0-9]*");
            boolean matches = pattern.matcher(account).matches();
            if (!matches){
                continue;
            }
            UserThird third = userThirdService.findByUserId(Long.parseLong(account));
            if (ObjectUtils.isEmpty(third)) {
                continue;
            }
            UserThird userThird = userThirdRepository.findByUserId(third.getUserId());
            if (ObjectUtils.isEmpty(userThird) || !third.getId().equals(userThird.getId())) {
                map.put("db", userThird);
                map.put("cache", third);
                findByUserIdNotMatchList.add(map);
            }
        }
        notMatch.put("findByUserId", findByUserIdNotMatchList);
        log.error("findByUserId不匹配的账号:{}",JSON.toJSONString(findByUserIdNotMatchList));
        return ResponseUtil.success(notMatch);
    }
}
