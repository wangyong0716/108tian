package com.ksider.mobile.android.model;

/**
 * Created by yong on 2015/5/26.
 */
public class CouponModel {
    private long couponId;
    private String couponName;
    private long userId;
    private int status;
    private long validTime;
    private long createTime;
    private double worth;
    private boolean canSum;
    private long productId;
    private long merchantId;
    private byte poiType;
    private long consumeTime;
    private long serialNumber;
    private double feeConstraint;
    private String note;

    public double getFeeConstraint() {
        return feeConstraint;
    }

    public void setFeeConstraint(double feeConstraint) {
        this.feeConstraint = feeConstraint;
    }

    public void setCouponId(long couponId) {
        this.couponId = couponId;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setWorth(double worth) {
        this.worth = worth;
    }

    public void setCanSum(boolean canSum) {
        this.canSum = canSum;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public void setPoiType(byte poiType) {
        this.poiType = poiType;
    }

    public void setConsumeTime(long consumeTime) {
        this.consumeTime = consumeTime;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCouponId() {

        return couponId;
    }

    public String getCouponName() {
        return couponName;
    }

    public long getUserId() {
        return userId;
    }

    public int getStatus() {
        return status;
    }

    public long getValidTime() {
        return validTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public double getWorth() {
        return worth;
    }

    public boolean isCanSum() {
        return canSum;
    }

    public long getProductId() {
        return productId;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public byte getPoiType() {
        return poiType;
    }

    public long getConsumeTime() {
        return consumeTime;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public String getNote() {
        return note;
    }

    public static CouponComparator getComparator(int sortType) {
        return new CouponComparator(sortType);
    }
}

class CouponComparator extends BaseComparator {
    public CouponComparator() {
        super();
    }

    public CouponComparator(int sortType) {
        super(sortType);
    }

    @Override
    public int compare(Object object1, Object object2) {
        CouponModel coupon1 = (CouponModel) object1;
        CouponModel coupon2 = (CouponModel) object2;
        if (sortType == MULTI_SORT) {
            return coupon1.getWorth() > coupon2.getWorth() ? -1 : (coupon1.getWorth() == coupon2.getWorth() ? (coupon1.getFeeConstraint() > coupon2.getFeeConstraint() ? -1 : (coupon1.getFeeConstraint() == coupon2.getFeeConstraint() ? (coupon1.getValidTime() > coupon2.getValidTime() ? 1 : (coupon1.getValidTime() == coupon2.getValidTime() ? 0 : -1)) : 1)) : 1);
        } else if (sortType == ASC_SORT) {
            return coupon1.getWorth() > coupon2.getWorth() ? 1 : (coupon1.getWorth() == coupon2.getWorth() ? (coupon1.getFeeConstraint() > coupon2.getFeeConstraint() ? 1 : (coupon1.getFeeConstraint() == coupon2.getFeeConstraint() ? (coupon1.getValidTime() > coupon2.getValidTime() ? 1 : (coupon1.getValidTime() == coupon2.getValidTime() ? 0 : -1)) : -1)) : -1);
        } else {
            return coupon1.getWorth() > coupon2.getWorth() ? -1 : (coupon1.getWorth() == coupon2.getWorth() ? (coupon1.getFeeConstraint() > coupon2.getFeeConstraint() ? -1 : (coupon1.getFeeConstraint() == coupon2.getFeeConstraint() ? (coupon1.getValidTime() > coupon2.getValidTime() ? -1 : (coupon1.getValidTime() == coupon2.getValidTime() ? 0 : 1)) : 1)) : 1);
        }
    }
}
