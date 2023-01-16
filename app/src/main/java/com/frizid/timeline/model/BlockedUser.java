package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("BlockedUsers")
public class BlockedUser extends ParseObject {

    public ParseUser userObj, bUserObj;
    public boolean isDeleted;

    public BlockedUser() {
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
}

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

    public ParseUser getBUserObj() {
        return getParseUser("bUserObj");
    }

    public void setBUserObj(ParseUser bUserObj) {
        this.bUserObj = bUserObj;
    }

}
