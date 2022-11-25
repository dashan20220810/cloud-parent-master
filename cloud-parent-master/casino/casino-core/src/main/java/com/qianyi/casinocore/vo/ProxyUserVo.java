package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.ProxyUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ProxyUserVo  implements Serializable {
    private static final long serialVersionUID = -6875619845650305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "账号")
    private String userName;
    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "密码")
    private String passWord;
    @ApiModelProperty(value = "手机号码")
    private String phone;
    @ApiModelProperty(value = "谷歌验证码KEY")
    private String gaKey;
    @ApiModelProperty(value = "谷歌验证码是否绑定1=未绑定，2=已绑定")
    private String gaBind;
    @ApiModelProperty(value = "代理邀请码")
    private String proxyCode;
    @ApiModelProperty(value = "代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "区域代理ID")
    private Long secondProxy;
    @ApiModelProperty(value = "总代理ID")
    private Long firstProxy;
    @ApiModelProperty(value = "上级代理账号")
    private String superiorProxyAccount;
    @ApiModelProperty(value = "所属总代")
    private String firstProxyAccount;
    @ApiModelProperty(value = "佣金分成比")
    private String commissionRatio;
    @ApiModelProperty(value = "直属玩家数")
    private Integer usersNum;
    @ApiModelProperty(value = "下级代理数")
    private Integer proxyUsersNum;
    @ApiModelProperty(value = "是否锁定 1：正常 2：锁定")
    private Integer userFlag;
    @ApiModelProperty("是否删除 1：正常 2：删除")
    private Integer isDelete;
    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("最后修改人")
    private String updateBy;
    public ProxyUserVo(){

    }
    public ProxyUserVo(ProxyUser proxyUser){
        this.id = proxyUser.getId();
        this.userName = proxyUser.getUserName();
        this.nickName = proxyUser.getNickName();
        this.phone = proxyUser.getPhone();
        this.proxyRole = proxyUser.getProxyRole();
        this.isDelete = proxyUser.getIsDelete();
        this.secondProxy = proxyUser.getSecondProxy();
        this.firstProxy = proxyUser.getFirstProxy();
        this.userFlag = proxyUser.getUserFlag();
        this.isDelete = proxyUser.getIsDelete();
        this.proxyUsersNum = proxyUser.getProxyUsersNum();
        this.createTime = proxyUser.getCreateTime();
        this.createBy = proxyUser.getCreateBy();
        this.updateTime = proxyUser.getUpdateTime();
        this.updateBy = proxyUser.getUpdateBy();
    }
}
