package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@SuppressWarnings("ALL")
@ParseClassName("CommentReply")
public class CommentReply extends ParseObject {

    public String comment;
    public Comment commentObj;
    public ParseUser userObj;
    public boolean isDeleted;

    public CommentReply() {
    }

    public Comment getCommentObj() {
        return (Comment) getParseObject("commentObj");
    }

    public void setCommentObj(Comment commentObj) {
        put("commentObj", commentObj);
    }

    public String getComment() {
        return getString("comment");
    }

    public void setComment(String comment) {
        put("comment", comment);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean report) {
        put("isDeleted", isDeleted);
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

}
