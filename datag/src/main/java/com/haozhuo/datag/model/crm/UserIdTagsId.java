package com.haozhuo.datag.model.crm;

import lombok.Setter;
import lombok.Getter;
@Getter
@Setter
public class UserIdTagsId {

    String userId;
    Object tagIds;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object getTagIds() {
        return tagIds;
    }

    public void setTagIds(Object tagIds) {
        this.tagIds = tagIds;
    }
}
