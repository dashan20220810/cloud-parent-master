package com.qianyi.modulecommon;

/**
 * 常量
 */
public class Constants {

    //是/否，开/关, 真/假
    public final static Integer open = 1;
    public final static Integer close = 0;
    public final static Integer yes = 1;
    public final static Integer no = 0;

    //订单状态
    public final static Integer order_wait = 1;//等待确认
    public final static Integer order_success = 2;//确认成功
    public final static Integer order_fail = 3;//确认失败

    //充值类型
    public final static Integer remitType_bank = 1;//银行卡充值1
    public final static Integer remitType_general = 2;//总控上分2
    public final static Integer remitType_proxy = 3;//代理上分3
    public final static Integer remitType_activity = 4;//活动赠送4

    //打吗账变类型
    public final static Integer CODENUMCHANGE_BET = 0;//有效投注 0
    public final static Integer CODENUMCHANGE_CLEAR = 1;//清0点 1
    public final static Integer CODENUMCHANGE_CHARGE = 2;//充值2
    public final static Integer CODENUMCHANGE_MASTERCONTROL = 3;//总控上分3
    public final static Integer CODENUMCHANGE_PROXY = 4;//代理上分4
    public final static Integer WITHDRAWORDER_MASTERCONTROL = 5;//总控下分
    public final static Integer WITHDRAWORDER_PROXY = 6;//代理下分

    //充值订单状态
    public final static Integer chargeOrder_wait = 0;//原始状态，未接单
    public final static Integer chargeOrder_success = 1;//确认成功
    public final static Integer chargeOrder_fail = 2;//确认失败
    public final static Integer chargeOrder_disabled = 3;//失效
    public final static Integer chargeOrder_masterControl = 4;//总控上分
    public final static Integer chargeOrder_proxy = 5;//代理上分
    //提现订单状态 1：通过，2：拒绝，3：冻结
    public final static Integer withdrawOrder_wait = 0;//原始状态，未接单
    public final static Integer withdrawOrder_success = 1;//确认成功
    public final static Integer withdrawOrder_fail = 2;//审核拒绝
    public final static Integer withdrawOrder_freeze = 3;//冻结
    public final static Integer withdrawOrder_masterControl = 4;//总控下分
    public final static Integer withdrawOrder_proxy = 5;//代理下分
    public final static Integer review_the_order = 6;//审核接单
    public final static Integer pass_the_audit = 7;//审核通过
    public final static Integer paragraph_to_refuse = 8;//出款拒绝
    /** 单个用户银行卡最大绑定数量 */
    public final static Integer BANK_USER_BOUND_MAX = 6;


    //会员状态
    public final static Integer USER_NORMAL = 1; //正常
    public final static Integer USER_LOCK_ACCOUNT = 2; //冻结账户
    public final static Integer USER_LOCK_BALANCE = 3; //冻结资金

    public final static Integer USER_LANGUAGE_CH = 1; //默认中文


    public final static Integer MIN_PASSWORD_NUM = 6; //密码最小位数
    public final static Integer MAX_PASSWORD_NUM = 12; //密码最大位数

    public final static Integer BANK_CLOSE = 1; //银行卡禁用
    public final static Integer BANK_OPEN = 0; //银行卡启用

    public final static Integer MAX_BANK_NUM = 6; //绑定银行卡最多数

    public final static Integer WITHDRAW_PASS = 1;
    public final static Integer WITHDRAW_REFUSE = 2;
    public final static Integer WITHDRAW_ORDER = 3;

    public final static Integer creditCard = 2;

    public final static Integer IPBLACK_CLOSE = 1; //ip黑名单禁用
    public final static Integer IPBLACK_OPEN = 0; //ip黑名单启用

    public final static String CASINO_WEB = "casino-web";
    public final static String PAY_ADMIN = "pay-admin";
    public final static String CASINO_ADMIN = "casino-admin";
    public final static String CASINO_PROXY = "casino-proxy";

    public final static String TOTAL_WASHCODE = "totalWashCode";//redis洗码总计key
    public final static String PLATFORM_WM = "wm";
    public final static String PLATFORM_WM_BIG = "WM";
    public final static String PLATFORM_PG = "PG";
    public final static String PLATFORM_CQ9 = "CQ9";
    public final static String PLATFORM_PG_CQ9 = "PG/CQ9";
    public final static String PLATFORM_OB = "OB";
    public final static String PLATFORM_OBDJ = "OBDJ";
    public final static String PLATFORM_OBTY = "OBTY";
    public final static String PLATFORM_SABASPORT = "SABASPORT";

    public final static String[] PLATFORM_ARRAY = {PLATFORM_WM, PLATFORM_PG, PLATFORM_CQ9, PLATFORM_OBDJ, PLATFORM_OBTY, PLATFORM_SABASPORT};
    //校验数字
    public static final String regex = "^[0-9]*$";
    //校验手机号
    public static final String regexPhone= "^[0-9 ()+-]{6,15}+$";
    //推广类型,人人代
    public static final String INVITE_TYPE_EVERYONE= "everyone";
    //推广类型,基层代理
    public static final String INVITE_TYPE_PROXY= "proxy";
    //推广类型,官方
    public static final String INVITE_TYPE_COMPANY= "888";
    //redis用户key前缀
    public static final String REDIS_USERID= "userId::";
    public static final String REDIS_GAMECODE= "gameCode::";
    //redis每日短信发送数量前缀
    public static final String REDIS_SMSIPSENDNUM= "sms::ipSendNum::";
    //redis每日短信前缀
    public static final String REDIS_SMSCODE = "sms::code::";
    //redis ip单位时间请求次数限制前缀
    public static final String REDIS_IPLIMIT = "ipLimit::";
    //三方总余额redis缓存可以
    public static final String REDIS_THRID_SUMBALANCE = "thridBalance::";
    //redis批量邀请码
    public static final String REDIS_INVITECODELIST= "inviteCodeList";
    //redis推广贷补充数据标识
    public static final String REDIS_SUPPLEMENTARYDATA= "supplementaryData";
    //会员类型：公司会员
    public static final Integer USER_TYPE0= 0;
    //会员类型：渠道会员
    public static final Integer USER_TYPE1= 1;
    //会员类型：官方推广会员
    public static final Integer USER_TYPE2= 2;
    //ip被封返回前端提示语
    public static final String IP_BLOCK= "IP被封，请联系客服";
    //redis casino-web模块token前缀
    public static final String TOKEN_CASINO_WEB = "token::casino-web::";
    public static final String REDIS_TOKEN_ADMIN= "token::casino-admin::";
    public static final String REDIS_TOKEN_PROXY= "token::casino-proxy::";
    //redis token前缀
    public static final String AUTHORIZATION= "authorization";
    //header 中多语言key
    public static final String LANGUAGE= "language";
    //web端JWT过期后，24H内可颁发新的token
    public static final Long WEB_REFRESH_TTL = 60 * 60L * 24;//秒
    public static final Long ADMIN_REFRESH_TTL = 60 * 60L * 24;//秒
    public static final Long PROXY_REFRESH_TTL = 60 * 60L * 24;//秒
    public static final Long THIRD_BALANCE_TTL = 15 * 60L;//15分钟

    public static final String LANGUAGE_CN= "zh-cn"; //中文
    public static final String LANGUAGE_EH= "eh"; //泰语
    public static final String LANGUAGE_EN= "en"; //英语

    public final static Integer OVERALL_TYPE = 0;//全局
    public final static Integer USER_TYPE = 1;//会员
    public final static Integer PROXY_TYPE = 2;//代理
}
