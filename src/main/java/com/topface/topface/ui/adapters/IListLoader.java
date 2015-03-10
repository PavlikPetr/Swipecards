package com.topface.topface.ui.adapters;

public interface IListLoader {
    static enum ItemType {LOADER, RETRY, WAITING, REPEAT, NONE, TEMP_MESSAGE}

    public boolean isLoader();

    @SuppressWarnings("UnusedDeclaration")
    public boolean isRetrier();

}
