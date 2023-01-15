package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Reel")
public class Reel extends ParseObject {

    public String text,comment,privacy;
    public ParseUser userObj;
    public ParseFile video;
    public boolean isDeleted;
    public Reel() {
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

    public String getText() {
        return getString("text");
    }

    public void setText(String text) {
        put("text", text);
    }

    public String getComment() {
        return getString("comment");
    }

    public void setComment(String comment) {
        put("comment", comment);
    }

    public ParseFile getVideo() {
        return getParseFile("video");
    }

    public void setVideo(ParseFile video) {
        put("video", video);
    }

    public String getPrivacy() {
        return getString("privacy");
    }

    public void setPrivacy(String privacy) {
        put("privacy", privacy);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}
