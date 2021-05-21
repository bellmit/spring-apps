package com.haozhuo.datag.model;

/**
 * Created by hcp on 2018/3/30.
 */
public enum ResponseEnum {
    SUCCESS(200, "成功"),
    ERROR(400, "失败"),
    PARAM_ERROR(400, "参数为空"),
/*    SIGN_ERROR(700, "签名验证失败"),
    WX_PAY_ERROR(3602001, "调用微信下单接口异常"),
    FREE_COUNT_OUT(3602002, "所选免费加项包数量超过允许最大数量"),
    CREDIT_COUNT_OUT(3602003, "所选挂帐加项包数量超过允许最大数量"),
    PAYMENT_CONFIRM_ERROR(3602004, "支付确认失败"),
    OUT_ORDER_NO_ERROR(3602005, "预约单号有误"),
    AMOUNT_VALIDATE_ERROR(3602006, "加项包自费总额有误"),
    CREATE_RESERVATION_ERROR(3602007, "请求美年接口保存预约信息失败"),
    OPERATION_TIMWOUT_ERROR(3602008, "请求时间超过规定时间"),
    DUPLICATE_ENTRY_ERROR(3602009, "serviceRecordId已存在"),
    AUTN_CODE_CAN_NOT_EMPTY(3602010, "验证码不能为空"),
    AUTN_CODE_NOT_EXIST(3602011, "验证码过期或不存在"),
    AUTN_CODE_IS_ERROR(3602012, "验证码错误"),*/
    ;
    private int code;

    private String msg;

    ResponseEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
