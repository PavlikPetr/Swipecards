package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;
import com.topface.topface.ui.fragments.feed.enhanced.fans.FansFragment;
import com.topface.topface.ui.fragments.feed.enhanced.visitors.VisitorsFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;

public class TabbedVisitorsFragment extends TabbedFeedFragment {

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.general_visitors)));
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
        return mVisitorsLastOpenedPage;
    }

    @Override
    protected void setLastOpenedPage(int lastOpenedPage) {
        mVisitorsLastOpenedPage = lastOpenedPage;
    }

    @Override
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.VISITORS_TABS;
    }
}
