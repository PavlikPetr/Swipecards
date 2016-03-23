package com.topface.topface.ui.adapters;

public interface IListLoader {
    enum ItemType {LOADER, RETRY, WAITING, REPEAT, NONE, TEMP_MESSAGE}

    boolean isLoader();

    boolean isRetrier();

}
