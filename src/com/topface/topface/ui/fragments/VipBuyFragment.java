package com.topface.topface.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BillingService;
import com.topface.topface.billing.Consts;
import com.topface.topface.billing.PurchaseObserver;
import com.topface.topface.billing.ResponseHandler;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.profile.BlackListActivity;
import com.topface.topface.utils.CacheProfile;

import static android.view.View.OnClickListener;

public class VipBuyFragment extends BaseFragment implements OnClickListener {

    EditSwitcher mInvisSwitcher;
    EditSwitcher mBgSwitcher;

    ProgressBar mInvisLoadBar;
    private BillingService mBillingService;

    public static final String BROADCAST_PURCHASE_ACTION = "com.topface.topface.PURCHASE_NOTIFICATION";

    // В этот метод потом можно будет передать аргументы,
    // чтобы потом установить их с помощью setArguments();
    public static VipBuyFragment newInstance() {
        return new VipBuyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ResponseHandler.register(new VipPurchaseObserver(new Handler()));

        mBillingService = new BillingService();
        mBillingService.setContext(getActivity());

        if (!mBillingService.checkBillingSupported(Consts.ITEM_TYPE_INAPP)) {
            Toast.makeText(getActivity(), R.string.buy_play_market_not_available, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mBillingService.unbind();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        if (CacheProfile.premium) {
            root = inflater.inflate(R.layout.fragment_edit_premium, null);
            initEditVipViews(root);
        } else {
            root = inflater.inflate(R.layout.fragment_buy_premium, null);
            initBuyVipViews(root);
        }
        return root;
    }

    private void initBuyVipViews(View root) {
        root.findViewById(R.id.fbpBuyingMonth)
                .setOnClickListener(this);
        root.findViewById(R.id.fbpBuyingYear)
                .setOnClickListener(this);
    }

    private void initEditVipViews(View root) {
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
                        R.drawable.edit_big_btn_top_selector,
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
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.default_market_link)));
        startActivity(i);
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CacheProfile.invisible = mInvisSwitcher.isChecked();
                            mInvisLoadBar.setVisibility(View.GONE);
                            mInvisSwitcher.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                if (mInvisSwitcher != null && getActivity() != null) {
                    if (CacheProfile.invisible != mInvisSwitcher.isChecked()) {
                        //TODO: Нужно как-то оповещать пользователя, что не получилось
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mInvisSwitcher.doSwitch();
                                mInvisLoadBar.setVisibility(View.GONE);
                                mInvisSwitcher.setVisibility(View.VISIBLE);
                            }
                        });

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
        switch (v.getId()) {
            case R.id.fbpBuyingMonth:
                mBillingService.requestPurchase("topface.premium.month.1", Consts.ITEM_TYPE_SUBSCRIPTION, null);
                break;

            case R.id.fbpBuyingYear:
                mBillingService.requestPurchase("topface.premium.year.1", Consts.ITEM_TYPE_SUBSCRIPTION, null);
//              mBillingService.requestPurchase("android.test.purchased", Consts.ITEM_TYPE_SUBSCRIPTION, null); //topface.premium.month.test
                break;

        }
    }

    private class VipPurchaseObserver extends PurchaseObserver {
        public VipPurchaseObserver(Handler handler) {
            super(getActivity(), handler);
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {

        }

        @Override
        public void onPurchaseStateChange(Consts.PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload, String signedData, String signature) {}

        @Override
        public void onRequestPurchaseResponse(BillingService.RequestPurchase request, Consts.ResponseCode responseCode) {}

        @Override
        public void onRestoreTransactionsResponse(BillingService.RestoreTransactions request, Consts.ResponseCode responseCode) {}
    }
}
