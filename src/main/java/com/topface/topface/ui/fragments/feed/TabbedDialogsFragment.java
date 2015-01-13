package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.utils.CacheProfile;

public class TabbedDialogsFragment extends TabbedFeedFragment {
    private static int mLastOpenedPage = 0;

    @Override
    protected void onBeforeCountersUpdate() {
        updatePageCounter(DialogsFragment.class.getName(), CacheProfile.unread_messages);
    }


    @Override
    protected String getTitle() {
        return getString(R.string.settings_messages);
    }

    @Override
    protected void addPages() {
        addBodyPage(DialogsFragment.class.getName(), getString(R.string.general_dbl_all), CacheProfile.unread_messages);
        addBodyPage(BookmarksFragment.class.getName(), getString(R.string.general_bookmarks), 0);
    }

    @Override
    protected int getLastOpenedPage() {
        return mLastOpenedPage;
    }

    @Override
    protected void setLastOpenedPage(int lastOpenedPage) {
        mLastOpenedPage = lastOpenedPage;
    }

    @Override
    protected int getIndicatorLayout() {
        return R.layout.tab_indicator_dialogs;
    }

    @Override
    public String getPageName() {
        return PageInfo.PAGE_TABBED_MESSAGES;
    }
}
