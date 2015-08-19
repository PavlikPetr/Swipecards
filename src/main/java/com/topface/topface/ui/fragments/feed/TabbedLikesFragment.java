package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;

public class TabbedLikesFragment extends TabbedFeedFragment {

    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(LikesFragment.class.getName(), countersData.likes);
        updatePageCounter(MutualFragment.class.getName(), countersData.mutual);
        if (!getOptions().isHideAdmirations) {
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
        if (!getOptions().isHideAdmirations) {
            addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), mCountersData.admirations);
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
