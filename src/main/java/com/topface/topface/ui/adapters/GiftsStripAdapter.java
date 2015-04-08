package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.ui.views.ImageViewRemote;

/**
 * Adapter for gifts int user's form
 */
public class GiftsStripAdapter extends GiftsAdapter {
    public GiftsStripAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    @Override
    protected int getGiftItemLayoutRes() {
        return R.layout.profile_item_form_gift;
    }
}
