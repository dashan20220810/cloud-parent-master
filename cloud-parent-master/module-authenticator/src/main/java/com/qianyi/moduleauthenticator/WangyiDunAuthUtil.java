package com.qianyi.moduleauthenticator;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 网易云盾验证码
 */
public class WangyiDunAuthUtil {

    public static boolean  verify(String validate) {
        String url = "http://c.dun.163.com/api/v2/verify";
        String timestamp = System.currentTimeMillis() + "";
        String nonce = timestamp + CommonUtil.random(5);
        String secretKey = "0827adaf67d2c4014c55e86c92104274";
        Map<String, Object> params = new HashMap<>();
        params.put("captchaId", "a2eb62e9d6be4d4e945d5a403285b229");
        params.put("validate", validate);
        params.put("user", "");
        params.put("secretId", "fb00c2047073fc009ec0135df4f5d33f");
        params.put("version", "v2");
        params.put("timestamp", timestamp);
        params.put("nonce", nonce);

        try {
            String signature = genSignature(secretKey, params);
            params.put("signature", signature);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String s = HttpClient4Util.doPost(url, params);
        System.out.println(s);
        if (CommonUtil.checkNull(s)) {
            return false;
        }
        JSONObject jsonObject = JSONObject.parseObject(s);
        Boolean result = jsonObject.getBoolean("result");
        return result;

    }

    private static String genSignature(String secretKey, Map<String, Object> params) throws UnsupportedEncodingException {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append(params.get(key));
        }
        // 3. 将secretKey拼接到最后
        sb.append(secretKey);

        // 4. MD5是128位长度的摘要算法，转换为十六进制之后长度为32字符
        return DigestUtils.md5Hex(sb.toString().getBytes("UTF-8"));
    }

    public static void main(String[] args) {
        String validate = "CN31_rZQV0bADDvXR9M-rDZOpvek9z74AXeIitoNmpWybrNJR5p6Mx_lnYEWtEuazD909yB2N6NL8aBBjh1Y8K9po5jfAviNQ77GV0s2Qbay9N.ch1NmrRPAOEH.6ckkelZ59hjspq00Zmnzar7YrngTdVS.dyua6oqmojTweeFU5CHFU2UCN_gQx4VwAlq8P4PpJTPkTMZUq_iIJI7oLIkZIJqHeh.gckuEAtSdMuYrOtoBmxUDdHQpMKPKV1MhGVxqxBpAGeGcNllI6CShNTA_vXXwzHxLcVSJkUYTLtnjmwX-zCk82wNQMbOkXWwpckos.7oNVOrRPtsgwf7Mh7lqjEQ4DhfPMvZCyXZME650tHLkOTnIyHbpC47IIjRpuV.4EFS6sjK.dV2-_uLCGl_L9n.wtzEowxUVjzxW1wiLoEuOcdfQuULgSyFvuCkZkUBDEKydmExUVNtLVVx07MAYws69pGib0e9JE5fcLdv4CH_9tw0A4iqnbmh7Uu0j3";
        boolean verify = WangyiDunAuthUtil.verify(validate);
        System.out.println(verify);
    }

}
