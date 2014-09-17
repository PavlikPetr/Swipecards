package com.topface.topface.utils.loadcontollers;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.HashMap;

public class FeedLoadController extends LoadController{

    @Override
    protected int[] getPreloadLimits() {
        return App.getContext().getResources().getIntArray(R.array.feed_limit);
    }

    @Override
    protected int[] getPreloadOffset() {
         return App.getContext().getResources().getIntArray(R.array.feed_offset);
    }
}
