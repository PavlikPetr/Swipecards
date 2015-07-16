package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;
import com.topface.topface.utils.CacheProfile;

public class TabbedLikesFragment extends TabbedFeedFragment {

    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(LikesFragment.class.getName(), countersData.likes);
        updatePageCounter(MutualFragment.class.getName(), countersData.mutual);
        if (!CacheProfile.getOptions().isHideAdmirations) {
            updatePageCounter(AdmirationFragment.class.getName(), countersData.admirations);
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    protected void addPages() {
        addBodyPage(LikesFragment.class.getName(), getString(R.string.general_likes), mCountersData.likes);
        addBodyPage(MutualFragment.class.getName(), getString(R.string.general_mutual), mCountersData.mutual);
        if (!CacheProfile.getOptions().isHideAdmirations) {
            addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), mCountersData.admirations);
        }
    }

    @Override
    protected int getLastOpenedPage() {
        return mLikesLastOpenedPage;
    }

    @Override
    protected void setLastOpenedPage(int lastOpenedPage) {
        mLikesLastOpenedPage = lastOpenedPage;
    }

    @Override
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.LIKES_TABS;
    }
}
