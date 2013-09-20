package com.topface.topface.ui.fragments.feed;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.DeleteFeedsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.VisitorsListAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import org.json.JSONObject;

import java.util.List;


public class VisitorsFragment extends NoFilterFeedFragment<Visitor> {

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.visitors);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_visitors);
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
        View btnBuyVip = inflated.findViewById(R.id.btnBuyVip);
        if (CacheProfile.premium) {
            inflated.findViewById(R.id.tvText).setVisibility(View.GONE);
            btnBuyVip.setVisibility(View.GONE);
        } else {
            inflated.findViewById(R.id.tvText).setVisibility(View.VISIBLE);
            btnBuyVip.setVisibility(View.VISIBLE);
            btnBuyVip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = ContainerActivity.getVipBuyIntent(null, "Visitors");
                    startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                }
            });
        }
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_visitors;
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_GUESTS;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.VISITORS;
    }

    @Override
    protected boolean isForPremium() {
        return true;
    }

    @Override
    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu_visitors;
    }

    @Override
    protected DeleteFeedsRequest getDeleteRequest(List<String> ids, Context context) {
        return null;
    }
}
