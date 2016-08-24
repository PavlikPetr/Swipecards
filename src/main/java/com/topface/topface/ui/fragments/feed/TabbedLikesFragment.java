package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;

public class TabbedLikesFragment extends TabbedFeedFragment {

    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(com.topface.topface.ui.fragments.feed.likes.LikesFragment.class.getName(), countersData.getLikes());
        updatePageCounter(com.topface.topface.ui.fragments.feed.mutual.MutualFragment.class.getName(), countersData.getMutual());
        if (!App.from(getActivity()).getOptions().isHideAdmirations) {
            updatePageCounter(AdmirationFragment.class.getName(), countersData.getAdmirations());
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    protected void addPages() {
        addBodyPage(com.topface.topface.ui.fragments.feed.likes.LikesFragment.class.getName(), getString(R.string.general_likes), mCountersData.getLikes());
        addBodyPage(com.topface.topface.ui.fragments.feed.mutual.MutualFragment.class.getName(), getString(R.string.general_mutual), mCountersData.getMutual());
        if (!App.from(getActivity()).getOptions().isHideAdmirations) {
            addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), mCountersData.getAdmirations());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
