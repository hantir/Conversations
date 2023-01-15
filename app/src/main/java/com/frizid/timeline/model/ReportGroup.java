package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ReportGroup")
public class ReportGroup extends ParseObject {

    public Group groupObj;
    public ParseUser userObj;
    public boolean isDeleted;

    public ReportGroup() {
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

    public Group getGroupObj() {
        return (Group) getParseObject("groupObj");
    }

    public void setGroupObj(Group groupObj) {
        put("groupObj", groupObj);
    }

}
