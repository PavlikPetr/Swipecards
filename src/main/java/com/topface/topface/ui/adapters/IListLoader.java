package com.topface.topface.ui.adapters;

public interface IListLoader {
    static enum ItemType {LOADER, RETRY, WAITING, REPEAT, HACK_ITEM, NONE}

    public boolean isLoader();

    @SuppressWarnings("UnusedDeclaration")
    public boolean isRetrier();

}
