package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
@Table(indexes = {@Index(columnList = "createTime")})
public class User extends BaseEntity implements UserDetails {
    @ApiModelProperty("名字")
    private String name;
    @Column(unique = true)
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("登录密码")
    private String password;
    @ApiModelProperty("手机")
    private String phone;
    @ApiModelProperty("语言")
    private Integer language;
    @ApiModelProperty("头像")
    private String headImg;
    //帐号状态（1：启用，其他：禁用）
    @ApiModelProperty("帐号状态（1：启用，其他：禁用）")
    private Integer state;
    @ApiModelProperty("注册ip")
    private String registerIp;
    @ApiModelProperty("收款卡张数")
    private Integer creditCard;
    @ApiModelProperty("配置收款卡等级")//null 完全随机  或者 A|B|C|D
    private String cardLevel;
    @ApiModelProperty("取款密码")
    private String withdrawPassword;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("qq")
    private String qq;
    @ApiModelProperty("微信")
    private String webChat;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("直属父级ID")
    private Long firstPid;
    @ApiModelProperty("第二级ID")
    private Long secondPid;
    @ApiModelProperty("第三级ID")
    private Long thirdPid;
    @Column(unique = true)
    @ApiModelProperty("邀请码")
    private String inviteCode;
    @ApiModelProperty("是否是首次下注：0是，1,否")
    private Integer isFirstBet = Constants.no;
    @ApiModelProperty("常用设备ID")
    private String deviceId;
    @ApiModelProperty("总代ID")
    private Long firstProxy;
    @ApiModelProperty("区域代理ID")
    private Long secondProxy;
    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;
    /**
     * admin创建，前台创建 公司会员
     * 代理创建 渠道会员
     * 代理链接注册 渠道会员
     * 人人代链接 直属上级是什么注册的就是什么
     */
    @ApiModelProperty("会员类型:0、公司会员，1、渠道会员，2、官方推广")
    private Integer type;
    @ApiModelProperty("注册域名")
    private String registerDomainName;


    @JsonIgnore
    @Transient
    private String token;
    //校验用户帐号权限
    public static boolean checkUser(User user) {
        if (user == null) {
            return false;
        }

        if (!Constants.open.equals(user.getState())) {
            return false;
        }
        return true;
    }

    /**
     * 校验用户信息。长度3-15位
     *
     * @return
     */
    public static boolean checkLength(String... strs) {
        if (ObjectUtils.isEmpty(strs)) {
            return false;
        }
        for (String str : strs) {
            if (ObjectUtils.isEmpty(strs)) {
                return false;
            }

            int length = str.length();
            if (length < 3 || length > 15) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkAccountLength(String account) {
        if (account == null) {
            return false;
        }
        if (!account.matches(RegexEnum.ACCOUNT.getRegex())) {
            return false;
        }
        return true;
    }

    public static boolean checkPasswordLength(String password) {
        if (password == null) {
            return false;
        }
        if (!password.matches(RegexEnum.ACCOUNT.getRegex())) {
            return false;
        }
        return true;
    }

    public static boolean checkPhone(String phone){
        if(ObjectUtils.isEmpty(phone)){
            return false;
        }
        if (!phone.matches(RegexEnum.PHONE.getRegex())) {
            return false;
        }
        return true;
    }

    public static User setBaseUser(String account,String password,String phone,String ip,String inviteCode){
        User user = new User();
        user.setAccount(account);
        user.setPassword(password);
        user.setPhone(phone);
        user.setState(Constants.open);
        user.setRegisterIp(ip);
        user.setInviteCode(inviteCode);
        return user;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("admin");
        authorities.add(authority);
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return getAccount();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
