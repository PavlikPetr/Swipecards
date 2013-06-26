package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.MutualListAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CountersManager;
import org.json.JSONObject;

public class MutualFragment extends FeedFragment<FeedMutual> {
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.mutual_back_icon);
    }

    @Override
    protected int getTitle() {
        return R.string.general_mutual;
    }

    @Override
    protected MutualListAdapter getNewAdapter() {
        return new MutualListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<FeedMutual> getFeedList(JSONObject response) {
        return new FeedListData<FeedMutual>(response, FeedMutual.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.MUTUAL;
    }

    @Override
    protected void initEmptyFeedView(View inflated) {

        inflated.findViewById(R.id.btnGetDating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationActivity.selectFragment(BaseFragment.F_DATING);
            }
        });

        inflated.findViewById(R.id.btnRefill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContainerActivity.getBuyingIntent());
            }
        });
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_mutual;
    }


    @Override
    protected void decrementCounters() {
        CountersManager.getInstance(getActivity().getApplicationContext()).decrementCounter(CountersManager.SYMPATHY);
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_SYMPATHY;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.SYMPATHY;
    }
}
