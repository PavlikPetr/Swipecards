package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class AirMessagesPopupFragment extends BaseFragment implements View.OnClickListener {

    private Options.PremiumMessages mPremiumMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPremiumMessages = CacheProfile.getOptions().premium_messages;
    }

    public static void showIfNeeded(FragmentManager manager) {
        Options.PremiumMessages options = CacheProfile.getOptions().premium_messages;
        if (options != null && options.isNeedShow()) {
            manager
                    .beginTransaction()
                    .add(android.R.id.content, new AirMessagesPopupFragment())
                    .commit();
            EasyTracker.getTracker().trackEvent("AirMessages", "Show", "", 0L);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.air_messages_popup, container, false);
        root.findViewById(R.id.buyVip).setOnClickListener(this);
        root.findViewById(R.id.deleteMessages).setOnClickListener(this);
        root.findViewById(R.id.closePopup).setOnClickListener(this);
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
        switch (v.getId()) {
            case R.id.buyVip:
                startActivityForResult(ContainerActivity.getVipBuyIntent(getMessage(), "VipDelivery"), ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                EasyTracker.getTracker().trackEvent("AirMessages", "ClickBuyVip", "", 0L);
                break;
            case R.id.deleteMessages:
            case R.id.closePopup:
                EasyTracker.getTracker().trackEvent("AirMessages", "Dismiss", v.getId() == R.id.deleteMessages ? "Delete" : "Close", 0L);
                getFragmentManager()
                        .beginTransaction()
                        .remove(AirMessagesPopupFragment.this)
                        .commit();
                CacheProfile.getOptions().premium_messages.setPopupShowTime();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
