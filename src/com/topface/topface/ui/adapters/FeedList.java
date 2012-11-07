package com.topface.topface.ui.adapters;

import java.util.LinkedList;


public class FeedList<T> extends LinkedList<T> {
    public boolean hasItem(int id) {
        return size() > id;
    }

    public T removeWithReindex(int position) {
        T removedItem = remove(position);
        T[] array = (T[]) this.toArray();
        this.clear();
        for(int i=0; i<array.length; i++) {
            if(array[i]!=null)
                addLast(array[i]);
        }
        return removedItem;
    }

}
