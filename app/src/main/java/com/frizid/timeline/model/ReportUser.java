package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ReportUser")
public class ReportUser extends ParseObject {

    public ParseUser userObj, rUserObj;
    public boolean isDeleted;

    public ReportUser() {
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

    public ParseUser getRUserObj() {
        return getParseUser("rUserObj");
    }

    public void setRUserObj(ParseUser rUserObj) {
        put("rUserObj",rUserObj);
    }

}
