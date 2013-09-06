package com.topface.topface.ui.fragments;


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
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.profile.BlackListActivity;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import static android.view.View.OnClickListener;

public class VipBuyFragment extends BillingFragment implements OnClickListener {

    public static final String ACTION_BAR_CONST = "needActionBar";
    public static final String ARG_TAG_EXRA_TEXT = "extra_text";
    EditSwitcher mInvisSwitcher;

    ProgressBar mInvisLoadBar;

    private LinearLayout mBuyVipViewsContainer;
    private LinearLayout mEditPremiumContainer;


    /**
     * Создает новый инстанс фрагмента покупки VIP
     *
     * @param needActionBar включает показ ActionBar (он не нужен например в профиле
     * @param text          сопроводительный текст фрагмента (нужен например что-бы объяснить, что определнная функция только для VIP)
     * @param from          параметр для статистики покупок, что бы определить откуда пользователь пришел
     * @return Фрагмент покупки VIP
     */
    public static VipBuyFragment newInstance(boolean needActionBar, String text, String from) {
        VipBuyFragment fragment = new VipBuyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
        if (text != null) {
            args.putString(ARG_TAG_EXRA_TEXT, text);
        }
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy_premium, null);
        initViews(view);
        initActionBar(view);
        return view;
    }

    private void initActionBar(View view) {
        if (getArguments() != null && getArguments().getBoolean(ACTION_BAR_CONST, false)) {
            ActionBar actionBar = getActionBar(view);
            actionBar.showBackButton(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            actionBar.setTitleText(getString(R.string.vip_buy_vip));
        }
    }

    private void initViews(View root) {
        initExtraText(root);
        initBuyVipViews(root);
        initEditVipViews(root);
        switchLayouts();
    }

    private void initExtraText(View root) {
        TextView textView = (TextView) root.findViewById(R.id.tvExtraText);
        String text = null;
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TAG_EXRA_TEXT);
            textView.setText(text);
        }
        textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    private void switchLayouts() {
        if (mBuyVipViewsContainer != null && mEditPremiumContainer != null) {
            if (CacheProfile.premium) {
                mEditPremiumContainer.setVisibility(View.VISIBLE);
                mBuyVipViewsContainer.setVisibility(View.GONE);
            } else {
                mEditPremiumContainer.setVisibility(View.GONE);
                mBuyVipViewsContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initBuyVipViews(View root) {
        mBuyVipViewsContainer = (LinearLayout) root.findViewById(R.id.fbpContainer);
        LinearLayout btnContainer = (LinearLayout) root.findViewById(R.id.fbpBtnContainer);
        if (CacheProfile.getOptions().premium.isEmpty()) {
            root.findViewById(R.id.fbpBuyingDisabled).setVisibility(View.VISIBLE);
        } else {
            root.findViewById(R.id.fbpBuyingDisabled).setVisibility(View.GONE);
        }
        for (Options.BuyButton curBtn : CacheProfile.getOptions().premium) {
            Options.setButton(btnContainer, curBtn, getActivity(), new Options.BuyButtonClickListener() {
                @Override
                public void onClick(String id) {
                    buySubscription(id);
                    Bundle arguments = getArguments();
                    String from = "";
                    if (arguments != null) {
                        from = "From" + arguments.getString(ARG_TAG_SOURCE);
                    }
                    EasyTracker.getTracker().trackEvent("Subscription", "ButtonClick" + from, id, 0L);
                }
            });
        }
    }

    private void initEditVipViews(View root) {
        mEditPremiumContainer = (LinearLayout) root.findViewById(R.id.editPremiumContainer);
        ImageButton editVip = (ImageButton) root.findViewById(R.id.fepVipEdit);
        editVip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editPremium();
            }
        });

        RelativeLayout invisLayout =
                initEditItem(root,
                        R.id.fepInvis,
                        R.drawable.edit_big_btn_selector,
                        R.drawable.ic_vip_invisible_min,
                        getString(R.string.vip_invis),
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setVisibility();
                            }
                        });
        mInvisSwitcher = new EditSwitcher(invisLayout);
        mInvisLoadBar = (ProgressBar) invisLayout.findViewWithTag("vsiLoadBar");
        mInvisSwitcher.setChecked(CacheProfile.invisible);

        initEditItem(root,
                R.id.fepBlackList,
                R.drawable.edit_big_btn_top_selector,
                R.drawable.ic_vip_blacklist_min,
                getString(R.string.vip_black_list),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToBlackList();
                    }
                });

        initEditItem(root,
                R.id.fepProfileBG,
                R.drawable.edit_big_btn_bottom_selector,
                R.drawable.ic_vip_profile_bg,
                getString(R.string.vip_profile_bg),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToBgPick();
                    }
                });
    }

    private RelativeLayout initEditItem(View root, int ID, int bgId, int bgLeftId, String text, OnClickListener listener) {
        RelativeLayout layout = initLayouts(root, ID, bgId, bgLeftId, text);
        layout.setOnClickListener(listener);
        return layout;
    }

    private RelativeLayout initLayouts(View root, int ID, int bgId, int bgLeftId, String text) {
        RelativeLayout layout = (RelativeLayout) root.findViewById(ID);

        TextView layoutText = (TextView) layout.findViewWithTag("tvTitle");
        layoutText.setText(text);
        layout.setBackgroundResource(bgId);
        layoutText.setCompoundDrawablesWithIntrinsicBounds(bgLeftId, 0, 0, 0);
        return layout;
    }

    private void editPremium() {
        Utils.goToMarket(getActivity());
    }

    private void setVisibility() {
        mInvisSwitcher.doSwitch();
        mInvisSwitcher.setVisibility(View.GONE);
        mInvisLoadBar.setVisibility(View.VISIBLE);

        SettingsRequest request = new SettingsRequest(getActivity());
        request.invisible = mInvisSwitcher.isChecked();
        registerRequest(request);

        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) throws NullPointerException {
                CacheProfile.invisible = mInvisSwitcher.isChecked();
                if (mInvisLoadBar != null && getActivity() != null) {
                    mInvisLoadBar.setVisibility(View.GONE);
                    mInvisSwitcher.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) throws NullPointerException {
                if (mInvisSwitcher != null && getActivity() != null) {
                    if (CacheProfile.invisible != mInvisSwitcher.isChecked()) {
                        mInvisSwitcher.doSwitch();
                        mInvisLoadBar.setVisibility(View.GONE);
                        mInvisSwitcher.setVisibility(View.VISIBLE);

                    }
                }
            }
        }).exec();
    }

    private void goToBlackList() {
        Intent intent = new Intent(getActivity(), BlackListActivity.class);
        startActivity(intent);
    }

    private void goToBgPick() {
        Intent intent = new Intent(getActivity(), EditContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, EditContainerActivity.INTENT_EDIT_BACKGROUND);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onSubscritionSupported() {
    }

    @Override
    public void onSubscritionUnsupported() {
        //Если подписка не поддерживается, сообщаем об этом пользователю
        if (!CacheProfile.premium) {
            Toast.makeText(App.getContext(), R.string.buy_play_market_not_available, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onInAppBillingSupported() {
    }

    @Override
    public void onInAppBillingUnsupported() {
    }

    @Override
    public void onPurchased() {
        switchLayouts();
    }

    @Override
    public void onError() {
    }

    @Override
    public void onCancel() {
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switchLayouts();
        }
    };
}
