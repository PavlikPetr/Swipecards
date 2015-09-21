package com.topface.topface.promo;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.promo.dialogs.PromoDialog;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import static com.topface.topface.data.Options.PromoPopupEntity.AIR_ADMIRATIONS;
import static com.topface.topface.data.Options.PromoPopupEntity.AIR_VISITORS;

public class PromoPopupManager {
    public static final String PROMO_POPUP_TAG = "promo_popup";
    private final FragmentActivity mActivity;

    public PromoPopupManager(FragmentActivity activity) {
        mActivity = activity;
    }

    private boolean startFragment() {
        //Пробуем по очереди показать каждый тип попапа
        Options options = App.from(mActivity).getOptions();
        if (options.premiumMessages != null && options.premiumMessages.getPageId() != BaseFragment.FragmentId.TABBED_DIALOGS.getId() && showPromoPopup(AIR_MESSAGES)) {
            return true;
        } else if (showPromoPopup(AIR_VISITORS) && options.premiumVisitors != null) {
            return true;
        } else if (!options.isHideAdmirations) {
            if (showPromoPopup(AIR_ADMIRATIONS) && options.premiumAdmirations != null) {
                return true;
            }
        }
        return false;
    }

    public boolean showPromoPopup(final int type) {
        PromoDialog promo = null;
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        Debug.log("Promo: try showPromoPopup #" + type);
        if (checkIsNeedShow(App.from(mActivity).getOptions().getPremiumEntityByType(type))) {
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
        }

        if (fragment != null && fragment.getPremiumEntity() == null) {
            return null;
        }
        return fragment;
    }

    public static boolean checkIsNeedShow(Options.PromoPopupEntity entity) {
        return entity != null && entity.isNeedShow();
    }

    public IStartAction createPromoPopupStartAction(final int priority) {
        return new IStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                startFragment();
            }

            @Override
            public boolean isApplicable() {
                Options options = App.from(mActivity).getOptions();
                if (App.from(mActivity).getProfile().premium) return false;
                return checkIsNeedShow(options.getPremiumEntityByType(AIR_VISITORS)) ||
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

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {

            }
        };
    }
}
