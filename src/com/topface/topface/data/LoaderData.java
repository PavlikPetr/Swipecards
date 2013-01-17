package com.topface.topface.data;

import com.topface.topface.ui.adapters.IListLoader;

public class LoaderData extends AbstractData implements IListLoader {

    //Loader indicators
    private boolean mIsListLoader = false;
    private boolean mIsListLoaderRetry = false;

    public LoaderData(IListLoader.ItemType type) {
         setLoaderTypeFlags(type);
    }

    public void setLoaderTypeFlags(ItemType type) {
        switch (type) {
            case LOADER:
                mIsListLoader = true;
                break;
            case RETRY:
                mIsListLoaderRetry = true;
                break;
            case NONE:
            default:
                mIsListLoader = false;
                mIsListLoaderRetry = false;
                break;
        }
    }

    @Override
    public boolean isLoader() {
        return mIsListLoader;
    }

    @Override
    public boolean isLoaderRetry() {
        return mIsListLoaderRetry;
    }
}
