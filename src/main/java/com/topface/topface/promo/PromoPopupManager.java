package com.topface.topface.promo;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.topface.topface.data.Options;
import com.topface.topface.promo.dialogs.PromoDialog;
import com.topface.topface.promo.dialogs.PromoKey31Dialog;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.IStartAction;

import static com.topface.topface.data.Options.PromoPopupEntity.AIR_ADMIRATIONS;
import static com.topface.topface.data.Options.PromoPopupEntity.AIR_MESSAGES;
import static com.topface.topface.data.Options.PromoPopupEntity.AIR_VISITORS;

public class PromoPopupManager {
    public static final String PROMO_POPUP_TAG = "promo_popup";
    private final FragmentActivity mActivity;

    public PromoPopupManager(FragmentActivity activity) {
        mActivity = activity;
    }

    private boolean startFragment() {
        //Пробуем по очереди показать каждый тип попапа
        if (showPromoPopup(AIR_MESSAGES)) {
            return true;
        } else if (showPromoPopup(AIR_VISITORS)) {
            return true;
        } else if (showPromoPopup(AIR_ADMIRATIONS)) {
            return true;
        }
        return false;
    }

    public boolean showPromoPopup(final int type) {
        PromoDialog promo = null;
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        Debug.log("Promo: try showPromoPopup #" + type);
        if (checkIsNeedShow(CacheProfile.getOptions().getPremiumEntityByType(type))) {
            Debug.log("Promo: need show popup #" + type);
            promo = (PromoDialog) fragmentManager.findFragmentByTag(PROMO_POPUP_TAG);
            //Проверяем, показывается ли в данный момент попап
            if (promo != null) {
                Debug.log("Promo: promo is already exists #" + type);
                //Если попап есть, но он не показывается пользователю, то удаляем его
                if (!promo.isAdded() || promo.isHidden()) {
                    Debug.log("Promo: promo is hidden #" + type);
                    promo.dismissAllowingStateLoss();
                } else if (promo.isAdded() && promo.isVisible()) {
                    //Если попап уже показывается, то ничего не делаем
                    return true;
                }
            }
            promo = getFragmentByType(type);
        }
        //Если удалось создать новый попап нужного типа, то показываем его
        if (promo != null) {
            //Показываем фрагмент, если он еще не показан
            if (promo.getDialog() == null) {
                Debug.log("Promo: promo show #" + type);
                try {
                    promo.show(fragmentManager, PROMO_POPUP_TAG);
                } catch (Exception e) {
                    Debug.error("Promo: show ecxeption", e);
                }
            }
            return true;
        }
        return false;
    }

    private PromoDialog getFragmentByType(int type) {
        PromoDialog fragment = null;

        switch (type) {
            case AIR_ADMIRATIONS:
                fragment = new PromoKey81Dialog();
                break;
            case AIR_VISITORS:
                fragment = new PromoKey71Dialog();
                break;
            case AIR_MESSAGES:
                fragment = new PromoKey31Dialog();
                break;
        }

        return fragment;
    }

    private boolean checkIsNeedShow(Options.PromoPopupEntity entity) {
        return entity != null && entity.isNeedShow();
    }

    public IStartAction createPromoPopupStartAction(final int priority) {
        return new AbstractStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                startFragment();
            }

            @Override
            public boolean isApplicable() {
                if (CacheProfile.premium) return false;
                Options options = CacheProfile.getOptions();
                return checkIsNeedShow(options.getPremiumEntityByType(AIR_MESSAGES)) ||
                        checkIsNeedShow(options.getPremiumEntityByType(AIR_VISITORS)) ||
                        checkIsNeedShow(options.getPremiumEntityByType(AIR_ADMIRATIONS));
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "PromoPopup";
            }
        };
    }
}
