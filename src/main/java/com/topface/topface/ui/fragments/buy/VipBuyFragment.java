package com.topface.topface.ui.fragments.buy;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.billing.OpenIabFragment;
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics;
import com.topface.statistics.processor.utils.RxUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.BuyScreenStatistics;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.statistics.PushButtonVipStatistics;
import com.topface.topface.statistics.PushButtonVipUniqueStatistics;
import com.topface.topface.ui.BlackListActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.external_libs.ironSource.IronSourceManager;
import com.topface.topface.ui.external_libs.ironSource.IronSourceOfferwallEvent;
import com.topface.topface.ui.external_libs.ironSource.IronSourceStatistics;
import com.topface.topface.ui.views.BuyButtonVer1;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

import org.jetbrains.annotations.Nullable;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.view.View.OnClickListener;

@FlurryOpenEvent(name = VipBuyFragment.PAGE_NAME)
public class VipBuyFragment extends OpenIabFragment implements OnClickListener {

    public static final String VIP_PURCHASED_INTENT = "com.topface.topface.VIP_PURCHASED";

    public static final String PAGE_NAME = "buy.vip.gp";

    EditSwitcher mInvisSwitcher;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switchLayouts();
            if (mInvisSwitcher != null) {
                mInvisSwitcher.setChecked(App.from(context).getProfile().invisible);
            }
        }
    };
    private LinearLayout mBuyVipViewsContainer;
    private LinearLayout mEditPremiumContainer;
    private IronSourceManager mIronSourceManager = App.getAppComponent().ironSourceManager();
    private BuyButtonVer1 mOfferwallBtn;
    private TextView mResourceInfo;
    private String mResourceInfoText;
    private Subscription mIronsrcSubscription;
    private Boolean mIsNeedOfferwall = !App.get().getOptions()
            .getOfferwallWithPlaces().getPurchaseScreenVip().isEmpty()
            && App.get().getOptions().getOfferwallWithPlaces()
            .getName().equalsIgnoreCase(IronSourceManager.NAME);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                getDataFromIntent(intent.getExtras());
            }
        }
    };
    private Subscription mVipOpenSubscription = null;

    private void getDataFromIntent(Bundle args) {
        if (args != null) {
            if (args.containsKey(PurchasesConstants.ARG_RESOURCE_INFO_TEXT)) {
                mResourceInfoText = args.getString(PurchasesConstants.ARG_RESOURCE_INFO_TEXT);
                setResourceInfoText();
            }
        }
    }

    /**
     * Создает новый инстанс фрагмента покупки VIP
     *
     * @param needActionBar включает показ ActionBar (он не нужен например в профиле
     * @param from          параметр для статистики покупок, что бы определить откуда пользователь пришел
     * @return Фрагмент покупки VIP
     */
    public static VipBuyFragment newInstance(boolean needActionBar, String from, String text) {
        VipBuyFragment fragment = new VipBuyFragment();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(text)) {
            args.putString(PurchasesConstants.ARG_RESOURCE_INFO_TEXT, text);
        }
        args.putBoolean(PurchasesConstants.ACTION_BAR_CONST, needActionBar);
        if (from != null) {
            args.putString(PurchasesConstants.ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIronsrcSubscription = mIronSourceManager.getOfferwallObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new com.topface.topface.utils.rx.RxUtils.ShortSubscription<IronSourceOfferwallEvent>() {
                    @Override
                    public void onNext(IronSourceOfferwallEvent type) {
                        super.onNext(type);
                        long OfferwallType = type.getType();
                        if (OfferwallType == IronSourceOfferwallEvent.OFFERWALL_CLOSED || OfferwallType == IronSourceOfferwallEvent.OFFERWALL_OPENED) {
                            if (mOfferwallBtn != null) {
                                mOfferwallBtn.stopWaiting();
                            }
                        }
                    }
                });
        getDataFromIntent(getArguments());
        if (mIsNeedOfferwall) {
            mIronSourceManager.initSdk(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mIronSourceManager.onResume(getActivity());
        mVipOpenSubscription = NewProductsKeysGeneratedStatistics.sendPost_VIP_OPEN(getActivity().getApplicationContext());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(OpenIabFragment.UPDATE_RESOURCE_INFO));
        mInvisSwitcher.setProgressState(false, App.from(getActivity()).getProfile().invisible);
        switchLayouts();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIronSourceManager.onPause(getActivity());
        RxUtils.safeUnsubscribe(mVipOpenSubscription);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.safeUnsubscribe(mIronsrcSubscription);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_buy_premium, null);
        mResourceInfo = (TextView) view.findViewById(R.id.payReasonFragmentBuyPremium);
        setResourceInfoText();
        initViews(view);
        return view;
    }

    private void initViews(View root) {
        initBuyVipViews(root);
        initEditVipViews(root);
        initOfferwallButton(root);
        switchLayouts();
    }

    private void switchLayouts() {
        if (App.get().getProfile().premium) {
            setViewVisibility(mEditPremiumContainer, true);
            setViewVisibility(mBuyVipViewsContainer, false);
        } else {
            setViewVisibility(mEditPremiumContainer, false);
            setViewVisibility(mBuyVipViewsContainer, true);
        }
    }

    private boolean setViewVisibility(View view, boolean isVisible) {
        if (view != null) {
            view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            return true;
        }
        return false;
    }

    @Nullable
    private String getBuyVipViewVersion(@Nullable Products products, String defaultValue) {
        String version = defaultValue;
        if (products != null && products.info != null && products.info.views != null) {
            version = products.info.views.buyVip;
        }
        return version;
    }

    @Nullable
    private String getBuyVipViewVersion(String defaultValue) {
        return getBuyVipViewVersion(getProducts(), defaultValue);
    }

    private void initBuyVipViews(View root) {
        mBuyVipViewsContainer = (LinearLayout) root.findViewById(R.id.fbpContainer);
        LinearLayout btnContainer = (LinearLayout) root.findViewById(R.id.fbpBtnContainer);
        Products products = getProducts();
        if (products == null) {
            return;
        }
        List<BuyButtonData> availableButtons = getAvailableButtons(products.premium);
        root.findViewById(R.id.fbpBuyingDisabled).setVisibility(availableButtons.isEmpty() ? View.VISIBLE : View.GONE);

        new PurchaseButtonList().getButtonsListView(btnContainer, availableButtons, App.getContext(), new PurchaseButtonList.BuyButtonClickListener() {
            @Override
            public void onClick(String id, BuyButtonData btnData) {
                buy(id, btnData);
            }
        });
    }

    private void initOfferwallButton(View root) {
        if (mIsNeedOfferwall) {
            final LinearLayout btnContainer = (LinearLayout) root.findViewById(R.id.fbpBtnContainer);
            mOfferwallBtn = new BuyButtonVer1.BuyButtonBuilder().discount(false)
                    .tag("offerWall_button_tag")
                    .showType(3).title(getResources().getString(R.string.get_free))
                    .onClick(null).build(getContext());
            mOfferwallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIronSourceManager.emmitNewState(IronSourceOfferwallEvent.Companion.getOnOfferwallCall());
                    mIronSourceManager.showOfferwallByType(IronSourceManager.VIP_OFFERWALL, IronSourceStatistics.BUY_VIP_PLC);
                    mOfferwallBtn.startWaiting();
                }
            });
            btnContainer.addView(mOfferwallBtn);
        }
    }

    @Override
    public void onResumeFragment() {
        super.onResumeFragment();
        BuyScreenStatistics.buyScreenShowSendStatistics(getClass().getSimpleName());
    }

    protected void buy(String id, BuyButtonData curBtn) {
        buy(curBtn);
        PushButtonVipUniqueStatistics.sendPushButtonVip(id, ((Object) this).getClass().getSimpleName(), getFrom(), App.from(getActivity()).getProfile());
        PushButtonVipStatistics.send(id, ((Object) this).getClass().getSimpleName(), getFrom());
        EasyTracker.sendEvent("Subscription", "ButtonClick" + getFrom(), id, 0L);
    }

    protected Products getProducts() {
        return CacheProfile.getMarketProducts();
    }

    private void initEditVipViews(View root) {
        mEditPremiumContainer = (LinearLayout) root.findViewById(R.id.editPremiumContainer);

        RelativeLayout invisLayout =
                initEditItem(root,
                        R.id.fepInvis,
                        getString(R.string.vip_invis),
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setInvisible();
                            }
                        }
                );
        mInvisSwitcher = new EditSwitcher(invisLayout);

        initEditItem(root,
                R.id.fepBlackList,
                getString(R.string.vip_black_list),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToBlackList();
                    }
                }
        );
    }

    private RelativeLayout initEditItem(View root, int ID, String text, OnClickListener listener) {
        RelativeLayout layout = initLayouts(root, ID, text);
        layout.setOnClickListener(listener);
        return layout;
    }

    private RelativeLayout initLayouts(View root, int ID, String text) {
        RelativeLayout layout = (RelativeLayout) root.findViewById(ID);

        TextView layoutText = (TextView) layout.findViewWithTag("tvTitle");
        if (layoutText != null) {
            layoutText.setText(text);
        }
        return layout;
    }

    private void setInvisible() {
        SettingsRequest request = new SettingsRequest(getActivity());
        final boolean invisibility = !mInvisSwitcher.isChecked();
        request.invisible = invisibility;
        mInvisSwitcher.setProgressState(true);
        final Profile profile = App.from(getActivity()).getProfile();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) throws NullPointerException {
                profile.invisible = invisibility;
                CacheProfile.sendUpdateProfileBroadcast();
            }

            @Override
            public void fail(int codeError, IApiResponse response) throws NullPointerException {
                if (mInvisSwitcher != null && getActivity() != null) {
                    if (profile.invisible != mInvisSwitcher.isChecked()) {
                        mInvisSwitcher.setChecked(profile.invisible);
                    }
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (mInvisSwitcher != null && getActivity() != null) {
                    mInvisSwitcher.setProgressState(false, profile.invisible);
                }
            }
        }).exec();
    }

    private void goToBlackList() {
        Intent intent = new Intent(getActivity(), BlackListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onSubscriptionSupported() {
    }

    @Override
    public void onSubscriptionUnsupported() {
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.fbpBuyingDisabled).setVisibility(View.VISIBLE);
            view.findViewById(R.id.fbpBtnContainer).setVisibility(View.GONE);
        }
    }

    @Override
    public void onInAppBillingSupported() {
    }

    @Override
    public void onInAppBillingUnsupported() {

    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
        switchLayouts();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(VIP_PURCHASED_INTENT));
    }

    private void setResourceInfoText() {
        if (mResourceInfo != null) {
            mResourceInfo.setText(mResourceInfoText);
            mResourceInfo.setVisibility(TextUtils.isEmpty(mResourceInfoText) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected boolean needOptionsMenu() {
        return false;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    public String getFrom() {
        Bundle arguments = getArguments();
        String from = "";
        if (arguments != null) {
            from = "From" + arguments.getString(PurchasesConstants.ARG_TAG_SOURCE);
        }
        return from;
    }
}
