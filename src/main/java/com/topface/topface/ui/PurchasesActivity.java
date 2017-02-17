package com.topface.topface.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.OfferwallPayload;
import com.topface.offerwall.common.TFCredentials;
import com.topface.offerwall.publisher.TFOfferwallActivity;
import com.topface.offerwall.publisher.TFOfferwallSDK;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarBinding;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.state.EventBus;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.bonus.view.BonusActivity;
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentBoilerplateFragment;
import com.topface.topface.ui.external_libs.offers.OffersModels;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.buy.PurchasesConstants;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel;
import com.topface.topface.ui.views.toolbar.view_models.PurchaseToolbarViewModel;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.PurchasesUtils;
import com.topface.topface.utils.controllers.startactions.TrialVipPopupAction;
import com.topface.topface.utils.rx.RxUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.ui.PaymentwallActivity.PW_CURRENCY;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRICE;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRODUCTS_COUNT;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRODUCTS_TYPE;
import static com.topface.topface.ui.PaymentwallActivity.PW_PRODUCT_ID;
import static com.topface.topface.ui.PaymentwallActivity.PW_TRANSACTION_ID;

public class PurchasesActivity extends CheckAuthActivity<PurchasesFragment, AcFragmentFrameBinding>
        implements ITabLayoutHolder {

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */

    // здесь настраивается вероятность, с которой будут отображаться экраны в случае "холостого" выхода
    // с экрана покупок
    private enum EXTRA_SCREEN {
        //TODO занулил вероятность показа SMS_INVITE_SCREEN пока не впилим роддержку пермишинов для смс и контактов
        TOPFACE_OFFERWALL_SCREEN(0, 10), BONUS_SCREEN(1, 30), SMS_INVITE_SCREEN(2, 0);

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

    @NotNull
    @Override
    protected BaseToolbarViewModel generateToolbarViewModel(@NotNull ToolbarBinding toolbar) {
        return new PurchaseToolbarViewModel(toolbar, this);
    }


    @Inject
    static TopfaceAppState mAppState;
    @Inject
    EventBus mEventBus;
    public static final int INTENT_BUY_VIP = 1;
    public static final int INTENT_BUY = 2;

    private ForceOfferwallRedirect mBonusRedirect;
    private static TopfaceOfferwallRedirect mTopfaceOfferwallRedirect;
    private boolean mIsOfferwallsReady;
    private Subscription mEventBusSubscriber;

    private ExperimentBoilerplateFragment mTrialVipPopup;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        App.from(this).inject(this);
        mTopfaceOfferwallRedirect = App.from(this).getOptions().topfaceOfferwallRedirect;
        if (TFOfferwallSDK.isInitialized()) {
            mIsOfferwallsReady = TFCredentials.getAdId() != null;
        }
        if (mTopfaceOfferwallRedirect != null) {
            mTopfaceOfferwallRedirect.setCompletedByIntent(getIntent());
        }
        mEventBusSubscriber = mEventBus.getObservable(OffersModels.OfferOpened.class).subscribe(new Action1<OffersModels.OfferOpened>() {
            @Override
            public void call(OffersModels.OfferOpened offerOpened) {
                if (mTopfaceOfferwallRedirect != null) {
                    mTopfaceOfferwallRedirect.setCompletedByBroadcast();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Debug.error(throwable);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mTopfaceOfferwallRedirect != null) {
            mTopfaceOfferwallRedirect.setComplited(false);
        }
        RxUtils.safeUnsubscribe(mEventBusSubscriber);
        super.onDestroy();
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        Options options = App.from(this).getOptions();
        mBonusRedirect = options.forceOfferwallRedirect;
        mTopfaceOfferwallRedirect = options.topfaceOfferwallRedirect;
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
            mTopfaceOfferwallRedirect = App.from(this).getOptions().topfaceOfferwallRedirect;
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

    public static Intent createBuyingIntent(String from, int itemType, int itemPrice, TopfaceOfferwallRedirect topfaceOfferwallRedirect) {
        Intent intent;
        Context context = App.getContext();
        if (needTFOfferwallOnOpenRedirect(itemPrice, topfaceOfferwallRedirect)) {
            OfferwallPayload payload = new OfferwallPayload();
            payload.experimentGroup = topfaceOfferwallRedirect.getGroup();
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

    public static Intent createBuyingIntent(String from, TopfaceOfferwallRedirect redirect) {
        return createBuyingIntent(from, -1, -1, redirect);
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
            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(new Intent(TabbedFeedFragment.HAS_FEED_AD));
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
    protected void initActionBarOptions(@Nullable ActionBar actionBar) {
        super.initActionBarOptions(actionBar);
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }


    private static boolean needTFOfferwallOnOpenRedirect(int itemPrice, TopfaceOfferwallRedirect topfaceOfferwallRedirect) {
        return TFOfferwallSDK.canShowOffers() && isTopfaceOfferwallRedirectEnabled(topfaceOfferwallRedirect) && topfaceOfferwallRedirect.isExpOnOpen() &&
                mAppState.getBalance().money < itemPrice && topfaceOfferwallRedirect.showOrNot();
    }

    private boolean needTFOfferwallOnCloseRedirect() {
        return isTopfaceOfferwallRedirectEnabled(App.from(this).getOptions().topfaceOfferwallRedirect) && mTopfaceOfferwallRedirect.isExpOnClose() &&
                !mTopfaceOfferwallRedirect.isCompleted() && mIsOfferwallsReady && !getFragment().isVipProducts();
    }

    private boolean isTopfaceOfferwallAvailable() {
        return needTFOfferwallOnCloseRedirect();
    }

    private boolean isBonusAvailable() {
        return !((isTopfaceOfferwallRedirectEnabled(App.from(this).getOptions().topfaceOfferwallRedirect) && mTopfaceOfferwallRedirect.isExpOnClose()) ||
                mTopfaceOfferwallRedirect.isCompleted() || getFragment().isBonusSkiped() ||
                !getFragment().isBonusPageAvailable() || !(mBonusRedirect != null && mBonusRedirect.isEnabled()));
    }

    private boolean isSMSInviteAvailable() {
        return !App.from(this).getProfile().premium && !(mTopfaceOfferwallRedirect != null &&
                (mTopfaceOfferwallRedirect.isExpOnClose() ||
                        mTopfaceOfferwallRedirect.isCompleted())) &&
                App.from(this).getOptions().forceSmsInviteRedirect.enabled;
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
                    finish();
                    startActivity(BonusActivity.createIntent());
                    mTopfaceOfferwallRedirect.setComplited(true);
                    return true;
            }
        }
        return false;
    }

    private static boolean isTopfaceOfferwallRedirectEnabled(TopfaceOfferwallRedirect topfaceOfferwallRedirect) {
        return topfaceOfferwallRedirect != null && topfaceOfferwallRedirect.isEnabled();
    }

    @Override
    protected boolean onPreFinish() {
        return !isScreenShow() && super.onPreFinish();
    }

    private boolean isScreenShow() {
        return showExtraScreen(getRandomPosByProbability(getListOfExtraScreens()));
    }

    private boolean callTrialVipPopup(DialogInterface.OnDismissListener dismissListener) {
        Profile profile = App.get().getProfile();
        if (getIntent().getIntExtra(App.INTENT_REQUEST_KEY, -1) == INTENT_BUY_VIP && App.isNeedShowTrial
                && !profile.premium && new GoogleMarketApiManager().isMarketApiAvailable()
                && App.get().getOptions().trialVipExperiment.enabled && !profile.paid) {
            //noinspection WrongConstant
            mTrialVipPopup = ExperimentBoilerplateFragment.newInstance(TrialVipPopupAction.getTrialVipType());
            mTrialVipPopup.setDismissListener(dismissListener);
            mTrialVipPopup.show(getSupportFragmentManager(), ExperimentBoilerplateFragment.TAG);
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
                finish();
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
                finish();
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
        //TODO
        /**
         * FATAL EXCEPTION: main
         Process: com.topface.topface, PID: 21584
         java.lang.IllegalArgumentException: n <= 0: -1
         at java.util.Random.nextInt(Random.java:182)
         at com.topface.topface.ui.PurchasesActivity.getRandomPosByProbability(PurchasesActivity.java:367)
         at com.topface.topface.ui.PurchasesActivity.isScreenShow(PurchasesActivity.java:315)
         at com.topface.topface.ui.PurchasesActivity.onPreFinish(PurchasesActivity.java:311)
         at com.topface.topface.ui.fragments.ToolbarActivity.doPreFinish(ToolbarActivity.kt:79)
         at com.topface.topface.ui.fragments.ToolbarActivity.onUpClick(ToolbarActivity.kt:71)
         at com.topface.topface.ui.PurchasesActivity.onUpClick(PurchasesActivity.java:342)
         at com.topface.topface.ui.fragments.ToolbarActivity.onUpButtonClick(ToolbarActivity.kt:67)
         at com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel$init$1$1.onClick(BaseToolbarViewModel.kt:33)
         at android.view.View.performClick(View.java:5204)
         at android.view.View$PerformClick.run(View.java:21153)
         at android.os.Handler.handleCallback(Handler.java:739)
         at android.os.Handler.dispatchMessage(Handler.java:95)
         at android.os.Looper.loop(Looper.java:148)
         at android.app.ActivityThread.main(ActivityThread.java:5417)
         at java.lang.reflect.Method.invoke(Native Method)
         at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726)
         at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616)
         */
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

    @NotNull
    @Override
    public ToolbarBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }

    @Nullable
    @Override
    public TabLayout getTabLayout() {
        return (TabLayout) getViewBinding().getRoot().findViewById(R.id.toolbarInternalTabs);
    }

    @Override
    public void showTabLayout(boolean show) {
        TabLayout tabLayout = getTabLayout();
        if (tabLayout != null) {
            tabLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
