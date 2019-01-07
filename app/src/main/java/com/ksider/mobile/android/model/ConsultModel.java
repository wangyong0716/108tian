package com.ksider.mobile.android.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yong on 2015/6/24.
 */
public class ConsultModel {
    public static final int DEFAULT_PRESHOW_CONSULT_NUM = 2;
    public static final int DEFAULT_PRESHOW_REPLY_NUM = 2;
    //mark the request intent
    public static final int GET_THREAD_COUNT = 1;
    public static final int GET_CONSULT_LIST = 2;
    public static final int ADD_CONSULT = 3;
    public static final int ADD_REPLY = 4;
    public static final int ADD_OPT = 5;
    //mark the consult type
    public static final int PROPOSE_QUESTION = 1;
    public static final int REPLY_QUESTION = 2;

    private String id;
    private String content;
    private long createTime;
    private boolean delete;
    private String parent;
    private String poiId;
    private String poiType;
    private String role;
    private String threadId;
    private int thumbsUp;
    private String userId;
    private String avatar;
    private String userName;
    private Drawable imageDrawable;
    private Bitmap imageBitmap;
    private String parentName;
    private int showNum;
    private boolean opt;


    private List<ConsultModel> replies;

    public boolean isOpt() {
        return opt;
    }

    public void setReplies(List<ConsultModel> replies) {
        this.replies = replies;
    }

    public List<ConsultModel> getReplies() {

        return replies;
    }

    public void setOpt(boolean opt) {

        this.opt = opt;
    }

    public void addReply(ConsultModel reply) {
        this.replies.add(reply);
    }

    public void setImageDrawable(Drawable imageDrawable) {
        this.imageDrawable = imageDrawable;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public void setShowNum(int showNum) {
        this.showNum = showNum;
    }

    public Drawable getImageDrawable() {

        return imageDrawable;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public int getShowNum() {
        return showNum;
    }

    public ConsultModel() {
        replies = new ArrayList<ConsultModel>();
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentName() {

        return parentName;
    }

    public ConsultModel(ConsultModel consult) {
        this.id = consult.getId();
        this.content = consult.getContent();
        this.createTime = consult.getCreateTime();
        this.delete = consult.isDelete();
        this.parent = consult.getParent();
        this.poiId = consult.getPoiId();
        this.poiType = consult.getPoiType();
        this.role = consult.getRole();
        this.threadId = consult.getThreadId();
        this.thumbsUp = consult.getThumbsUp();
        this.userId = consult.getUserId();
        this.avatar = consult.getAvatar();
        this.userName = consult.getUserName();
        this.replies = consult.getReplies();
        this.showNum = consult.getShowNum();
        this.parentName = consult.getParentName();
        this.imageDrawable = consult.getImageDrawable();
        this.imageBitmap = consult.getImageBitmap();
        this.opt = consult.isOpt();
    }

    public void copyValues(ConsultModel consult) {
        this.id = consult.getId();
        this.content = consult.getContent();
        this.createTime = consult.getCreateTime();
        this.delete = consult.isDelete();
        this.parent = consult.getParent();
        this.poiId = consult.getPoiId();
        this.poiType = consult.getPoiType();
        this.role = consult.getRole();
        this.threadId = consult.getThreadId();
        this.thumbsUp = consult.getThumbsUp();
        this.userId = consult.getUserId();
        this.avatar = consult.getAvatar();
        this.userName = consult.getUserName();
        this.showNum = consult.getShowNum();
        this.parentName = consult.getParentName();

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setThumbsUp(int thumbsUp) {
        this.thumbsUp = thumbsUp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {

        return id;
    }

    public String getContent() {
        return content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isDelete() {
        return delete;
    }

    public String getParent() {
        return parent;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getPoiType() {
        return poiType;
    }

    public String getRole() {
        return role;
    }

    public String getThreadId() {
        return threadId;
    }

    public int getThumbsUp() {
        return thumbsUp;
    }

    public String getUserId() {
        return userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUserName() {
        return userName;
    }

    public static ConsultComparator getComparator() {
        return new ConsultComparator();
    }

    public static ConsultComparator getComparator(int sortType) {
        return new ConsultComparator(sortType);
    }
}

class ConsultComparator extends BaseComparator {
    public ConsultComparator() {
        super();
    }

    public ConsultComparator(int sortType) {
        super(sortType);
    }

    @Override
    public int compare(Object object1, Object object2) {
        ConsultModel consult1 = (ConsultModel) object1;
        ConsultModel consult2 = (ConsultModel) object2;
        if (sortType == ASC_SORT) {
            return consult1.getCreateTime() > consult2.getCreateTime() ? 1 : (consult1.getCreateTime() == consult2.getCreateTime() ? 0 : -1);
        } else {
            return consult1.getCreateTime() > consult2.getCreateTime() ? -1 : (consult1.getCreateTime() == consult2.getCreateTime() ? 0 : 1);
        }
    }
}
