package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.config.FeedsCache;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class FansFragment extends BookmarksFragment {

    public static final String FANS = "Fans";

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
        Button buttonBuy = (Button) inflated.findViewById(R.id.btnBuy);
        TextView message = ((TextView) inflated.findViewById(R.id.tvText));
        if (CacheProfile.premium) {
            message.setText(R.string.buy_more_sympathies);
            buttonBuy.setText(R.string.buy_sympathies);
            buttonBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent(FANS));
                }
            });
        } else {
            message.setText(R.string.likes_buy_vip);
            buttonBuy.setText(R.string.buying_vip_status);
            buttonBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = PurchasesActivity.createVipBuyIntent(null, FANS);
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

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_FANS_FEEDS;
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

    @Override
    protected int getUnreadCounter() {
        return mCountersData.fans;
    }
}
