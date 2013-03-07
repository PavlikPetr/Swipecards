package com.topface.topface.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.billing.BillingFragment;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiResponse;
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
    EditSwitcher mInvisSwitcher;

    ProgressBar mInvisLoadBar;

    private LinearLayout mBuyVipViewsContainer;
    private LinearLayout mEditPremiumContainer;

    // В этот метод потом можно будет передать аргументы,
    // чтобы потом установить их с помощью setArguments();
    public static VipBuyFragment newInstance() {
        return new VipBuyFragment();
    }

    public static VipBuyFragment newInstance(boolean needActionBar) {
        VipBuyFragment fragment = new VipBuyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
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
        if( getArguments() != null && getArguments().getBoolean(ACTION_BAR_CONST, false) ) {
            view.findViewById(R.id.navBar).setVisibility(View.VISIBLE);
            view.findViewById(R.id.headerShadow).setVisibility(View.VISIBLE);
            ActionBar actionBar = getActionBar(view);
            actionBar.showBackButton(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            actionBar.setTitleText(getString(R.string.vip_buy_vip));
        }
        return view;
    }

    private void initViews(View root) {
        initBuyVipViews(root);
        initEditVipViews(root);
        switchLayouts();
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
                    buySubscriotion(id);
                    EasyTracker.getTracker().trackEvent("Subscription", "ButtonClick", id, 0L);
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
        mInvisLoadBar = (ProgressBar) invisLayout.findViewById(R.id.vsiLoadBar);
        mInvisSwitcher.setChecked(CacheProfile.invisible);

//  Здесь работа с переключателем отображения VIP бэкграунда в элементах ленты,
//  так как пока решили его не использовать, из основного layouta он был удален.
//  View.gone ему нельзя было сделать, так как он был подключен с помощью include
//
//        RelativeLayout bgSwitchLayout =
//                initEditItem(root,
//                    R.id.fepMsgsBG,
//                    R.drawable.edit_big_btn_bottom_selector,
//                    R.drawable.ic_vip_message_bg_min,
//                    getString(R.string.vip_messages_bg),
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                        }
//                    }
//                    );
//        mBgSwitcher = new EditSwitcher(bgSwitchLayout);
//        mBgSwitcher.setChecked(false);

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

        TextView layoutText = (TextView) layout.findViewById(R.id.tvTitle);
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
            public void success(ApiResponse response) throws NullPointerException {
                if (mInvisLoadBar != null && getActivity() != null) {
                    CacheProfile.invisible = mInvisSwitcher.isChecked();
                    mInvisLoadBar.setVisibility(View.GONE);
                    mInvisSwitcher.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                if (mInvisSwitcher != null && getActivity() != null) {
                    if (CacheProfile.invisible != mInvisSwitcher.isChecked()) {
                        //TODO: Нужно как-то оповещать пользователя, что не получилось
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
        //Подписки на премиумы
//        switch (v.getId()) {
//            case R.id.fbpBuyingMonth:
//                buySubscriotion("topface.premium.month.1");
//                EasyTracker.getTracker().trackEvent("Subscription", "ButtonClick", "Month", 0L);
//                break;
//
//            case R.id.fbpBuyingYear:
//                buySubscriotion("topface.premium.year.1");
//                EasyTracker.getTracker().trackEvent("Subscription", "ButtonClick", "Year", 0L);
//                break;
//
//        }
    }

    @Override
    public void onSubscritionSupported() {
    }

    @Override
    public void onSubscritionUnsupported() {
        //Если подписка не поддерживается, сообщаем об этом пользователю
        if (!CacheProfile.premium) {
            Toast.makeText(getActivity(), R.string.buy_play_market_not_available, Toast.LENGTH_SHORT)
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
        //TODO: сделать обработку ошибок
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
