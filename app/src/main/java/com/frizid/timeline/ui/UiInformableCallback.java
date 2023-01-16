package com.frizid.timeline.ui;

public interface UiInformableCallback<T> extends UiCallback<T> {
    void inform(String text);
}
