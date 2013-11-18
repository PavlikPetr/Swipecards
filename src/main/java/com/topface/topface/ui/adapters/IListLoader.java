package com.topface.topface.ui.adapters;

public interface IListLoader {
    static enum ItemType {LOADER, RETRY, WAITING, REPEAT, NONE}

    public boolean isLoader();

    public boolean isRetrier();

}
