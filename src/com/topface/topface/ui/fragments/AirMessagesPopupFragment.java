package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class AirMessagesPopupFragment extends BaseFragment implements View.OnClickListener {

    private Options.PremiumMessages mPremiumMessages;
    private boolean mUserClickButton = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPremiumMessages = CacheProfile.getOptions().premium_messages;
        setNeedTitles(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Отключаем боковое меню
        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Отмечаем время закрытия попапа
        CacheProfile.getOptions().premium_messages.setPopupShowTime();

        //Включаем боковое меню
        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(false);
        }

        if (!mUserClickButton) {
            EasyTracker.getTracker().trackEvent("AirMessages", "Dismiss", "BackClose", 0L);
        }
    }

    public static void showIfNeeded(FragmentManager manager) {
        Options.PremiumMessages options = CacheProfile.getOptions().premium_messages;
        if (options != null && options.isNeedShow()) {
            manager
                    .beginTransaction()
                    .add(android.R.id.content, new AirMessagesPopupFragment())
                    .addToBackStack(null)
                    .commit();
            EasyTracker.getTracker().trackEvent("AirMessages", "Show", "", 0L);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.air_messages_popup, container, false);
        root.findViewById(R.id.buyVip).setOnClickListener(this);
        root.findViewById(R.id.deleteMessages).setOnClickListener(this);
        TextView popupText = (TextView) root.findViewById(R.id.airMessagesText);
        popupText.setText(getMessage());
        return root;
    }

    private String getMessage() {
        int count = mPremiumMessages.getCount();
        return Utils.getQuantityString(R.plurals.popup_vip_messages, count, count);
    }

    @Override
    public void onClick(View v) {
        mUserClickButton = true;

        switch (v.getId()) {
            case R.id.buyVip:
                startActivityForResult(
                        ContainerActivity.getVipBuyIntent(getMessage(), "VipDelivery"),
                        ContainerActivity.INTENT_BUY_VIP_FRAGMENT
                );
                EasyTracker.getTracker().trackEvent("AirMessages", "ClickBuyVip", "", 0L);
                break;
            case R.id.deleteMessages:
                EasyTracker.getTracker().trackEvent("AirMessages", "Dismiss", "Delete", 0L);
                break;
        }

        closeFragment();
    }

    private void closeFragment() {
        getFragmentManager()
                .beginTransaction()
                .remove(AirMessagesPopupFragment.this)
                .commit();
    }

}
