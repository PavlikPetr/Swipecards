package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;

public class TabbedVisitorsFragment extends TabbedFeedFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.general_visitors);
    }

    @Override
    protected boolean isScrollableTabs() {
        return false;
    }

    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(VisitorsFragment.class.getName(), countersData.visitors);
        updatePageCounter(FansFragment.class.getName(), countersData.fans);
    }

    @Override
    protected void addPages() {
        addBodyPage(VisitorsFragment.class.getName(), getString(R.string.general_visitors_tab_views), mCountersData.visitors);
        addBodyPage(FansFragment.class.getName(), getString(R.string.general_fans), mCountersData.fans);
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
