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
        if (showPromoPopup(fm, Options.PremiumAirEntity.AIR_MESSAGES)) {
            return;
        } else if (showPromoPopup(fm, Options.PremiumAirEntity.AIR_VISITORS)) {
            return;
        } else if (showPromoPopup(fm, Options.PremiumAirEntity.AIR_ADMIRATIONS)) {
            return;
        }
    }

    public boolean showPromoPopup(FragmentManager fm, int type) {
        PromoPopupFragment promo = null;
        if (checkIsNeedShow(CacheProfile.getOptions().getPremiumEntityByType(type)) &&
                getLastFragmentType() != type) {
            mType = type;
            promo = getFragmentByType(type);
        }
        if (promo != null) {
            setLastFragmentType(type);

            fm.beginTransaction()
                    .add(android.R.id.content, promo)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    public boolean showPromoPopup(FragmentManager fm, int type, boolean doNeedSetLastType) {
        PromoPopupFragment promo = null;
        if (checkIsNeedShow(CacheProfile.getOptions().getPremiumEntityByType(type)) &&
                getLastFragmentType() != type) {
            mType = type;
            promo = getFragmentByType(type);
        }
        if (promo != null) {
            setLastFragmentType(Options.PremiumAirEntity.AIR_NONE);

            fm.beginTransaction()
                    .add(android.R.id.content, promo)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    private PromoPopupFragment getFragmentByType(int type) {
        switch (type) {
            case Options.PremiumAirEntity.AIR_ADMIRATIONS:
                return new PromoAdmirationsPopup();
            case Options.PremiumAirEntity.AIR_VISITORS:
                return new PromoVisitorsPopup();
            case Options.PremiumAirEntity.AIR_MESSAGES:
                return new AirMessagesFragment();
        }
        return null;
    }

    private boolean checkIsNeedShow(Options.PremiumAirEntity entity) {
        return entity != null && entity.isNeedShow();
    }

    public int getLastFragmentType() {
        SharedPreferences prefs = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return prefs.getInt(AIR_TYPE_LAST_TAG, Options.PremiumAirEntity.AIR_NONE);
    }

    public void setLastFragmentType(final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                prefs.edit().putInt(AIR_TYPE_LAST_TAG, type).commit();
            }
        }).start();
    }


}
