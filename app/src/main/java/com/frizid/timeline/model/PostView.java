package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("PostView")
public class PostView extends ParseObject {

    public ParseUser userObj;
    public Post postObj;
    boolean isDeleted;

    public PostView() {
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

    public Post getPostObj() {
        return (Post) getParseObject("postObj");
    }

    public void setPostObj(Post postObj) {
        put("postObj", postObj);
    }

}
