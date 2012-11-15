package com.topface.topface.ui.adapters;

import java.util.LinkedList;


@SuppressWarnings("serial")
public class FeedList<T> extends LinkedList<T> {
    public boolean hasItem(int id) {
        return size() > id;
    }

}
