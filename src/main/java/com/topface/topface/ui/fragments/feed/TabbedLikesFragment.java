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
import com.topface.topface.experiments.feed_design.DialogsAndLikesFeedDesigned;
import com.topface.topface.ui.fragments.feed.admiration.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.likes.LikesFragment;
import com.topface.topface.ui.fragments.feed.mutual.MutualFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.utils.config.WeakStorage;

import javax.inject.Inject;

public class TabbedLikesFragment extends TabbedFeedFragment {

    @Inject
    WeakStorage mWeakStorage;
    @Override
    protected void onBeforeCountersUpdate(CountersData countersData) {
        updatePageCounter(LikesFragment.class.getName(), countersData.getLikes());
        updatePageCounter(MutualFragment.class.getName(), countersData.getMutual());
        if (!App.from(getActivity()).getOptions().isHideAdmirations) {
            updatePageCounter(AdmirationFragment.class.getName(), countersData.getAdmirations());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.general_sympathies)));
    }

    @Override
    protected void addPages() {
        addBodyPage(LikesFragment.class.getName(), getString(R.string.general_likes), mCountersData.getLikes());
        if (DialogsAndLikesFeedDesigned.getDesignVersionJava() != DialogsAndLikesFeedDesigned.NEW_DIALOGS_AND_SINGLE_TAB) {
            addBodyPage(MutualFragment.class.getName(), getString(R.string.general_mutual), mCountersData.getMutual());
            if (!App.from(getActivity()).getOptions().isHideAdmirations) {
                addBodyPage(AdmirationFragment.class.getName(), getString(R.string.general_admirations), mCountersData.getAdmirations());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.get().inject(this);
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
