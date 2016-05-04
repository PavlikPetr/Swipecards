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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.billing.IFreePurchases;
import com.topface.billing.OpenIabFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BlackListActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.utils.CacheProfile;

import java.util.LinkedList;

import static android.view.View.OnClickListener;

public class IFreeBuyVipFragment extends IFreePurchases implements OnClickListener {
    public final static String TAG = "VipBuyFragment";

    public static final String VIP_PURCHASED_INTENT = "com.topface.topface.VIP_PURCHASED";
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
    private TextView mResourceInfo;
    private View mRoot;
    private ProgressBar mProgress;
    private String mResourceInfoText;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                getDataFromIntent(intent.getExtras());
            }
        }
    };

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
    public static IFreeBuyVipFragment newInstance(boolean needActionBar, String from, String text) {
        IFreeBuyVipFragment fragment = new IFreeBuyVipFragment();
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
        setNeedTitles(false);
        getDataFromIntent(getArguments());

    }

    @Override
    public void onLibraryInitialised() {
        setProgressVisibility(false);
        if (!isLibraryInitialised()) {
            Products products = CacheProfile.getMarketProducts();
            if (!App.from(getActivity()).getProfile().premium) {
                initBuyVipViews(mRoot, products != null ? validateProducts(products.premium) : null);
            }
        }
        super.onLibraryInitialised();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(OpenIabFragment.UPDATE_RESOURCE_INFO));
        mInvisSwitcher.setProgressState(false, App.from(getActivity()).getProfile().invisible);
        switchLayouts();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_buy_premium, null);
        mRoot = view;
        mProgress = (ProgressBar) view.findViewById(R.id.purchases_progress_bar);
        view.findViewById(R.id.EditorTestStub).setVisibility(View.GONE);
        mResourceInfo = (TextView) view.findViewById(R.id.payReasonFragmentBuyPremium);
        setResourceInfoText();
        initViews(view);
        initActionBar();
        if (!App.from(getActivity()).getProfile().premium) {
            setProgressVisibility(true);
        }
        return view;
    }

    private void setProgressVisibility(boolean isVisible) {
        if (mProgress != null) {
            mProgress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        if (isVisible) {
            mRoot.findViewById(R.id.fbpBtnContainer).setVisibility(View.GONE);
        }
    }

    private void initActionBar() {
        if (getArguments() != null && getArguments().getBoolean(PurchasesConstants.ACTION_BAR_CONST, false)) {
            setActionBarTitles(R.string.vip_buy_vip);
        }
    }

    private void initViews(View root) {
        mBuyVipViewsContainer = (LinearLayout) root.findViewById(R.id.fbpContainer);
        initEditVipViews(root);
        switchLayouts();
    }

    private void switchLayouts() {
        if (mBuyVipViewsContainer != null && mEditPremiumContainer != null) {
            if (App.from(getActivity()).getProfile().premium) {
                mEditPremiumContainer.setVisibility(View.VISIBLE);
                mBuyVipViewsContainer.setVisibility(View.GONE);
            } else {
                mEditPremiumContainer.setVisibility(View.GONE);
                mBuyVipViewsContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initBuyVipViews(View root, LinkedList<BuyButtonData> premium) {
        if (premium == null || premium.isEmpty()) {
            root.findViewById(R.id.fbpBuyingDisabled).setVisibility(View.VISIBLE);
            return;
        }
        root.findViewById(R.id.fbpBuyingDisabled).setVisibility(View.GONE);
        LinearLayout btnContainer = (LinearLayout) root.findViewById(R.id.fbpBtnContainer);
        btnContainer.setVisibility(View.VISIBLE);
        if (btnContainer.getChildCount() > 0) {
            btnContainer.removeAllViews();
        }
        new PurchaseButtonList().getButtonsListView(null, btnContainer, premium, App.getContext(), new PurchaseButtonList.BuyButtonClickListener() {
            @Override
            public void onClick(String id, BuyButtonData btnData) {
                buyProduct(id, getFrom());
            }
        });
    }

    protected Products getProducts() {
        return CacheProfile.getMarketProducts();
    }

    private void initEditVipViews(View root) {
        mEditPremiumContainer = (LinearLayout) root.findViewById(R.id.editPremiumContainer);

        RelativeLayout invisLayout =
                initEditItem(root,
                        R.id.fepInvis,
                        R.drawable.list_like_btn_normal,
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
                R.drawable.list_item_btn,
                getString(R.string.vip_black_list),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToBlackList();
                    }
                }
        );
    }

    private RelativeLayout initEditItem(View root, int ID, int bgId, String text, OnClickListener listener) {
        RelativeLayout layout = initLayouts(root, ID, bgId, text);
        layout.setOnClickListener(listener);
        return layout;
    }

    private RelativeLayout initLayouts(View root, int ID, int bgId, String text) {
        RelativeLayout layout = (RelativeLayout) root.findViewById(ID);

        TextView layoutText = (TextView) layout.findViewWithTag("tvTitle");
        if (layoutText != null) {
            layoutText.setText(text);
            layout.setBackgroundResource(bgId);
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