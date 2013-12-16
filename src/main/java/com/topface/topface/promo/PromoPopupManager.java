package com.topface.topface.promo;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.topface.topface.data.Options;
import com.topface.topface.promo.fragments.PromoFragment;
import com.topface.topface.promo.fragments.PromoKey31Fragment;
import com.topface.topface.promo.fragments.PromoKey71Fragment;
import com.topface.topface.promo.fragments.PromoKey81Fragment;
import com.topface.topface.utils.CacheProfile;

public class PromoPopupManager {
    public static boolean needShowPopup = true;
    private final FragmentActivity mActivity;

    public PromoPopupManager(FragmentActivity activity) {
        mActivity = activity;
    }

    public boolean startFragment() {
        //Если в эту сессию показывали промо-попап, то больше не показываем
        if (!needShowPopup) {
            return false;
        }

        //Пробуем по очереди показать каждый тип попапа
        if (showPromoPopup(Options.PromoPopupEntity.AIR_MESSAGES)) {
            return true;
        } else if (showPromoPopup(Options.PromoPopupEntity.AIR_VISITORS)) {
            return true;
        } else if (showPromoPopup(Options.PromoPopupEntity.AIR_ADMIRATIONS)) {
            return true;
        } else {
            needShowPopup = false;
        }
        return false;
    }

    public boolean showPromoPopup(final int type) {
        PromoFragment promo = null;
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        if (checkIsNeedShow(CacheProfile.getOptions().getPremiumEntityByType(type))) {
            promo = getFragmentByType(type);
        }
        if (promo != null) {
            //Подписываемся на события закрытия попапа (купить vip или закрыть)
            promo.setOnCloseListener(new PromoFragment.OnCloseListener() {
                @Override
                public void onClose() {
                    needShowPopup = false;
                }
            });
            //Показываем фрагмент, если он еще не показан
            if (promo.getDialog() == null) {
                promo.show(fragmentManager, getTag(type));
            }
            return true;
        }
        return false;
    }

    private String getTag(int type) {
        return "promo_popup_" + type;
    }

    private PromoFragment getFragmentByType(int type) {
        PromoFragment fragment = null;

        switch (type) {
            case Options.PromoPopupEntity.AIR_ADMIRATIONS:
                fragment = new PromoKey81Fragment();
                break;
            case Options.PromoPopupEntity.AIR_VISITORS:
                fragment = new PromoKey71Fragment();
                break;
            case Options.PromoPopupEntity.AIR_MESSAGES:
                fragment = new PromoKey31Fragment();
                break;
        }

        return fragment;
    }

    private boolean checkIsNeedShow(Options.PromoPopupEntity entity) {
        return entity != null && entity.isNeedShow();
    }


}
