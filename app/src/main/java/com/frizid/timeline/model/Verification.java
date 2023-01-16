package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Verification")
public class Verification extends ParseObject {

    public String name, username, link, known;
    public ParseUser userObj;
    public boolean isDeleted;
    public Verification() {
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public String getUsername() {
        return getString("username");
    }

    public void setUsername(String username) {
        put("username", username);
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

    public String getLink() {
        return getString("link");
    }

    public void setLink(String link) {
        put("link", link);
    }

    public String getKnown() {
        return getString("known");
    }

    public void setKnown(String known) {
        put("known", known);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}
