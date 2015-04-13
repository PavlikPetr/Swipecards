package com.topface.topface.ui.fragments.profile;

import android.content.Context;

import com.topface.topface.data.Photos;
import com.topface.topface.ui.adapters.LoadingListAdapter;

public class OwnPhotoGridAdapter extends ProfilePhotoGridAdapter {


    public OwnPhotoGridAdapter(Context context, Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(context, photoLinks, totalPhotos, callback);
    }

    @Override
    protected boolean isAddPhotoButtonEnabled() {
        return true;
    }
}
