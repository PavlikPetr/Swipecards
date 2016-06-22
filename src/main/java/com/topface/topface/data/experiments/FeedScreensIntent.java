package com.topface.topface.data.experiments;

import android.content.Intent;

import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.utils.gcmutils.GCMUtils;

public class FeedScreensIntent {
    public static void equipMessageAllIntent(Intent intent) {
        equipFeedIntent(intent, new LeftMenuSettingsData(FragmentIdData.TABBED_DIALOGS), DialogsFragment.class.getName());
    }

    public static void equipNotificationIntent(Intent intent) {
        equipMessageAllIntent(intent);
        intent.putExtra(GCMUtils.NOTIFICATION_INTENT, true);
    }

    public static void equipPhotoFeedIntent(Intent intent) {
        equipFeedIntent(intent, new LeftMenuSettingsData(FragmentIdData.PHOTO_BLOG), PhotoBlogFragment.class.getName());
    }

    private static void equipFeedIntent(Intent intent, LeftMenuSettingsData fragmentSettings, String pageName) {
        if (intent != null) {
            intent.putExtra(GCMUtils.NEXT_INTENT, fragmentSettings);
            intent.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, pageName);
        }
    }

    public static void equipDatingIntent(Intent intent) {
        intent.putExtra(GCMUtils.NOTIFICATION_INTENT, true);
        equipFeedIntent(intent, new LeftMenuSettingsData(FragmentIdData.DATING), DatingFragment.class.getName());
    }

}
