package com.frizid.timeline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Ads")
public class Ads extends ParseObject {

    public String type;

    public Ads() {
    }

    public void setType(String type) {
        put("type", type);
    }

    public void getType(String type) {
        getString("type");
    }
}
