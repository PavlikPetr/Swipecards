package com.topface.topface.data.experiments;

import android.content.Intent;

import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.utils.gcmutils.GCMUtils;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_DIALOGS;

public class MessagesWithTabs {
    public static void equipNavigationActivityIntent(Intent intent) {
        intent.putExtra(GCMUtils.NEXT_INTENT, TABBED_DIALOGS);
        intent.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, DialogsFragment.class.getName());
    }
}
