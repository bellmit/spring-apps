package com.haozhuo.datag.common;

public class ResultCodeBase {
    public static final int CODE_SUCCESS = 200; // 成功

    public static final int CODE_BAD_REQUEST = 400; //无效请求

    public static final int CODE_EXCEPTION = 500; // 异常

    public static final int CODE_ERROR_USER_NOT_LOGIN = 600; //未登陆

    public static final int CODE_ERROR_USER_SIGN_ERROR = 700; //签名验证失败

    public static final int CODE_ERROR_CURRENT_LIMITING = 800; //访问过于频繁

    public static final int CODE_ERROR_SYSTEM_MAINTENANCE = 900; //停机维护

    public static final int CODE_RED_PACKET_RAIN= 901; //红包雨活动


    public static final int CODE_RED_PACKET_RAIN_IGNORE= 904; //红包雨活动 忽略的url

    /**
     * app版本过低
     */
    public static final int CODE_ERROR_APP_VERSION=1010;

    public static final int CODE_ONS_CONSUME_LATER = 1000; //ons重新消费

    public static final int CODE_DATA_IS_NULL= 501; //数据为空
    // ec 模块错误码  (409 开头)
    public static final int EC_GOODS_FROM_ORDER = 40901; // 商品已下架

}
