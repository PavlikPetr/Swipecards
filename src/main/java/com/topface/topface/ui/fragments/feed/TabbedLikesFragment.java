package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.utils.CacheProfile;

public class TabbedLikesFragment extends TabbedFeedFragment {

    @Override
    protected boolean isScrollable() {
        return true;
    }

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        TabLayout layout = (TabLayout) view.findViewById(R.id.feedTabs);
        layout.setTabMode(TabLayout.MODE_SCROLLABLE);
        return view;
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
