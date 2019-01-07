package com.ksider.mobile.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yong on 2015/5/22.
 */
public class OrderModel implements Serializable {
    private long createTime;
    private double totalFee;
    private double sellPrice;
    private int status;
    private long userId;
    private long serialNumber;
    private long quantity;
    private String productName;
    private long productId;
    private long consumeTime;
    private String productImg;
    private double couponDiscount = 0;
    private String coupons = "";
    private long couponId;
    private int refund;
    private int evaluate;

    public void setCouponId(long couponId) {
        this.couponId = couponId;
    }

    public long getCouponId() {

        return couponId;
    }

    public OrderModel() {

    }

    public int getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(int evaluate) {
        this.evaluate = evaluate;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }

    public long getCreateTime() {
        return createTime;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public int getStatus() {
        return status;
    }

    public long getUserId() {
        return userId;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    public long getProductId() {
        return productId;
    }

    public long getConsumeTime() {
        return consumeTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public void setConsumeTime(long consumeTime) {
        this.consumeTime = consumeTime;
    }

    public double getCouponDiscount() {
        return couponDiscount;
    }

    public String getCoupons() {
        return coupons;
    }

    public void setCouponDiscount(double couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public void setCoupons(String coupons) {
        this.coupons = coupons;
    }

    public int getRefund() {
        return refund;
    }

    public void setRefund(int refund) {
        this.refund = refund;
    }

    public void loadJson(JSONObject json) {
        try {
            productName = json.getString("productName");
        } catch (JSONException e) {
            productName = "";
            e.printStackTrace();
        }
        try {
            totalFee = json.getDouble("totalFee");
        } catch (JSONException e) {
            totalFee = 0;
            e.printStackTrace();
        }
        try {
            productId = json.getLong("productId");
        } catch (JSONException e) {
            productId = 0;
            e.printStackTrace();
        }
        try {
            quantity = json.getLong("quantity");
        } catch (JSONException e) {
            quantity = 0;
            e.printStackTrace();
        }
        try {
            consumeTime = json.getLong("consumeTime");
        } catch (JSONException e) {
            consumeTime = 0;
            e.printStackTrace();
        }
        try {
            couponDiscount = json.getDouble("couponDiscount");
        } catch (JSONException e) {
            couponDiscount = 0;
            e.printStackTrace();
        }
        try {
            coupons = json.getString("coupons");
        } catch (JSONException e) {
            coupons = "";
            e.printStackTrace();
        }
        try {
            serialNumber = json.getLong("serialNumber");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            sellPrice = json.getDouble("sellPrice");
        } catch (JSONException js) {
            sellPrice = 0;
            js.printStackTrace();
        }
        try {
            refund = json.getInt("refund");
        } catch (JSONException js) {
            refund = 0;

            js.printStackTrace();
        }
    }
}
