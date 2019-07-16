package com.haozhuo.datag.common;

public class TipConstBase {
    //基础操作
    public static final String OPERATION_NO_LOGIN= "请先登陆";
    public static final String OPERATION_LOGIN_SUCCESS = "登录成功";
    public static final String OPERATION_LOGIN_ERROR = "登录失败，用户名或密码错误";
    public static final String OPERATION_REGIST_SUCCESS = "注册成功";
    public static final String OPERATION_SIGN_ERROR= "签名验证失败";
    public static final String OPERATION_SYS_SO_BUSY= "系统正忙,请稍后再试";
    public static final String OPERATION_GET_SUCCESS = "获取成功";
    public static final String OPERATION_GET_ERROR = "获取失败";
    public static final String OPERATION_UPDATE_SUCCESS = "更新成功";
    public static final String OPERATION_UPDATE_ERROR = "更新失败";
    public static final String OPERATION_SAVE_SUCCESS = "保存成功";
    public static final String OPERATION_SAVE_ERROR = "保存失败";
    public static final String OPERATION_CANCLE_SUCCESS = "取消成功";
    public static final String OPERATION_DELETE_SUCCESS = "删除成功";
    public static final String OPERATION_DELETE_ERROR = "删除失败";
    public static final String INVALID_PARAMETER = "非法参数";


    public static final String TOKEN_COMPARE_ERROR = "token有误";


    /**
     * 手机号有误
     */
    public static final String PHONENUMBER_VALIDATE_ERROR = "手机号码格式有误，请输入正确的手机号。";

    public static final String SERVICE_ERROR = "系统异常";

    public static final String MN_PACKAGE_SOURCE_TYPE_ERROR = "套餐来源不合法";
    public static final String MN_PACKAGE_SOURCE_TYPE_MODIFY = "美年套餐不允许修改";
    public static final String MN_PACKAGE_SEX_ERROR = "性别类型不合法";

    public static final String CHECK_PLAN_PACKAGE_NOT_EXIST = "定制体检套餐记录不存在";
    public static final String CHECK_PLAN_NOT_EXIST = "体检定制记录缺失";

    public static final String ORDER_NOT_WAIT_PAY = "订单不在待支付状态";

    public static final String ORDERNO_ERROR = "订单信息未找到";

    public static final String NO_MEMBER_ENTERPEISE_INFO = "没有查找到与该成员信息匹配的企业，请重新检查输入信息";

    public static final String GOODS_COUNT_ERROR = "没有足够的商品库存,请重新购买";

    public static final String GOODS_LIMIT_AMOUNT_ERROR = "此商品你已达到最大购买次数限制！无法继续购买！";

    public static final String VIP_EXIST = "您已是VIP，续费请购买续费商品哦";

    public static final String SERVER_GROUP_DIFFERENT= "此商品服务类型与您当前VIP分组不同，无法购买！";

    /**
     * 关闭实名认证接口
     */
    public static final String FUNCTION_IS_TESTING = "功能内测中，敬请期待";

    /**
     * 所有科室编号定义10000
     */
    public static final String ALL_DEPARTMENT_CODE = "10000";

    /**
     * 无效的兑换码
     */
    public static final String INVALID_RED_PACK_REDEEM_CODE = "无效的兑换码";

    /**
     * 套餐编码已添加
     */
    public static final String MN_PACKAGE_IS_EXIST = "套餐编码已添加";
}
