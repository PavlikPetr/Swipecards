package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.topface.topface.R;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;


public class FansFragment extends BookmarksFragment{
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.fans);
    }

    @Override
    protected int getTitle() {
        return R.string.general_fans;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.FANS;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.general_no_fans;
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
        return R.layout.layout_empty_fans;
    }
}
