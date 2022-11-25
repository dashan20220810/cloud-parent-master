package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 管理后台用户表
 */
@Entity
@Data
@ApiModel("后台用户表")
public class SysUser extends BaseEntity {

    /**
     * 管理员账号
     */
    @Column(unique = true)
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 密码
     */
    private String passWord;

    /**
     * 随机盐，加密密码用
     */
    private String safeCode;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 是否锁定 1：正常 2：锁定, 3：删除
     */
    private Integer userFlag;


    /**
     * 在线状态 默认1离线，2：在线
     */
    private Integer currentState;

    /**
     * 谷歌状态 默认1开启， 2关闭
     */
    private Integer gaStatus;

    /**
     * 谷歌验证码KEY
     */
    private String gaKey;

    /**
     * 谷歌验证码是否绑定1=未绑定，2=已绑定
     */
    private String gaBind;

    /**
     * 最后登录ip
     */
    private String LastLoginIp;

    /**
     * 在线时长
     */
    private String onLineTime;

    //校验用户帐号权限
    public static boolean checkUser(SysUser user) {
        if (user == null) {
            return false;
        }

        if (Constants.open != user.getUserFlag()) {
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
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("admin");
        authorities.add(authority);
        return authorities;
    }
}
