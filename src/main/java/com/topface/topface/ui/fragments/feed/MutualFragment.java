package com.topface.topface.ui.fragments.feed;

import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteMutualsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.MutualListAdapter;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.json.JSONObject;

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
    protected FeedListData<FeedMutual> getFeedList(JSONObject response) {
        return new FeedListData<>(response, FeedMutual.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.MUTUAL;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {

        inflated.findViewById(R.id.btnGetDating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment.selectFragment(FragmentId.DATING);
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
    protected int getFeedCounter() {
        return mCountersData.mutual;
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
