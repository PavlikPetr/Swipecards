package com.topface.topface.ui.adapters;

import java.util.LinkedList;


public class FeedList<T> extends LinkedList<T> {
    public boolean hasItem(int id) {
        return size() > id;
    }
}
