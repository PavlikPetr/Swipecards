package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;

import com.topface.billing.OpenIabFragment;
import com.topface.offerwall.common.OfferwallPayload;
import com.topface.offerwall.common.TFCredentials;
import com.topface.offerwall.publisher.TFOfferwallActivity;
import com.topface.offerwall.publisher.TFOfferwallSDK;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.buy.PurchasesConstants;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.PurchasesUtils;
import com.topface.topface.utils.actionbar.ActionBarView;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import static com.topface.topface.ui.PaymentwallActivity.PW_CURRENCY;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRICE;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRODUCTS_COUNT;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRODUCTS_TYPE;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRODUCT_ID;
import static com.topface.topface.ui.PaymentwallActivity.PW_TRANSACTION_ID;

public class PurchasesActivity extends CheckAuthActivity<PurchasesFragment> {

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */

    // здесь настраивается вероятность, с которой будут отображаться экраны в случае "холостого" выхода
    // с экрана покупок
    private enum EXTRA_SCREEN {
        TOPFACE_OFFERWALL_SCREEN(0, 10), BONUS_SCREEN(1, 30), SMS_INVITE_SCREEN(2, 70);

        private int pos;
        private int probability;

        EXTRA_SCREEN(int pos, int probability) {
            this.pos = pos;
            this.probability = probability;
        }

        public int getPosition() {
            return pos;
        }

        public int getProbability() {
            return probability;
        }

    }

    @Inject
    static TopfaceAppState mAppState;
    public static final int INTENT_BUY_VIP = 1;
    public static final int INTENT_BUY = 2;

    private ForceOfferwallRedirect mBonusRedirect;
    private static TopfaceOfferwallRedirect mTopfaceOfferwallRedirect;
    private boolean mIsOfferwallsReady;

    static {
        mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;
    }

