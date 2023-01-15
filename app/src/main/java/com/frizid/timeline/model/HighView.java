package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("HighView")
public class HighView extends ParseObject {

    public ParseUser userObj;
    public Story storyObj;
    public High highObj;
    public boolean isDeleted;

    public HighView() {
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

    public High getHighObj() {
        return (High) getParseObject("highObj");
    }

    public void setHighObj(High highObj) {
        put("highObj", highObj);
    }

    public Story getStoryObj() {
        return (Story) getParseObject("storyObj");
    }

    public void setStoryObj(Story storyObj) {
        put("storyObj", storyObj);
    }
}
