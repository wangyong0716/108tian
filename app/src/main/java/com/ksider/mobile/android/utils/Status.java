package com.ksider.mobile.android.utils;

/**
 * Created by yong on 2015/6/3.
 */
public class Status {
    /**
     * order status
     * 1<-初始化，待支付
     * 2<-֧支付完成
     * 3<-申请退款
     * 4<-退款审核成功，退款中
     * 5<-退款审核失败
     * 6<-退款成功，已回款
     * 7<-订单取消
     * 8<-订单过期
     * 9<-部分退款审核中
     * 10<-部分退款成功
     * 11<-部分退款审核失败
     * 12<-部分退款成功,已回款
     * 13<-完成验证
     */
    public static final byte ORDER_INIT = (byte) 1;
    public static final byte ORDER_PAYMENT_DONE = (byte) 2;
    public static final byte ORDER_REFOUND_REQUIRED = (byte) 3;
    public static final byte ORDER_REFOUND_APPROVED = (byte) 4;
    public static final byte ORDER_REFOUND_REJECTED = (byte) 5;
    public static final byte ORDER_REFOUND_DONE = (byte) 6;
    public static final byte ORDER_CANCELED = (byte) 7;
    public static final byte ORDER_EXPIRED = (byte) 8;
    public static final byte ORDER_PARTIALLY_REFUND_REQUIRED = (byte) 9;
    public static final byte ORDER_PARTIALLY_REFUND_APPROVED = (byte) 10;
    public static final byte ORDER_PARTIALLY_REFUND_REJECTED = (byte) 11;
    public static final byte ORDER_PARTIALLY_REFUND_DONE = (byte) 12;
    public static final byte ORDER_CONSUMED = (byte) 13;

    /**
     * code status
     * 1<-未消费
     * 2<-已消费
     * 3<-申请退款
     * 4<-同意退款
     * 5<-退款驳回
     * 6<-已完成退款
     * 7<-已向商家回款
     * 8<-代销中
     * 9<-已向代售方收款
     */
    public static final byte CODE_UNCONSUMED = (byte) 1;
    public static final byte CODE_CONSUMED = (byte) 2;
    public static final byte CODE_REFOUND_REQUIRED = (byte) 3;
    public static final byte CODE_REFOUND_APPROVED = (byte) 4;
    public static final byte CODE_REFOUND_REJECTED = (byte) 5;
    public static final byte CODE_REFOUND_DONE = (byte) 6;
    public static final byte CODE_SETTLED = (byte) 7;
    public static final byte CODE_CREDIT = (byte) 8;
    public static final byte CODE_PAYMENT_COLLECTED = (byte) 9;

    /**
     * coupon status
     * 1<-正常
     * 2<-已使用
     * 3<-注销
     */
    public static final byte COUPON_NORMAL = (byte) 1;
    public static final byte COUPON_CONSUMED = (byte) 2;
    public static final byte COUPON_CANCEL = (byte) 3;

    /**
     * product type
     * 1<-需指定消费时间
     * 2<-不指定消费时间
     */
    public static final int PRODUCT_TYPE_TIME = 1;
    public static final int PRODUCT_TYPE_ALWAYS = 2;
}
