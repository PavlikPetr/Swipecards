package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;
import com.topface.topface.ui.fragments.feed.bookmarks.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.dialogs.DialogsFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;

public class TabbedDialogsFragment extends TabbedFeedFragment {

    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(DialogsFragment.class.getName(), countersData.getDialogs());
    }

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.settings_messages)));
    }

    @Override
    protected void addPages() {
        addBodyPage(DialogsFragment.class.getName(), getString(R.string.general_dbl_all), mCountersData.getDialogs());
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
