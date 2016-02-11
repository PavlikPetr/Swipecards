package com.topface.topface.ui.fragments.feed;

import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.R;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteMutualsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.MutualListAdapter;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public class MutualFragment extends FeedFragment<FeedMutual> {

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
    protected void initEmptyFeedView(View inflated, int errorCode) {

        inflated.findViewById(R.id.btnGetDating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment.selectFragment(FragmentId.DATING.getFragmentSettings());
            }
        });

        inflated.findViewById(R.id.btnRefill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent("EmptyMutual"));
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
        return mCountersData.mutual;
    }

}
