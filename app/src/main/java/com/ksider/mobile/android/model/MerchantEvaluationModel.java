package com.ksider.mobile.android.model;

import java.util.ArrayList;

/**
 * Created by yong on 11/7/15.
 */
public class MerchantEvaluationModel {
    private String id;
    private String avatar;
    private ArrayList<EvaluationModel> evaluations;
    private long createTime;
    private int merchantId;
    private String poiId;
    private String poiType;
    private int productId;
    private double score;
    private long serialNumber;
    private String uid;
    private String userName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ArrayList<EvaluationModel> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(ArrayList<EvaluationModel> evaluations) {
        this.evaluations = evaluations;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public String getPoiType() {
        return poiType;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void addEvaluation(EvaluationModel model) {
        if (evaluations == null) {
            evaluations = new ArrayList<EvaluationModel>();
        }
        evaluations.add(model);
    }
}
