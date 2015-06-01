package com.topface.topface.data.experiments;

import android.content.Intent;

import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.utils.gcmutils.GCMUtils;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.DATING;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.PHOTO_BLOG;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_DIALOGS;

public class FeedScreensIntent {
    public static void equipMessageAllIntent(Intent intent) {
        equipFeedIntent(intent, TABBED_DIALOGS, DialogsFragment.class.getName());
    }

    public static void equipNotificationIntent(Intent intent) {
        equipMessageAllIntent(intent);
        intent.putExtra(GCMUtils.NOTIFICATION_INTENT, true);
    }

    public static void equipPhotoFeedIntent(Intent intent) {
        equipFeedIntent(intent, PHOTO_BLOG, PhotoBlogFragment.class.getName());
    }

    private static void equipFeedIntent(Intent intent, BaseFragment.FragmentId feedId, String pageName) {
        if (intent != null) {
            intent.putExtra(GCMUtils.NEXT_INTENT, feedId);
            intent.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, pageName);
        }
    }

    public static void equipDatingIntent(Intent intent) {
        intent.putExtra(GCMUtils.NOTIFICATION_INTENT, true);
        equipFeedIntent(intent, DATING, DatingFragment.class.getName());
    }

}
