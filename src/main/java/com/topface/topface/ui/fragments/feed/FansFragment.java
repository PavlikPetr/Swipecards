package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;

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
        Button btnBuyVip = (Button) inflated.findViewById(R.id.btnBuyVip);
        TextView textView = ((TextView) inflated.findViewById(R.id.tvText));
        if (CacheProfile.premium) {
            textView.setText(App.getContext().getString(R.string.buy_more_sympathies));
            btnBuyVip.setText(App.getContext().getString(R.string.buy_sympathies));
            btnBuyVip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(getPurchasesActivityIntent(), PurchasesActivity.INTENT_BUY_VIP);
                }
            });
        } else {
            btnBuyVip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = PurchasesActivity.createVipBuyIntent(null, FANS);
                    startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                }
            });
        }

    }

    private Intent getPurchasesActivityIntent() {
        return CacheProfile.premium ? PurchasesActivity.createBuyingIntent(FANS)
                : PurchasesActivity.createVipBuyIntent(null, FANS);
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

    @Override
    protected int getUnreadCounter() {
        return CacheProfile.unread_fans;
    }
}
