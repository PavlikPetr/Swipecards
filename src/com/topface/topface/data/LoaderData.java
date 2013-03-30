package com.topface.topface.data;

import com.topface.topface.ui.adapters.IListLoader;
import org.json.JSONObject;

public class LoaderData extends AbstractData implements IListLoader {

    //Loader indicators
    private boolean mIsListLoader = false;
    private boolean mIsListRetrier = false;

    public LoaderData(IListLoader.ItemType type) {
         setLoaderTypeFlags(type);
    }

    public LoaderData() {}

    public LoaderData(JSONObject data) {
        super(data);
    }

    public void setLoaderTypeFlags(ItemType type) {
        switch (type) {
            case LOADER:
                mIsListLoader = true;
                break;
            case RETRY:
                mIsListRetrier = true;
                break;
            case NONE:
            default:
                mIsListLoader = false;
                mIsListRetrier = false;
                break;
        }
    }

    @Override
    public boolean isLoader() {
        return mIsListLoader;
    }

    @Override
    public boolean isRetrier() {
        return mIsListRetrier;
    }

    public boolean isLoaderOrRetrier() {
        return mIsListLoader || mIsListRetrier;
    }
}
