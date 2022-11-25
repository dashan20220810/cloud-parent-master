package com.qianyi.sms.buka;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.response.ResponseCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.TreeMap;

@RestController
@RequestMapping("buka")
@Api(value = "不卡短信")
public class BukaController {

    private final static String url_half = "https://api.onbuka.com/v3/";

    @PostMapping("sendRegister")
    @ApiOperation("注册短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchant", value = "商户代号。找平台获取", required = true),
            @ApiImplicitParam(name = "country", value = "86.中国（默认），855.柬埔寨，60：马来。66，泰国 ", required = false),
            @ApiImplicitParam(name = "phone", value = "电话", required = true),
            @ApiImplicitParam(name = "code", value = "验证码", required = true),
            @ApiImplicitParam(name = "language", value = "1. 中文（默认） 2. 英文。 3. 柬文. 4. 马来。 5.泰文", required = false),
    })
    public ResponseEntity sendRegister(String merchant, String country, String phone, String code, Integer language) {
        if (CommonUtil.checkNull(phone, code, merchant)) {
            return ResponseUtil.parameterNotNull();
        }

        if (!checkMerchant(merchant)) {
            return ResponseUtil.custom("不支持的商户");
        }

        if (!checkCountry(country)) {
            return ResponseUtil.custom("不支持的国家");
        }

        if (!checkLanguage(language)) {
            return ResponseUtil.custom("不支持的语言");
        }

        String url = getUrl("sendSms");
        TreeMap<String, Object> params = new TreeMap<>();
//        params.put("appId", "xb2jVhUB");
        params.put("appId", "XVi0FJNS");
        phone = country + phone;
        params.put("numbers", phone);
        String content = getContent(merchant, language, code);
        params.put("content", content);
//        //发送号码
//        params.put("senderId", senderId);

        String result = BukaHttpClient4Util.doPost(url, params);
        System.out.println(result);
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer status = jsonObject.getInteger("status");
            if (status != 0) {
                String message = jsonObject.getString("reason");
                if (!ObjectUtils.isEmpty(message)) {
                    return ResponseUtil.custom(message);
                }
                return ResponseUtil.custom(ResponseCode.getMsgByCode(status));
            }

//            JSONArray array = jsonObject.getJSONArray("array");
//            JSONObject arrayObject = (JSONObject) array.get(0);
//            String msgId = arrayObject.getString("msgId");
            return ResponseUtil.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.custom("发送失败，请重新操作");
        }

    }

    @PostMapping("balance")
    @ApiOperation("查询平台余额")
    public ResponseEntity balance() {
        String url = getUrl("getBalance");
        String result = BukaHttpClient4Util.doGet(url);
        System.out.println(result);

        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer status = jsonObject.getInteger("status");
            if (status != 0) {
                String message = jsonObject.getString("reason");
                if (!ObjectUtils.isEmpty(message)) {
                    return ResponseUtil.custom(message);
                }
                return ResponseUtil.custom(ResponseCode.getMsgByCode(status));
            }

            String balance = jsonObject.getString("balance");
            return ResponseUtil.success(balance);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail();
        }
    }

    private boolean checkMerchant(String merchant) {

        switch (merchant) {
            //casino项目
            case "js":
                return true;
        }
        return false;
    }

    private String getContent(String merchant, Integer language, String code) {
        String content = "【" + merchant + "】" + " 您的验证码为:" + code + ",5分钟内有效";
        switch (language) {
            case 2:
                content = "【" + merchant + "】" + "verification code:" + code + ",valid within 5 minutes";
                break;
            case 3:
                content = "【" + merchant + "】" + "កូដ ផ្ទៀង ផ្ទាត់ :" + code + ",មាន\u200Bសុពលភាព\u200Bក្នុង\u200B៥ នាទី";
                break;
            case 4:
                content = "【" + merchant + "】" + "Kod pengesahan :" + code + ",sah selama 5 minit";
                break;
            //TODO
            case 5:
                content = "【" + merchant + "】" + "รหัสยืนยัน:" + code + ",จะมีอายุ 5 นาที";
                break;
            default:
        }
        return content;
    }

    private boolean checkCountry(String country) {
        if (country == null) {
            country = "86";
        }

        switch (country) {
            case "86":
            case "855":
            case "60":
            case "66":
                return true;
        }

        return false;
    }

    private boolean checkLanguage(Integer language) {
        if (language == null) {
            language = 1;
        }

        switch (language) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
        }
        return false;
    }

    private String getUrl(String apiName) {
        return url_half.concat(apiName);
    }


    public static void main(String[] args) {
        BukaController controller = new BukaController();
        ResponseEntity responseEntity = controller.sendRegister("js", "60", "1172692858", "123456", 4);
//        ResponseEntity responseEntity = controller.balance();
        System.out.println(JSONObject.toJSONString(responseEntity));
    }
}
