package com.ksider.mobile.android.model;

/**
 * Created by yong on 2015/5/23.
 */
public class ConsumeCodeModel {
    private double settlePrice;
    private String productName;
    private int sellCount;
    private long validTime;
    private long consumeTime;
    private String code;
    private int status;
    private int count = 0;
    private long serialNumber;

    public ConsumeCodeModel() {

    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getSettlePrice() {
        return settlePrice;
    }

    public String getProductName() {
        return productName;
    }

    public int getSellCount() {
        return sellCount;
    }

    public long getValidTime() {
        return validTime;
    }

    public long getConsumeTime() {
        return consumeTime;
    }

    public void setSettlePrice(double settlePrice) {
        this.settlePrice = settlePrice;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setSellCount(int sellCount) {
        this.sellCount = sellCount;
    }

    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }

    public void setConsumeTime(long consumeTime) {
        this.consumeTime = consumeTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static ConsumeCodeComparator getComparator() {
        return new ConsumeCodeComparator();
    }

    public static ConsumeCodeComparator getComparator(int sortType) {
        return new ConsumeCodeComparator(sortType);
    }
}

class ConsumeCodeComparator extends BaseComparator {
    public ConsumeCodeComparator() {
        super();
    }

    public ConsumeCodeComparator(int sortType) {
        super(sortType);
    }

    @Override
    public int compare(Object object1, Object object2) {
        ConsumeCodeModel code1 = (ConsumeCodeModel) object1;
        ConsumeCodeModel code2 = (ConsumeCodeModel) object2;
        if (sortType == ASC_SORT) {
            return code1.getValidTime() > code2.getValidTime() ? 1 : (code1.getValidTime() == code2.getValidTime() ? 0 : -1);
        } else {
            return code1.getValidTime() > code2.getValidTime() ? -1 : (code1.getValidTime() == code2.getValidTime() ? 0 : 1);
        }
    }
}

