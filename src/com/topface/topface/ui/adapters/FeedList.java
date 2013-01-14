package com.topface.topface.ui.adapters;

import java.util.ArrayList;


@SuppressWarnings("serial")
public class FeedList<T> extends ArrayList<T> {
    public boolean hasItem(int id) {
        return size() > id;
    }

    public T getLast() {
        if (!this.isEmpty()) return this.get(size()-1);
        else return null;
    }

    public T getFirst() {
        if (!this.isEmpty()) return this.get(0);
        else return null;
    }

    public void addFirst(T item) {
        this.add(0,item);
    }

    public void removeLast() {
        if (!this.isEmpty()) {
            this.remove(size()-1);
        }
    }
}
