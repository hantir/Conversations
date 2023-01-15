package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("High")
public class High extends ParseObject {

    public String type;
    public Story storyObj;
    public ParseUser userObj;
    public ParseFile file;
    public boolean isDeleted;
    public High() {
    }
    

    public ParseFile getFile() {
        return getParseFile("file");
    }

    public void setFile(ParseFile file) {
        put("file", file);
}

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public Story getStoryObj() {
        return (Story) getParseObject("storyObj");
    }

    public void setStoryObj(Story storyObj) {
        put("storyObj", storyObj);
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }
    
    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}
