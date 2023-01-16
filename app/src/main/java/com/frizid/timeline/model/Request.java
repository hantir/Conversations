package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Request")
public class Request extends ParseObject {

    public ParseUser userObj;
    public Group groupObj;
    public boolean isDeleted;
    public Request() {
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
    
    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}
