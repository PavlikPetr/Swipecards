package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.topface.billing.OpenIabFragment;
import com.topface.offerwall.common.OfferwallPayload;
import com.topface.offerwall.common.TFCredentials;
import com.topface.offerwall.publisher.TFOfferwallActivity;
import com.topface.offerwall.publisher.TFOfferwallSDK;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.actionbar.ActionBarView;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

public class PurchasesActivity extends CheckAuthActivity<PurchasesFragment> {

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */
    public static final int INTENT_BUY_VIP = 1;
    public static final int INTENT_BUY = 2;
    private static TopfaceOfferwallRedirect mTopfaceOfferwallRedirect;
    private BroadcastReceiver mOfferwallOpenedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTopfaceOfferwallRedirect != null) {
                mTopfaceOfferwallRedirect.setCompletedByBroadcast(intent);
            }
        }
    };
    private ForceOfferwallRedirect mBonusRedirect;
    private boolean mIsOfferwallsReady;

    static {
        if (CacheProfile.isLoaded()) {
            mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;
        }
    }

    public static Intent createVipBuyIntent(String extraText, String from) {
        Intent intent = new Intent(App.getContext(), PurchasesActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY_VIP);
        intent.putExtra(PurchasesFragment.ARG_TAG_EXRA_TEXT, extraText);
        intent.putExtra(OpenIabFragment.ARG_TAG_SOURCE, from);
        intent.putExtra(PurchasesFragment.IS_VIP_PRODUCTS, true);
        return intent;
    }

    public static Intent createBuyingIntent(String from, int itemType, int itemPrice) {
        Intent intent;
        Context context = App.getContext();
        if (needTFOfferwallOnOpenRedirect(itemPrice)) {
            OfferwallPayload payload = new OfferwallPayload();
            payload.experimentGroup = TopfaceOfferwallRedirect.KEY_EXP_ON_OPEN;
            intent = TFOfferwallSDK.getIntent(context, true, context.getString(R.string.general_bonus), payload);
            intent.putExtra(TFOfferwallActivity.RELAUNCH_PARENT_WITH_SAME_INTENT, true);
        } else {
            intent = new Intent(context, PurchasesActivity.class);
        }
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY);
        intent.putExtra(OpenIabFragment.ARG_TAG_SOURCE, from);
        if (itemType != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_TYPE, itemType);
        }
        if (itemPrice != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_PRICE, itemPrice);
        }
        return intent;
    }

    public static Intent createBuyingIntent(String from, int itemPrice) {
        return createBuyingIntent(from, -1, itemPrice);
    }

    public static Intent createBuyingIntent(String from) {
        return createBuyingIntent(from, -1, -1);
    }

    private static boolean needTFOfferwallOnOpenRedirect(int itemPrice) {
        return TFOfferwallSDK.canShowOffers() && isTopfaceOfferwallRedirectEnabled() && mTopfaceOfferwallRedirect.isExpOnOpen() &&
                CacheProfile.money < itemPrice && mTopfaceOfferwallRedirect.showOrNot();
    }

    private static boolean isTopfaceOfferwallRedirectEnabled() {
        return mTopfaceOfferwallRedirect != null && mTopfaceOfferwallRedirect.isEnabled();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (TFOfferwallSDK.isInitialized()) {
            mIsOfferwallsReady = TFCredentials.getAdId() != null;
        }
        if (mTopfaceOfferwallRedirect != null) {
            mTopfaceOfferwallRedirect.setCompletedByIntent(getIntent());
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mOfferwallOpenedReceiver, new IntentFilter(BonusFragment.OFFERWALL_OPENED));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOfferwallOpenedReceiver);
        mTopfaceOfferwallRedirect.setComplited(false);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (showTopfaceOfferwall() || showBonus()) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!(showTopfaceOfferwall() || showBonus())) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        mBonusRedirect = CacheProfile.getOptions().forceOfferwallRedirect;
        if (mTopfaceOfferwallRedirect == null) {
            mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;
        }
        if (!TFOfferwallSDK.isInitialized()) {
            OfferwallsManager.initTfOfferwall(this, new TFCredentials.OnInitializeListener() {
                @Override
                public void onInitialized() {
                    mIsOfferwallsReady = true;
                }

                @Override
                public void onError() {
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TopfaceOfferwallRedirect.TOPFACE_OFFERWAL_REDIRECT, mTopfaceOfferwallRedirect);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        TopfaceOfferwallRedirect topfaceOfferwallRedirectRestored =
                savedInstanceState.getParcelable(TopfaceOfferwallRedirect.TOPFACE_OFFERWAL_REDIRECT);
        if (mTopfaceOfferwallRedirect == null) {
            mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;
        }
        mTopfaceOfferwallRedirect.setComplited(topfaceOfferwallRedirectRestored.isCompleted());
    }

    public void skipBonus() {
        getFragment().skipBonus();
    }

    @Override
    protected String getFragmentTag() {
        return PurchasesFragment.class.getSimpleName();
    }

    @Override
    protected PurchasesFragment createFragment() {
        return new PurchasesFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Это супер мега хак, смотри документацию processRequestCode
        if (!OpenIabFragment.processRequestCode(
                getSupportFragmentManager(),
                requestCode,
                resultCode,
                data,
                PurchasesFragment.class //В нашем случае BillingFragment находится внутри PurchasesFragment
        )) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void initActionBar(ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBarView = new ActionBarView(actionBar, this);
        actionBarView.setPurchasesView((String) getTitle());
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(android.R.color.transparent);
        actionBar.setLogo(android.R.color.transparent);
    }

    private boolean needTFOfferwallOnCloseRedirect() {
        return isTopfaceOfferwallRedirectEnabled() && mTopfaceOfferwallRedirect.isExpOnClose() &&
                !mTopfaceOfferwallRedirect.isCompleted() && mIsOfferwallsReady && !getFragment().isVipProducts();
    }

    private boolean showBonus() {
        /*
        First check if redirection to topface offers is on. If no check redirection to bonus tab
         */
        if ((isTopfaceOfferwallRedirectEnabled() && mTopfaceOfferwallRedirect.isExpOnClose()) ||
                mTopfaceOfferwallRedirect.isCompleted()) {
            return false;
        }
        return mBonusRedirect != null &&
                mBonusRedirect.isEnabled() &&
                getFragment().forceBonusScreen(mBonusRedirect.getText());
    }

    private boolean showTopfaceOfferwall() {
        if (needTFOfferwallOnCloseRedirect()) {
            OfferwallPayload payload = new OfferwallPayload();
            payload.experimentGroup = TopfaceOfferwallRedirect.KEY_EXP_ON_CLOSE;
            OfferwallsManager.startTfOfferwall(this, payload);
            mTopfaceOfferwallRedirect.setComplited(true);
            return true;
        }
        return false;
    }
}
