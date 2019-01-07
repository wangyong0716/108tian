package com.ksider.mobile.android.model;

/**
 * Created by yong on 2015/6/24.
 */
public class ReplyModel {
    private String originUserId;
    private String poiId;
    private long createTime;
    private String threadId;
    private int thumbsUp;
    private String parent;
    private String avatar;
    private boolean deleted;
    private String content;
    private String poiType;
    private String id;
    private String userId;
    private String userName;
    private String role;
    private boolean isRead;
    private String parentName;
    private ReplyModel origin;

    public void setOrigin(ReplyModel origin) {
        this.origin = origin;
    }

    public ReplyModel getOrigin() {

        return origin;
    }

    public void setOriginUserId(String originUserId) {
        this.originUserId = originUserId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setThumbsUp(int thumbsUp) {
        this.thumbsUp = thumbsUp;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getOriginUserId() {

        return originUserId;
    }

    public String getPoiId() {
        return poiId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getThreadId() {
        return threadId;
    }

    public int getThumbsUp() {
        return thumbsUp;
    }

    public String getParent() {
        return parent;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getContent() {
        return content;
    }

    public String getPoiType() {
        return poiType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getParentName() {
        return parentName;
    }
}
