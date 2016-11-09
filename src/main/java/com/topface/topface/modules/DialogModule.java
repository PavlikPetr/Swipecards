package com.topface.topface.modules;

import android.content.Context;

import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup;
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopupViewModel;
import com.topface.topface.ui.fragments.feed.admiration.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.admiration.AdmirationFragmentViewModel;
import com.topface.topface.ui.fragments.feed.admiration.AdmirationLockScreenViewModel;
import com.topface.topface.ui.fragments.feed.blacklist.BlackListFragmentViewModel;
import com.topface.topface.ui.fragments.feed.bookmarks.BookmarksFragmentViewModel;
import com.topface.topface.ui.fragments.feed.dialogs.DialogsFragmentViewModel;
import com.topface.topface.ui.fragments.feed.fans.FansFragmentViewModel;
import com.topface.topface.ui.fragments.feed.feed_base.ActionModeController;
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel;
import com.topface.topface.ui.fragments.feed.feed_base.BaseLockScreenViewModel;
import com.topface.topface.ui.fragments.feed.likes.LikesFragment;
import com.topface.topface.ui.fragments.feed.likes.LikesFragmentViewModel;
import com.topface.topface.ui.fragments.feed.likes.LikesLockScreenViewModel;
import com.topface.topface.ui.fragments.feed.mutual.MutualFragmentViewModel;
import com.topface.topface.ui.fragments.feed.mutual.MutualLockScreenViewModel;
import com.topface.topface.ui.fragments.feed.photoblog.HeaderPhotoBlogViewModel;
import com.topface.topface.ui.fragments.feed.photoblog.PhotoblogFragmentViewModel;
import com.topface.topface.ui.fragments.feed.visitors.VisitorsFragmentViewModel;
import com.topface.topface.ui.fragments.feed.visitors.VisitorsLockScreenViewModel;

import dagger.Module;
import dagger.Provides;

@Module(library = true, complete = false,
        injects = {
                TakePhotoPopupViewModel.class,
                TakePhotoPopup.class
        })
public class DialogModule {
}