    private BroadcastReceiver mOfferwallOpenedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTopfaceOfferwallRedirect != null) {
                mTopfaceOfferwallRedirect.setCompletedByBroadcast(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        App.from(this).inject(this);
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
        if (mTopfaceOfferwallRedirect != null) {
            mTopfaceOfferwallRedirect.setComplited(false);
        }
        super.onDestroy();
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        mBonusRedirect = CacheProfile.getOptions().forceOfferwallRedirect;
        mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;
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
    public void onSaveInstanceState(Bundle outState) {
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

    public static Intent createVipBuyIntent(String extraText, String from) {
        Intent intent = new Intent(App.getContext(), PurchasesActivity.class);
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_BUY_VIP);
        intent.putExtra(PurchasesFragment.ARG_TAG_EXRA_TEXT, extraText);
        intent.putExtra(PurchasesConstants.ARG_TAG_SOURCE, from);
        intent.putExtra(PurchasesFragment.IS_VIP_PRODUCTS, true);
        return intent;
    }

    public static Intent createBuyingIntent(String from, int itemType, int itemPrice) {
        Intent intent;
        Context context = App.getContext();
        if (needTFOfferwallOnOpenRedirect(itemPrice)) {
            OfferwallPayload payload = new OfferwallPayload();
            payload.experimentGroup = mTopfaceOfferwallRedirect.getGroup();
            intent = TFOfferwallSDK.getIntent(context, true, context.getString(R.string.general_bonus), payload);
            intent.putExtra(TFOfferwallActivity.RELAUNCH_PARENT_WITH_SAME_INTENT, true);
        } else {
            intent = new Intent(context, PurchasesActivity.class);
        }
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_BUY);
        intent.putExtra(PurchasesConstants.ARG_TAG_SOURCE, from);
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
        if (resultCode == RESULT_OK && requestCode == PaymentwallActivity.ACTION_BUY) {
            PurchasesUtils.sendPurchaseEvent(
                    data.getIntExtra(PW_PRODUCTS_COUNT, 0),
                    data.getStringExtra(PW_PRODUCTS_TYPE),
                    data.getStringExtra(PW_PRODUCT_ID),
                    data.getStringExtra(PW_CURRENCY),
                    data.getDoubleExtra(PW_PRICE, 0),
                    data.getStringExtra(PW_TRANSACTION_ID), false, false);
            // для обновления счетчиков монет и лайков при покупке через paymentWall
            new ProfileRequest(this).exec();
        }
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

    private static boolean needTFOfferwallOnOpenRedirect(int itemPrice) {
        return TFOfferwallSDK.canShowOffers() && isTopfaceOfferwallRedirectEnabled() && mTopfaceOfferwallRedirect.isExpOnOpen() &&
                mAppState.getBalance().money < itemPrice && mTopfaceOfferwallRedirect.showOrNot();
    }

    private boolean needTFOfferwallOnCloseRedirect() {
        return isTopfaceOfferwallRedirectEnabled() && mTopfaceOfferwallRedirect.isExpOnClose() &&
                !mTopfaceOfferwallRedirect.isCompleted() && mIsOfferwallsReady && !getFragment().isVipProducts();
    }

    private boolean isTopfaceOfferwallAvailable() {
        return needTFOfferwallOnCloseRedirect();
    }

    private boolean isBonusAvailable() {
        return !((isTopfaceOfferwallRedirectEnabled() && mTopfaceOfferwallRedirect.isExpOnClose()) ||
                mTopfaceOfferwallRedirect.isCompleted() || getFragment().isBonusSkiped() ||
                !getFragment().isBonusPageAvailable() || !(mBonusRedirect != null && mBonusRedirect.isEnabled()));
    }

    private boolean isSMSInviteAvailable() {
        return !CacheProfile.premium && !(mTopfaceOfferwallRedirect != null &&
                (mTopfaceOfferwallRedirect.isExpOnClose() ||
                        mTopfaceOfferwallRedirect.isCompleted())) &&
                CacheProfile.getOptions().forceSmsInviteRedirect.enabled;
    }

    private boolean showExtraScreen(EXTRA_SCREEN screen) {
        if (null != screen) {
            switch (screen) {
                case BONUS_SCREEN:
                    return mBonusRedirect != null &&
                            getFragment().forceBonusScreen(mBonusRedirect.getText());
                case SMS_INVITE_SCREEN:
                    finish();
                    startActivity(SMSInviteActivity.createIntent(this));
                    mTopfaceOfferwallRedirect.setComplited(true);
                    return true;

                case TOPFACE_OFFERWALL_SCREEN:
                    OfferwallPayload payload = new OfferwallPayload();
                    payload.experimentGroup = mTopfaceOfferwallRedirect.getGroup();
                    OfferwallsManager.startTfOfferwall(this, payload);
                    mTopfaceOfferwallRedirect.setComplited(true);
                    return true;
            }
        }
        return false;
    }

    private static boolean isTopfaceOfferwallRedirectEnabled() {
        return mTopfaceOfferwallRedirect != null && mTopfaceOfferwallRedirect.isEnabled();
    }

    @Override
    protected boolean onPreFinish() {
        return !isScreenShow() && super.onPreFinish();
    }

    private boolean isScreenShow() {
        return showExtraScreen(getRandomPosByProbability(getListOfExtraScreens()));
    }

    private boolean callTrialVipPopup(DialogInterface.OnDismissListener dismissListener) {
        if (getIntent().getIntExtra(App.INTENT_REQUEST_KEY, -1) == INTENT_BUY_VIP && App.isNeedShowTrial
                && !CacheProfile.getProfile().premium && new GoogleMarketApiManager().isMarketApiAvailable()
                && CacheProfile.getOptions().trialVipExperiment.enabled && !CacheProfile.paid) {
            TrialVipPopup trialVipPopup = TrialVipPopup.newInstance(true);
            trialVipPopup.setOnDismissListener(dismissListener);
            trialVipPopup.show(getSupportFragmentManager(), TrialVipPopup.TAG);
            App.isNeedShowTrial = false;
            return true;
        }
        return false;
    }

    @Override
    public void onUpClick() {
        boolean isCalled = callTrialVipPopup(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                PurchasesActivity.super.onUpClick();
            }
        });
        if (!isCalled) {
            super.onUpClick();
        }
    }

    @Override
    public void onBackPressed() {
        boolean isCalled = callTrialVipPopup(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onBackPressed();
            }
        });
        if (!isCalled && !isScreenShow()) {
            super.onBackPressed();
        }
    }

    private EXTRA_SCREEN getRandomPosByProbability(List<EXTRA_SCREEN> extraScreenArray) {
        if (null == extraScreenArray || extraScreenArray.size() == 0) {
            return null;
        }
        int sum = 0;
        for (int i = 0; i < extraScreenArray.size(); i++) {
            sum += extraScreenArray.get(i).getProbability();
        }
        int randomValue = new Random().nextInt(sum - 1) + 1;
        sum = 0;
        for (int i = 0; i < extraScreenArray.size(); i++) {
            EXTRA_SCREEN currentValue = extraScreenArray.get(i);
            if (randomValue > sum && randomValue <= (currentValue.getProbability() + sum)) {
                return currentValue;
            }
            sum += currentValue.getProbability();
        }
        return null;
    }

    private List<EXTRA_SCREEN> getListOfExtraScreens() {
        List<EXTRA_SCREEN> screensArray = new ArrayList<>();
        if (isTopfaceOfferwallAvailable()) {
            screensArray.add(EXTRA_SCREEN.TOPFACE_OFFERWALL_SCREEN);
        }
        if (isBonusAvailable()) {
            screensArray.add(EXTRA_SCREEN.BONUS_SCREEN);
        }
        if (isSMSInviteAvailable()) {
            screensArray.add(EXTRA_SCREEN.SMS_INVITE_SCREEN);
        }
        return screensArray;
    }

}
