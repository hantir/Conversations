package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("CommentReaction")
public class CommentReaction extends ParseObject {

    public Comment commentObj;
    public ParseUser userObj;
    public boolean isDeleted;
    public String value;

    public CommentReaction() {
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

    public String getValue() {
        return getString("value");
    }

    public void setValue(String value) {
        put("value", value);
    }

    public Comment getCommentObj() {
        return (Comment) getParseObject("commentObj");
    }

    public void setCommentObj(Comment commentObj) {
        put("commentObj", commentObj);
    }

}
