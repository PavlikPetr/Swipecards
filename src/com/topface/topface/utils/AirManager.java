package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.promo.AirMessagesFragment;
import com.topface.topface.ui.fragments.promo.PromoAdmirationsPopup;
import com.topface.topface.ui.fragments.promo.PromoPopupFragment;
import com.topface.topface.ui.fragments.promo.PromoVisitorsPopup;

public class AirManager {
    public static final String AIR_TYPE_LAST_TAG = "air_type_last_tag";
    private final Context mContext;
    private int mType;

    public AirManager(Context context) {
        mContext = context;
    }

    public void startFragment(FragmentManager fm) {
        int lastFragmentType = getLastFragmentType();
        PromoPopupFragment promoPopup;


        Options.PremiumAirEntity premiumMessages = CacheProfile.getOptions().premium_messages;
        Options.PremiumAirEntity premiumVisitors = CacheProfile.getOptions().premium_visitors;
        Options.PremiumAirEntity premiumAdmirations = CacheProfile.getOptions().premium_admirations;
        //Проверяем можем ли мы показать какой-нибудь попап и не был ли он показан последним.
        //Если ничего не можем показать, ничего и не показываем.
        if (checkIsNeedShow(premiumMessages) &&
                lastFragmentType != Options.PremiumAirEntity.AIR_MESSAGES) {

            mType = Options.PremiumAirEntity.AIR_MESSAGES;
            promoPopup = new AirMessagesFragment();
        } else if (checkIsNeedShow(premiumVisitors) &&
                lastFragmentType != Options.PremiumAirEntity.AIR_VISITORS) {

            mType = Options.PremiumAirEntity.AIR_VISITORS;
            promoPopup = new PromoVisitorsPopup();
        } else if (checkIsNeedShow(premiumAdmirations) &&
                lastFragmentType != Options.PremiumAirEntity.AIR_ADMIRATIONS) {

            mType = Options.PremiumAirEntity.AIR_ADMIRATIONS;
            promoPopup = new PromoAdmirationsPopup();
        } else {
            return;
        }

        setLastFragmentType();

        fm.beginTransaction()
            .add(android.R.id.content, promoPopup)
            .addToBackStack(null)
            .commit();
    }

    private boolean checkIsNeedShow(Options.PremiumAirEntity entity) {
        return entity != null && entity.isNeedShow();
    }

    public int getLastFragmentType() {
        SharedPreferences prefs = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return prefs.getInt(AIR_TYPE_LAST_TAG, Options.PremiumAirEntity.AIR_NONE);
    }

    public void setLastFragmentType() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                prefs.edit().putInt(AIR_TYPE_LAST_TAG, mType).commit();
            }
        }).start();
    }
}
