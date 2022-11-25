package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 管理后台用户表
 */
@Entity
@Data
@ApiModel("代理管理表")
@Table(indexes = {@Index(columnList = "createTime")})
public class ProxyUser extends BaseEntity {


    /**
     * 管理员账号
     */
    @Column(unique = true)
    @ApiModelProperty("管理员账号")
    private String userName;

    /**
     * 昵称
     */
    @ApiModelProperty("昵称")
    private String nickName;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String passWord;


    /**
     * 手机号码
     */
    @ApiModelProperty("手机号码")
    private String phone;


    /**
     * 谷歌验证码KEY
     */
    @ApiModelProperty("谷歌验证码KEY")
    private String gaKey;

    /**
     * 谷歌验证码是否绑定1=未绑定，2=已绑定
     */
    @ApiModelProperty("谷歌验证码是否绑定1=未绑定，2=已绑定")
    private String gaBind;

    /**
     * 代理邀请码
     */
    @ApiModelProperty("代理邀请码")
    private String proxyCode;

    /**
     * 代理角色 1：总代理 2：区域代理 3：基层代理
     */
    @ApiModelProperty("代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;

    /**
     * 区域代理ID
     */
    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    /**
     * 总代理ID
     */
    @ApiModelProperty("总代理ID")
    private Long firstProxy;

    @ApiModelProperty(value = "下级代理数")
    private Integer proxyUsersNum;
    /**
     * 是否锁定 1：正常 2：锁定, 3：删除
     */
    @ApiModelProperty("是否锁定 1：正常 2：锁定")
    private Integer userFlag;

    @ApiModelProperty("是否删除 1：正常 2：删除")
    private Integer isDelete;

    //校验用户帐号权限
    public static boolean checkUser(ProxyUser proxyUser) {
        if (proxyUser == null) {
            return false;
        }

        if (Constants.open != proxyUser.getUserFlag()) {
            return false;
        }
        if (Constants.open != proxyUser.getIsDelete()) {
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
