package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ReelLike")
public class ReelLike extends ParseObject {

    public Reel reelObj;
    public ParseUser userObj;
    public boolean isDeleted;

    public ReelLike() {
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
