package com.topface.topface.ui.fragments.feed.feed_di;

import android.content.Context;

import com.topface.topface.ui.fragments.feed.blacklist.BlackListFragmentViewModel;
import com.topface.topface.ui.fragments.feed.bookmarks.BookmarksFragmentViewModel;
import com.topface.topface.ui.fragments.feed.dialogs.DialogsFragmentViewModel;
import com.topface.topface.ui.fragments.feed.fans.FansFragmentViewModel;
import com.topface.topface.ui.fragments.feed.feed_base.ActionModeController;
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel;
import com.topface.topface.ui.fragments.feed.feed_base.BaseLockScreenViewModel;
import com.topface.topface.ui.fragments.feed.likes.LikesFragmentViewModel;
import com.topface.topface.ui.fragments.feed.mutual.MutualFragmentViewModel;
import com.topface.topface.ui.fragments.feed.photoblog.PhotoblogFragmentViewModel;
import com.topface.topface.ui.fragments.feed.visitors.VisitorsFragmentViewModel;
import com.topface.topface.ui.fragments.feed.visitors.VisitorsLockScreenViewModel;

import dagger.Module;
import dagger.Provides;

@Module(library = true, complete = false,
        injects = {
                ActionModeController.class,
                VisitorsLockScreenViewModel.class,
                BlackListFragmentViewModel.class,
                BookmarksFragmentViewModel.class,
                BaseFeedFragmentViewModel.class,
                FansFragmentViewModel.class,
                LikesFragmentViewModel.class,
                MutualFragmentViewModel.class,
                PhotoblogFragmentViewModel.class,
                VisitorsFragmentViewModel.class,
                DialogsFragmentViewModel.class,
                BaseLockScreenViewModel.class
        })
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
