package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.utils.CacheProfile;

public class TabbedLikesFragment extends TabbedFeedFragment {
    private static int mLastOpenedPage = 0;

    @Override
    protected void onBeforeCountersUpdate() {
        updatePageCounter(LikesFragment.class.getName(), CacheProfile.unread_likes);
        updatePageCounter(MutualFragment.class.getName(), CacheProfile.unread_mutual);
        if (!CacheProfile.getOptions().isHideAdmiration) {
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
        if (!CacheProfile.getOptions().isHideAdmiration) {
            addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), CacheProfile.unread_admirations);
        }
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
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.LIKES_TABS;
    }
}
