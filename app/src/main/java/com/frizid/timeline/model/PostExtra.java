package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("PostExtra")
public class PostExtra extends ParseObject {

    public String privacy, feeling, location;
    public Post postObj;
    public boolean isDeleted;
    public PostExtra() {
    }

    public Post getPostObj() {
        return (Post) getParseObject("postObj");
    }

    public void setPostObj(Post postObj) {
        put("postObj", postObj);
    }

    public String getFeeling() {
        return getString("feeling");
    }

    public void setFeeling(String feeling) {
        put("feeling", feeling);
    }

    public String getLocation() {
        return getString("location");
    }

    public void setLocation(String location) {
        put("location", location);
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
