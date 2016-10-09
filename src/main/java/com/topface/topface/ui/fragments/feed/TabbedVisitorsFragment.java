package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;
import com.topface.topface.ui.fragments.feed.fans.FansFragment;
import com.topface.topface.ui.fragments.feed.visitors.VisitorsFragment;

public class TabbedVisitorsFragment extends TabbedFeedFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.general_visitors);
    }

    @Override
    protected String getSubtitle() {
        return "";
    }

    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(VisitorsFragment.class.getName(), countersData.getVisitors());
        updatePageCounter(FansFragment.class.getName(), countersData.getFans());
    }

    @Override
    protected void addPages() {
        addBodyPage(VisitorsFragment.class.getName(), getString(R.string.general_visitors_tab_views), mCountersData.getVisitors());
        addBodyPage(FansFragment.class.getName(), getString(R.string.general_fans), mCountersData.getFans());
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
