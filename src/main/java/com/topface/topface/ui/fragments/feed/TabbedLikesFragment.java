package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.utils.CacheProfile;

public class TabbedLikesFragment extends TabbedFeedFragment {

    @Override
    protected void onBeforeCountersUpdate() {
        updatePageCounter(LikesFragment.class.getName(), CacheProfile.unread_likes);
        updatePageCounter(MutualFragment.class.getName(), CacheProfile.unread_mutual);
        if (!CacheProfile.getOptions().isHideAdmirations) {
            updatePageCounter(AdmirationFragment.class.getName(), CacheProfile.unread_admirations);
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    protected void addPages() {
        addBodyPage(LikesFragment.class.getName(), getString(R.string.general_likes), CacheProfile.unread_likes);
        addBodyPage(MutualFragment.class.getName(), getString(R.string.general_mutual), CacheProfile.unread_mutual);
        if (!CacheProfile.getOptions().isHideAdmirations) {
            addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), CacheProfile.unread_admirations);
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
