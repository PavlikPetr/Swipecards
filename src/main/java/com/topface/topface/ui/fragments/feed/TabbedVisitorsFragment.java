package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.utils.CacheProfile;

public class TabbedVisitorsFragment extends TabbedFeedFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.general_visitors);
    }

    @Override
    protected void onBeforeCountersUpdate() {
        updatePageCounter(VisitorsFragment.class.getName(), CacheProfile.unread_visitors);
        updatePageCounter(FansFragment.class.getName(), CacheProfile.unread_fans);
    }

    @Override
    protected void addPages() {
        addBodyPage(VisitorsFragment.class.getName(), getString(R.string.general_visitors_tab_views), CacheProfile.unread_visitors);
        addBodyPage(FansFragment.class.getName(), getString(R.string.general_fans), CacheProfile.unread_fans);
    }

    @Override
    protected int getLastOpenedPage() {
        return mVisitorsastOpenedPage;
    }

    @Override
    protected void setLastOpenedPage(int lastOpenedPage) {
        mVisitorsastOpenedPage = lastOpenedPage;
    }

    @Override
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.VISITORS_TABS;
    }
}
