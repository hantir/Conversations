package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ReportComment")
public class ReportComment extends ParseObject {

    public Comment commentObj;
    public ParseUser userObj;
    public boolean isDeleted;

    public ReportComment() {
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

    public Comment getCommentObj() {
        return (Comment) getParseObject("commentObj");
    }

    public void setCommentObj(Comment commentObj) {
        put("commentObj", commentObj);
    }

}
