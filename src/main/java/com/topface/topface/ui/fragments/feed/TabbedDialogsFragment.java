package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.utils.CacheProfile;

public class TabbedDialogsFragment extends TabbedFeedFragment {

    @Override
    protected boolean isScrollable() {
        return false;
    }

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
        return mDialogsLastOpenedPage;
    }

    @Override
    protected void setLastOpenedPage(int lastOpenedPage) {
        mDialogsLastOpenedPage = lastOpenedPage;
    }

    @Override
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.MESSAGES_TABS;
    }
}
