package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {

    public ParseUser userObj;
    public Post postObj;
    public String comment, type;
    public boolean isDeleted;
    public ParseFile file;
    public Reel reelObj;
    public Comment() {
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

    public String getComment() {
        return getString("comment");
    }

    public void setComment(String comment) {
        put("comment", comment);
    }

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public Post getPostObj() {
        return (Post) getParseObject("postObj");
    }

    public void setPostObj(Post postObj) {
        put("postObj", postObj);
    }

    public ParseFile getFile() {
        return getParseFile("file");
    }

    public void setFile(ParseFile file) {
        put("file", file);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }

    public Reel getReelObj() {
        return (Reel) getParseObject("reelObj");
    }

    public void setReelObj(Reel reelObj) {
        put("reelObj", reelObj);
    }
}
