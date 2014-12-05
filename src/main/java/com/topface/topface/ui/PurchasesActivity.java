package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.topface.billing.OpenIabFragment;
import com.topface.offerwall.common.TFCredentials;
import com.topface.offerwall.publisher.TFOfferwallActivity;
import com.topface.offerwall.publisher.TFOfferwallSDK;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

public class PurchasesActivity extends CheckAuthActivity<PurchasesFragment> {

    private static final String TOPFACE_OFFERWALL_COMPLETENESS = "topface_offewall_completeness";

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */
    public static final int INTENT_BUY_VIP = 1;
    public static final int INTENT_BUY = 2;

    private ForceOfferwallRedirect mBonusRedirect;
    private static TopfaceOfferwallRedirect mTopfaceOfferwallRedirect;
    private boolean mIsTopfaceOfferwallCompleted;
    private boolean mIsTopfaceOfferwallsReady;

    static {
        if (CacheProfile.isLoaded()) {
            mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (TFOfferwallSDK.isInitialized()) {
            mIsTopfaceOfferwallsReady = true;
        }
        if (getIntent().hasExtra(TFOfferwallActivity.EXPEREMENT_GROUP)) {
            mIsTopfaceOfferwallCompleted = true;
        }
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
                    mIsTopfaceOfferwallsReady = true;
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
        outState.putBoolean(TOPFACE_OFFERWALL_COMPLETENESS, mIsTopfaceOfferwallCompleted);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    public void skipBonus() {
        getFragment().skipBonus();
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
        if (mTopfaceOfferwallRedirect != null && mTopfaceOfferwallRedirect.isEnabled() &&
                mTopfaceOfferwallRedirect.isExpOnOpen() && CacheProfile.money < itemPrice) {
            intent = TFOfferwallSDK.getIntent(context, true,
                    context.getString(R.string.general_bonus), TopfaceOfferwallRedirect.KEY_EXP_ON_OPEN);
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
        actionBar.setCustomView(R.layout.actionbar_container_title_view);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(android.R.color.transparent);
        actionBar.setLogo(android.R.color.transparent);
    }

    private boolean showBonus() {
        /*
        First check if redirection to topface offers is on. If no check redirection to bonus tab
         */
        if (mTopfaceOfferwallRedirect != null && mTopfaceOfferwallRedirect.isEnabled() &&
                mTopfaceOfferwallRedirect.isExpOnClose()) {
            return false;
        }
        return mBonusRedirect != null &&
                mBonusRedirect.isEnabled() &&
                getFragment().forceBonusScreen(mBonusRedirect.getText());
    }

    private boolean showTopfaceOfferwall() {
        if (mTopfaceOfferwallRedirect != null && mTopfaceOfferwallRedirect.isEnabled() &&
                mTopfaceOfferwallRedirect.isExpOnClose() && !mIsTopfaceOfferwallCompleted && mIsTopfaceOfferwallsReady) {
            OfferwallsManager.startTfOfferwall(this, TopfaceOfferwallRedirect.KEY_EXP_ON_CLOSE);
            mIsTopfaceOfferwallCompleted = true;
            return true;
        }
        return false;
    }
}
