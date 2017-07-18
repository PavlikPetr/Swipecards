package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.statistics.android.Slices;
import com.topface.statistics.generated.TabbedLikesStatisticsGeneratedStatistics;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.CountersData;
import com.topface.topface.ui.fragments.feed.admiration.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.likes.LikesFragment;
import com.topface.topface.ui.fragments.feed.mutual.MutualFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;

import static com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.TabbedLikesStatistics.TAB_DESIGN_TYPE;


public class TabbedLikesFragment extends TabbedFeedFragment {

    private static String OLD_TABBED_LIKES = "new_tabbed_likes";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabbedLikesStatisticsGeneratedStatistics.sendNow_TABBED_LIKES_SHOW(new Slices().putSlice(TAB_DESIGN_TYPE, OLD_TABBED_LIKES));
    }

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
        addBodyPage(MutualFragment.class.getName(), getString(R.string.general_mutual), mCountersData.getMutual());
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
}
