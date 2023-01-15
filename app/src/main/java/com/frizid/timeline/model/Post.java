package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public String text, type,url;
    ParseFile meme, vine;
    public ParseUser userObj;
    public Group groupObj;
    public boolean isDeleted;
    public Post() {
    }

    public ParseUser getUserObj() {
        return getParseUser("userObj");
    }

    public void setUserObj(ParseUser userObj) {
        put("userObj", userObj);
    }

    public Group getGroupObj() {
        return (Group) getParseObject("groupObj");
    }

    public void setGroupObj(Group groupObj) {
        put("groupObj", groupObj);
    }

    public String getText() {
        return getString("text");
    }

    public void setText(String text) {
        put("text", text);
    }

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public String getUrl() {
        return getString("url");
    }

    public void setUrl(String url) {
        put("url", url);
    }

    public ParseFile getMeme() {
        return getParseFile("meme");
    }

    public void setMeme(ParseFile meme) {
        put("meme", meme);
    }

    public ParseFile getVine() {
        return getParseFile("vine");
    }

    public void setVine(ParseFile vine) {
        put("vine", vine);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}

