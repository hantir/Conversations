package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@SuppressWarnings("ALL")
@ParseClassName("Story")
public class Story extends ParseObject {

    public ParseFile imageObj;
    public ParseUser userObj;
    public String type;
    public boolean isDeleted;

    public Story() {
    }

    public ParseFile getImageObj() {
        return getParseFile("imageObj");
    }

    public void setImageObj(ParseFile imageObj) {

        put("imageObj", imageObj);
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

    public void setType(String type) {
        put("type", type);
    }

    public String getType() {
        return getString("type");
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}

