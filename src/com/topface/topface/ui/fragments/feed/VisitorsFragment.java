package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.VisitorsListAdapter;
import com.topface.topface.utils.CountersManager;
import org.json.JSONObject;


public class VisitorsFragment extends NoFilterFeedFragment<Visitor> {

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.visitors);
    }

    @Override
    protected int getTitle() {
        return R.string.general_visitors;
    }

    @Override
    protected FeedAdapter<Visitor> getNewAdapter() {
        return new VisitorsListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<Visitor> getFeedList(JSONObject response) {
        return new FeedListData<Visitor>(response, Visitor.class);
    }


    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.VISITORS;
    }

    @Override
    protected void initEmptyFeedView(View inflated) {
        inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
            }
        });
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_visitors;
    }

    @Override
    protected void decrementCounters() {
        CountersManager.getInstance(getActivity().getApplicationContext()).decrementCounter(CountersManager.VISITORS);
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_GUESTS;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.VISITORS;
    }
}
