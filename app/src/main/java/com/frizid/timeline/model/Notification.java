package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Notification")
public class Notification extends ParseObject {
    public String notification, type;
    public Post postObj;
    public Reel reelObj;
    public ParseUser pUserObj;
    public ParseUser sUserObj;
    public boolean isDeleted;
    
    public Notification() {
    }

    public Post getPostObj() {
        return (Post) getParseObject("postObj");
    }

    public void setPostObj(Post postObj) {
        put("postObj", postObj);
    }

    public Reel getReelObj() {
        return (Reel) getParseObject("reelObj");
    }

    public void setReelObj(Reel postObj) {
        put("reelObj", postObj);
    }

    public ParseUser getPUserObj() {
        return getParseUser("pUserObj");
    }

    public void setPUserObj(ParseUser pUserObj) {
        put("pUserObj", pUserObj);
    }

    public String getNotification() {
        return getString("notification");
    }

    public void setNotification(String notification) {
        put("notification",notification);
    }

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public ParseUser getSUserObj() {
        return getParseUser("sUserObj");
    }

    public void setSUserObj(ParseUser sUserObj) {
        put("sUserObj", sUserObj);
    }
    
    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}
