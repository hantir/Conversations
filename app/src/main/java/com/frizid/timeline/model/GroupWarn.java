package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("GroupWarn")
public class GroupWarn extends ParseObject {

    public Group groupObj;
    public boolean value;
    public boolean isDeleted;

    public GroupWarn() {
    }

    public Group getGroupObj() {
        return (Group) getParseObject("groupObj");
    }

    public void setGroupObj(Group groupObj) {
        put("groupObj", groupObj);
    }

    public boolean getValue() {
        return getBoolean("value");
    }

    public void setValue(boolean value) {
        put("value", value);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}

