package com.qianyi.casinoproxy.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.UserThirdVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/userThird")
@Api(tags = "客户中心")
public class UserThirdController {
    @Autowired
    private UserThirdService userThirdService;

    @Autowired
    private UserService userService;


    @ApiOperation("根据我方用户账号查询三方账号")
    @GetMapping("/findUserThird")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userAccount", value = "用户账号", required = true),
        @ApiImplicitParam(name = "tag", value = "tag 0 用我方账号查第三方账号 ,1 第三方账号查我方账号", required = true),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG/CQ9,OBDJ,OBTY,SABA", required = false),
    })
    public ResponseEntity<UserThirdVo> findUserThird(String userAccount,Integer tag,String platform){
        if (CasinoProxyUtil.checkNull(tag,userAccount)){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag != CommonConst.NUMBER_0 && tag != CommonConst.NUMBER_1){
            return ResponseUtil.custom("参数不合法");
        }
        if(tag == CommonConst.NUMBER_1 && CasinoProxyUtil.checkNull(platform)){
            return ResponseUtil.custom("参数不合法");
        }
        User user;
        UserThird userThird;
        JSONArray json = new JSONArray();
        if (tag == CommonConst.NUMBER_0){
            user = userService.findByAccount(userAccount);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.success();
            }
            userThird = userThirdService.findByUserId(user.getId());
            if (CasinoProxyUtil.checkNull(userThird)){
                return ResponseUtil.success();
            }
            if (CasinoProxyUtil.checkNull(platform)){
                if (!CasinoProxyUtil.checkNull(userThird.getAccount())){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getAccount());
                    jsonObject.put("platform","WM");
                    json.add(jsonObject);
                }
                if (!CasinoProxyUtil.checkNull(userThird.getGoldenfAccount())){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getGoldenfAccount());
                    jsonObject.put("platform","PG/CQ9/SABA");
                    json.add(jsonObject);
                }
                if (!CasinoProxyUtil.checkNull(userThird.getObdjAccount())){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getObdjAccount());
                    jsonObject.put("platform",Constants.PLATFORM_OBDJ);
                    json.add(jsonObject);
                }
                if (!CasinoProxyUtil.checkNull(userThird.getObtyAccount())){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getObtyAccount());
                    jsonObject.put("platform",Constants.PLATFORM_OBTY);
                    json.add(jsonObject);
                }
            }else if (platform.equals("WM")){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getAccount());
                jsonObject.put("platform","WM");
                json.add(jsonObject);
            }else if(platform.equals(Constants.PLATFORM_OBDJ)){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getObdjAccount());
                jsonObject.put("platform",Constants.PLATFORM_OBDJ);
                json.add(jsonObject);
            }else if(platform.equals(Constants.PLATFORM_OBTY)){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getObtyAccount());
                jsonObject.put("platform",Constants.PLATFORM_OBTY);
                json.add(jsonObject);
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getGoldenfAccount());
                jsonObject.put("platform","PG/CQ9/SABA");
                json.add(jsonObject);
            }
        }else{
            if (CasinoProxyUtil.checkNull(platform)){
                userThird = userThirdService.findByAccount(userAccount);
                if (!CasinoProxyUtil.checkNull(userThird)){
                    user = userService.findById(userThird.getUserId());
                    if (CasinoProxyUtil.checkNull(user)){
                        return ResponseUtil.success();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getAccount());
                    jsonObject.put("platform","WM");
                    json.add(jsonObject);
                    return ResponseUtil.success(json);
                }

                userThird =  userThirdService.findByGoldenfAccount(userAccount);
                if (!CasinoProxyUtil.checkNull(userThird)){
                    user = userService.findById(userThird.getUserId());
                    if (CasinoProxyUtil.checkNull(user)){
                        return ResponseUtil.success();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getGoldenfAccount());
                    jsonObject.put("platform","PG/CQ9/SABA");
                    json.add(jsonObject);
                    return ResponseUtil.success(json);
                }
                userThird =  userThirdService.findByObdjAccount(userAccount);
                if(!CasinoProxyUtil.checkNull(userThird)){
                    user = userService.findById(userThird.getUserId());
                    if (CasinoProxyUtil.checkNull(user)){
                        return ResponseUtil.success();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getObdjAccount());
                    jsonObject.put("platform",Constants.PLATFORM_OBDJ);
                    json.add(jsonObject);
                    return ResponseUtil.success(json);
                }
                userThird =  userThirdService.findByObtyAccount(userAccount);
                if (!CasinoProxyUtil.checkNull(userThird)){
                    user = userService.findById(userThird.getUserId());
                    if (CasinoProxyUtil.checkNull(user)){
                        return ResponseUtil.success();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("account",user.getAccount());
                    jsonObject.put("thirdAccount",userThird.getObtyAccount());
                    jsonObject.put("platform",Constants.PLATFORM_OBTY);
                    json.add(jsonObject);
                    return ResponseUtil.success(json);
                }

            }else if (platform.equals("WM")){
                userThird = userThirdService.findByAccount(userAccount);
                if (CasinoProxyUtil.checkNull(userThird)){
                    return ResponseUtil.success();
                }
                user = userService.findById(userThird.getUserId());
                if (CasinoProxyUtil.checkNull(user)){
                    return ResponseUtil.success();
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getAccount());
                jsonObject.put("platform","WM");
                json.add(jsonObject);
            }else if(platform.equals(Constants.PLATFORM_OBDJ)){
                userThird =  userThirdService.findByObdjAccount(userAccount);
                if (CasinoProxyUtil.checkNull(userThird)){
                    return ResponseUtil.success();
                }
                user = userService.findById(userThird.getUserId());
                if (CasinoProxyUtil.checkNull(user)){
                    return ResponseUtil.success();
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getObdjAccount());
                jsonObject.put("platform",Constants.PLATFORM_OBDJ);
                json.add(jsonObject);
            }else if(platform.equals(Constants.PLATFORM_OBTY)){
                userThird =  userThirdService.findByObtyAccount(userAccount);
                if (CasinoProxyUtil.checkNull(userThird)){
                    return ResponseUtil.success();
                }
                user = userService.findById(userThird.getUserId());
                if (CasinoProxyUtil.checkNull(user)){
                    return ResponseUtil.success();
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getObtyAccount());
                jsonObject.put("platform",Constants.PLATFORM_OBTY);
                json.add(jsonObject);
            }else {
                userThird =  userThirdService.findByGoldenfAccount(userAccount);
                if (CasinoProxyUtil.checkNull(userThird)){
                    return ResponseUtil.success();
                }
                user = userService.findById(userThird.getUserId());
                if (CasinoProxyUtil.checkNull(user)){
                    return ResponseUtil.success();
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account",user.getAccount());
                jsonObject.put("thirdAccount",userThird.getGoldenfAccount());
                jsonObject.put("platform","PG/CQ9/SABA");
                json.add(jsonObject);
            }

        }
        return ResponseUtil.success(json);
    }

}
