package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;

import java.util.List;


public class FansFragment extends BookmarksFragment {
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.fans);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_fans);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.FANS;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
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
                    Intent intent = PurchasesActivity.createVipBuyIntent(null, "Fans");
                    startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                }
            });
        }


    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_fans;
    }

    @Override
    protected boolean isForPremium() {
        return true;
    }

    @Override
    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu_fans;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        //Данный тип ленты не поддерживает удаление
        return null;
    }
}
