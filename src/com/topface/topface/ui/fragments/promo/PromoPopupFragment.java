package com.topface.topface.ui.fragments.promo;

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
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;

public abstract class PromoPopupFragment extends BaseFragment implements View.OnClickListener {

    public static final String AIR_TYPE = "AIR_TYPE";

    private Options.PremiumAirEntity mPremiumEntity;
    private boolean mUserClickButton = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPremiumEntity = getPremiumEntity();
    }

    public abstract Options.PremiumAirEntity getPremiumEntity();

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
            EasyTracker.getTracker().sendEvent(getMainTag(), "Dismiss", "BackClose", 0L);
        }
    }

    public abstract String getMainTag();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.air_messages_popup, container, false);
        root.findViewById(R.id.buyVip).setOnClickListener(this);
        ((TextView)root.findViewById(R.id.deleteMessages)).setText(getDeleteButtonText());
        root.findViewById(R.id.deleteMessages).setOnClickListener(this);
        TextView popupText = (TextView) root.findViewById(R.id.airMessagesText);
        int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
        CountersManager.getInstance(getActivity()).setCounter(CountersManager.VISITORS, curVisitCounter + mPremiumEntity.getCount(), true);
        popupText.setText(getMessage());
        EasyTracker.getTracker().sendEvent(getMainTag(), "Show", "", 0L);
        return root;
    }

    //Здесь нужно возращать айди строки в ресурсах
    protected abstract int getDeleteButtonText();

    protected abstract String getMessage();

    protected abstract int getPluralForm();

    @Override
    public void onClick(View v) {
        mUserClickButton = true;

        switch (v.getId()) {
            case R.id.buyVip:
                startActivityForResult(
                        ContainerActivity.getVipBuyIntent(getMessage(), getTagForBuyingFragment()),
                        ContainerActivity.INTENT_BUY_VIP_FRAGMENT
                );
                EasyTracker.getTracker().sendEvent(getMainTag(), "ClickBuyVip", "", 0L);
                break;
            case R.id.deleteMessages:
                deleteMessages();
                EasyTracker.getTracker().sendEvent(getMainTag(), "Dismiss", "Delete", 0L);
                break;
        }

        closeFragment();
    }

    protected abstract void deleteMessages();

    protected String getTagForBuyingFragment() {
        return getMainTag();
    }

    private void closeFragment() {
        getFragmentManager()
                .beginTransaction()
                .remove(PromoPopupFragment.this)
                .commit();
    }

}
