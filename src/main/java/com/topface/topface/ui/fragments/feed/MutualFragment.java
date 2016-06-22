package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteMutualsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.MutualListAdapter;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;


@FlurryOpenEvent(name = MutualFragment.PAGE_NAME)
public class MutualFragment extends FeedFragment<FeedMutual> {

    public static final String PAGE_NAME = "Mutual";

    @Inject
    NavigationState mNavigationState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().inject(this);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    protected MutualListAdapter createNewAdapter() {
        return new MutualListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedMutual>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedMutual.class;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.MUTUAL;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {

        inflated.findViewById(R.id.btnGetDating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavigationState.emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.DATING), WrappedNavigationData.SELECT_EXTERNALY));
            }
        });

        inflated.findViewById(R.id.btnRefill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent("EmptyMutual", App.from(getActivity()).getOptions().topfaceOfferwallRedirect));
            }
        });
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_mutual;
    }


    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_MUTUAL};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.SYMPATHY;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteMutualsRequest(ids, getActivity());
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_MUTUAL_UPDATE;
    }

    @Override
    protected int getUnreadCounter() {
        return mCountersData.getMutual();
    }

}
