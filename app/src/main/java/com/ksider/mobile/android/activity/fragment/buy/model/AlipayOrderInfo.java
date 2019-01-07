package com.ksider.mobile.android.activity.fragment.buy.model;

import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.DeviceUuid;
import com.ksider.mobile.android.utils.SignUtils;
import com.ksider.mobile.android.utils.StringUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by wenkui on 5/7/15.
 */
public class AlipayOrderInfo {
    protected  String mPartner="2088611853232559";  /**支付宝客户id**/
    protected  String mSellerId="payment@108tian.com"; /**卖家支付宝账号**/
    protected  String mNotifyUrl;   /**支付宝客户端回调url**/
    protected  String mOutTradeNo;  /**交易流水号**/
    protected  String mSubject;     /**商品名称,交易产品名称**/
    protected  String mTotalFee;    /**付款金额**/
    protected static final String RSA_PRIVATE ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANCnLdiBoP5p6iy9" +
                    "wg4rwVy/eC6JhpKZm5gkxgcryKdf/HZtDIZiQoErsMEHksXIW6iXuhyV8xWMbmvG" +
                    "qnw8AUALEmXlMoFsRGL5aBdH45EAk5G1m7UKdMOOliLtDvOh642xVwKS/swA5Shc" +
                    "ZWWo9Rz/xIjx+cmCkv8MG3ojlXkNAgMBAAECgYAyHLTKFvgPomh3rmt489FQ8Ttw" +
                    "Ed+cPgA8njhhaIacjFUaLD6h19+ZHmEpOyRpFCHPcAFm9CyZtEyg3en8RH0X8W65" +
                    "/LJVxBGCUau1zU+HiJAXo8fUsXkdhFODNRk6kCcuJp6CJwK2vUJSnakjtP/QuJfa" +
                    "lVK7rhKG2i8VQ+Z5EQJBAPH1+TseO3TO52SyVFKgqraaqW4J2dxbuGgY8YHE/sfz" +
                    "X9Lj7PbuH00y5zlne8vyc7GJCrpO5j+fHLR7q+DIoesCQQDcwnW3ecmacF6UKBNa" +
                    "JLMFu8ATiFmhSOCDswC/E1BdSqJbaeYX2tgHzEeDNyQ/+Fq1uqDPCL+6KqWyiu5q" +
                    "bJrnAkATyfpIS4CxXPqv0aXz3BAaPyv8Q/H9g8LhQKj/5AyybcU4ikunJnKI7dDb" +
                    "cQs+8uVjb8Hg2vMZ4PmICVVVXRNhAkAc0hZkObYkP39ZgFHmdtwYZ4aQBkENWWJW" +
                    "T0xNpcvcIh5IKO8tNhj2C6labByDbX0KNK9B2DaPq3mucYzmv/8JAkEA5+3FRJO1" +
                    "T1y2ie7Y8GCK77KZaMc9Dp3elclVT3GRsdwmWrE1+agEAp41la7KpKDYO93c5fx7" +
                    "YRjOVvnQ4QFo5w==" ;
    protected static final String RSA_PUBLIC =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQpy3YgaD+aeosvcIOK8Fcv3gu" +
            "iYaSmZuYJMYHK8inX/x2bQyGYkKBK7DBB5LFyFuol7oclfMVjG5rxqp8PAFACxJl" +
            "5TKBbERi+WgXR+ORAJORtZu1CnTDjpYi7Q7zoeuNsVcCkv7MAOUoXGVlqPUc/8SI" +
            "8fnJgpL/DBt6I5V5DQIDAQAB";
    public AlipayOrderInfo(String notifyUrl, String outTradeNo, String subject, String totalFee){
        mNotifyUrl = notifyUrl;
        mOutTradeNo = outTradeNo;
        mSubject = subject;
        mTotalFee = totalFee;
    }
    public String getOrderInfo() throws UnsupportedEncodingException {
        String content ="service=\"mobile.securitypay.pay\""
                        +"&partner=\""+mPartner+"\""
                        +"&_input_charset=\"utf-8\""
                        +"&notify_url=\""+mNotifyUrl+"\""
                        +"&app_id=\""+ DeviceUuid.getUuid()+"\""
                        +"&appenv=\"system=android_"+DeviceUuid.getUuid()+"^channel="+DeviceUuid.getChannel()+"\""
                        +"&out_trade_no=\""+mOutTradeNo+"\""
                        +"&body=\""+mSubject+"--售价："+mTotalFee+"\""
                        +"&subject=\""+mSubject+"\""
                        +"&total_fee=\""+mTotalFee+"\""
                        +"&payment_type=\"1\""
                        +"&seller_id=\""+mSellerId+"\"";
        String sign = SignUtils.sign(content, RSA_PRIVATE);
        sign = URLEncoder.encode(sign, "UTF-8");
        return content+"&sign=\""+sign+"\"&sign_type=\"RSA\"";
    }
    public static Boolean veirify(String result){
       String signPrix ="&sign=\"";
       String sign = result.substring(result.indexOf(signPrix) + signPrix.length(),
               result.lastIndexOf("\""));

        int end = result.indexOf(signPrix)>result.indexOf("&sign_type=\"RSA\"")
                ?result.indexOf("&sign_type=\"RSA\""):result.indexOf(signPrix);
        String content = result.substring(0, end);
        return SignUtils.verify(content, sign, RSA_PUBLIC);
    }

}
