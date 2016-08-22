package com.topface.topface.ui.fragments.feed.feed_di;

import android.content.Context;

import com.topface.topface.ui.fragments.feed.feed_base.ActionModeController;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tiberal on 01.08.16.
 */
@Module(library = true, complete = false,
        injects = ActionModeController.class)
public class FeedModule {

    private final Context mContext;

    public FeedModule(Context context) {
        mContext = context;
    }

    @Provides
    public Context providesContext() {
        return mContext;
    }
}
