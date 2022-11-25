package com.qianyi.casinoadmin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SysUserVo implements Serializable {

    private static final long serialVersionUID = -3005417929250305179L;

    private Long id;

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

    private Long sysRoleId;

    private String roleName;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String createBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    private String updateBy;

    private List<SysPermissionVo> sysPermissionVoList;
}
