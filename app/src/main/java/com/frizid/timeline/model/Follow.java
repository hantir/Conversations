package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Follow")
public class Follow extends ParseObject {

    public ParseUser fromObj;
    public ParseUser toObj;
    public boolean isDeleted;

    public Follow() {
    }
    
    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
}

    public ParseUser getFromObj() {
        return getParseUser("fromObj");
    }

    public void setFromObj(ParseUser fromObj) {
        put("fromObj", fromObj);
    }

    public ParseUser getToObj() {
        return getParseUser("toObj");
    }

    public void setToObj(ParseUser toObj) {
        put("toObj", toObj);
    }
}
