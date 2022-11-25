package com.qianyi.paycore.model;

import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity
public class User extends BaseEntity {

    private String name;
    @Column(unique = true)
    private String account;
    private String password;
    //谷歌秘钥
    private String secret;
    //帐号状态（1：启用，其他：禁用）
    private Integer state;

    //校验用户帐号权限
    public static boolean checkUser(User user) {
        if (user == null) {
            return false;
        }

        if (Constants.open != user.getState()) {
            return false;
        }
        return true;
    }

    /**
     * 校验用户信息。长度3-15位
     * @return
     */
    public static boolean checkLength(String... strs) {
        if (ObjectUtils.isEmpty(strs)) {
            return false;
        }
        for(String str:strs){
            if (ObjectUtils.isEmpty(strs)) {
                return false;
            }

            int length=str.length();
            if (length < 3 || length > 15) {
                return false;
            }
        }

        return true;
    }
}
