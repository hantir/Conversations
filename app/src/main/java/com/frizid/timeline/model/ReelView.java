package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ReelView")
public class ReelView extends ParseObject {

    public ParseUser userObj;
    public Reel reelObj;
    public boolean isDeleted;

    public ReelView() {
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

    public Reel getReelObj() {
        return (Reel) getParseObject("reelObj");
    }

    public void setReelObj(Reel reelObj) {
        put("reelObj", reelObj);
    }

}
