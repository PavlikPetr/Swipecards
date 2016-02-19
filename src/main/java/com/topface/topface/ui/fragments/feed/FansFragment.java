package com.topface.topface.ui.fragments.feed;

import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.config.FeedsCache;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class FansFragment extends BookmarksFragment {

    public static final String SCREEN_TYPE = "Fans";
    public static final String UNLOCK_FUCTIONALITY_TYPE = "fans";

    @Override
    protected String getTitle() {
        return getString(R.string.general_fans);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.FANS;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
        initGagView(inflated, R.string.likes_buy_vip, R.string.buying_vip_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE), PurchasesActivity.INTENT_BUY_VIP);
            }
        });
        setUnlockButtonView((Button) inflated.findViewById(R.id.btnUnlock));
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        initGagView(inflated, R.string.buy_more_sympathies, R.string.buy_sympathies, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent(SCREEN_TYPE, App.get().getOptions().topfaceOfferwallRedirect));
            }
        });
    }

    private void initGagView(@NotNull View inflated, @StringRes int text, @StringRes int buttonText, View.OnClickListener listener) {
        Button btnBuyVip = (Button) inflated.findViewById(R.id.btnBuy);
        TextView textView = (TextView) inflated.findViewById(R.id.tvText);
        textView.setText(text);
        btnBuyVip.setText(buttonText);
        btnBuyVip.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        btnBuyVip.setOnClickListener(listener);
    }

    @Override
    protected String getUnlockFunctionalityType() {
        return UNLOCK_FUCTIONALITY_TYPE;
    }

    @Override
    protected UnlockFunctionalityOption.UnlockScreenCondition getUnlockScreenCondition(UnlockFunctionalityOption data) {
        return data.getUnlockFansCondition();
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
