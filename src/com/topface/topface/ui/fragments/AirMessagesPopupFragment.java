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
import com.topface.topface.requests.VisitorsMarkReadedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;

public class AirMessagesPopupFragment extends BaseFragment implements View.OnClickListener {

    public static final String AIR_TYPE = "AIR_TYPE";

    private Options.PremiumAirEntity mPremiumEntity;
    private boolean mUserClickButton = false;
    private int airType = Options.PremiumAirEntity.AIR_MESSAGES;

    public static AirMessagesPopupFragment newInstance(int airType) {
        Bundle arguments = new Bundle();
        arguments.putInt(AIR_TYPE, airType);
        AirMessagesPopupFragment fragment = new AirMessagesPopupFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            airType = getArguments().getInt(AIR_TYPE, Options.PremiumAirEntity.AIR_MESSAGES);
        }
        mPremiumEntity = airType == Options.PremiumAirEntity.AIR_MESSAGES?
                CacheProfile.getOptions().premium_messages : CacheProfile.getOptions().premium_visitors;

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
            EasyTracker.getTracker().sendEvent(getMainTag(airType), "Dismiss", "BackClose", 0L);
        }
    }

    public static boolean showIfNeeded(FragmentManager manager, int type) {
        Options.PremiumAirEntity premiumEntity;
        if (type == Options.PremiumAirEntity.AIR_MESSAGES) {
            premiumEntity = CacheProfile.getOptions().premium_messages;
        } else {
            premiumEntity = CacheProfile.getOptions().premium_visitors;
        }
        if (premiumEntity != null && premiumEntity.isNeedShow()) {
            manager
                    .beginTransaction()
                    .add(android.R.id.content, AirMessagesPopupFragment.newInstance(type))
                    .addToBackStack(null)
                    .commit();
            EasyTracker.getTracker().sendEvent(getMainTag(type), "Show", "", 0L);
            return true;
        }
        return false;
    }

    public static String getMainTag(int type) {
        return type == Options.PremiumAirEntity.AIR_MESSAGES? "AirMessages" : "key_7_1";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.air_messages_popup, container, false);
        root.findViewById(R.id.buyVip).setOnClickListener(this);
        if (airType == Options.PremiumAirEntity.AIR_GUESTS) {
            ((TextView)root.findViewById(R.id.deleteMessages)).setText(R.string.delete_visitors);
        } else {
            ((TextView)root.findViewById(R.id.deleteMessages)).setText(R.string.general_delete_messages);
        }
        root.findViewById(R.id.deleteMessages).setOnClickListener(this);
        TextView popupText = (TextView) root.findViewById(R.id.airMessagesText);
        int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
        CountersManager.getInstance(getActivity()).setCounter(CountersManager.VISITORS, curVisitCounter + mPremiumEntity.getCount(), true);
        popupText.setText(getMessage());
        return root;
    }

    private String getMessage() {
        int count = mPremiumEntity.getCount();
        if (airType == Options.PremiumAirEntity.AIR_GUESTS) {
            int guests = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
            count = guests > 0? guests:count;
        }
        return Utils.getQuantityString(getPluralForm(), count, count);
    }

    private int getPluralForm() {
        return airType == Options.PremiumAirEntity.AIR_MESSAGES? R.plurals.popup_vip_messages:R.plurals.popup_vip_visitors;
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
                EasyTracker.getTracker().sendEvent(getMainTag(airType), "ClickBuyVip", "", 0L);
                break;
            case R.id.deleteMessages:
                if (airType == Options.PremiumAirEntity.AIR_GUESTS) {
                    //Отправляем запрос удаления гостей
                    VisitorsMarkReadedRequest request = new VisitorsMarkReadedRequest(getActivity());
                    request.exec();
                    //Откручиваем счетчик назад
                    int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
                    CountersManager.getInstance(getActivity()).setCounter(CountersManager.VISITORS, curVisitCounter - mPremiumEntity.getCount(), true);
                }
                EasyTracker.getTracker().sendEvent(getMainTag(airType), "Dismiss", "Delete", 0L);
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
