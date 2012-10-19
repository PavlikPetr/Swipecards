package com.topface.topface.ui.adapters;

public interface IListLoader {
    static enum ItemType {LOADER, RETRY, NONE}

    public boolean isLoader();

    public boolean isLoaderRetry();

}
