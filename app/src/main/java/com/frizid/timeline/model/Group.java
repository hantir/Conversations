package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Group")
public class Group extends ParseObject {

    public String gName, gGroupname, gBio, gLink, gPrivacy;
    public ParseFile gIcon, gCover;
    public boolean warn;
    public boolean isDeleted;

    public Group() {
    }

    public String getGName() {
        return getString("gName");
    }

    public void setGName(String gName) {
        put("gName", gName);
    }
    
    public String getGBio() {
        return getString("gBio");
    }

    public void setGBio(String gBio) {
        put("gBio", gBio);
    }

    public String getGPrivacy() {
        return getString("gPrivacy");
    }

    public void setGPrivacy(String gPrivacy) {
        put("gPrivacy", gPrivacy);
    }

    public String getGLink() {
        return getString("gLink");
    }

    public void setGLink(String gLink) {
        put("gLink", gLink);
    }

    public String getGUsername() {
        return getString("gGroupname");
    }

    public void setGroupname(String gGroupname) {
        put("gGroupname", gGroupname);
    }

    public ParseFile getGIcon() {
        return getParseFile("gIcon");
    }

    public void setGIcon(ParseFile gIcon) {
        put("gIcon", gIcon);
    }

    public ParseFile getGCover() {
        return getParseFile("gCover");
    }
    
    public void setGCover(ParseFile gCover) {
        put("gCover", gCover);
    }

    public boolean getWarn() {
        return getBoolean("warn");
    }

    public void setWarn(boolean warn) {
        put("warn", warn);
    }

    public boolean getIsDeleted() {
        return getBoolean("isDeleted");
    }

    public void setIsDeleted(boolean isDeleted) {
        put("isDeleted", isDeleted);
    }
}