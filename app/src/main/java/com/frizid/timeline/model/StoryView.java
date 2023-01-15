package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("StoryView")
public class StoryView extends ParseObject {

    public ParseUser userObj;
    public Story storyObj;
    public boolean isDeleted;

    public StoryView() {
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

    public Story getStoryObj() {
        return (Story) getParseObject("storyObj");
    }

    public void setStoryObj(Story storyObj) {
        put("storyObj", storyObj);
    }
}
