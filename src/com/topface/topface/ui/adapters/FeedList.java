package com.topface.topface.ui.adapters;

import java.util.ArrayList;
import java.util.Collection;


@SuppressWarnings("serial")
public class FeedList<T> extends ArrayList<T> {
    public boolean hasItem(int id) {
        return size() > id && id >= 0;
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

    public void addAllFirst(Collection<T> item) {
        this.addAll(0, item);
    }

    public void removeFirst() {
        if (!this.isEmpty()) {
            this.remove(0);
        }
    }

    public void removeLast() {
        if (!this.isEmpty()) {
            this.remove(size()-1);
        }
    }
}
