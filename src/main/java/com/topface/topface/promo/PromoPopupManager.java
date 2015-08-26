package com.topface.topface.promo;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.Options;
import com.topface.topface.promo.dialogs.PromoDialog;
import com.topface.topface.promo.dialogs.PromoKey31Dialog;
import com.topface.topface.promo.dialogs.PromoKey71Dialog;
import com.topface.topface.promo.dialogs.PromoKey81Dialog;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import static com.topface.topface.data.Options.PromoPopupEntity.AIR_ADMIRATIONS;
import static com.topface.topface.data.Options.PromoPopupEntity.AIR_MESSAGES;
import static com.topface.topface.data.Options.PromoPopupEntity.AIR_VISITORS;

public class PromoPopupManager {
    public static final String PROMO_POPUP_TAG = "promo_popup";
    private final FragmentActivity mActivity;
    private Options mOptions;

    public PromoPopupManager(FragmentActivity activity) {
        mActivity = activity;
        if (activity instanceof BaseFragmentActivity) {
            mOptions = ((BaseFragmentActivity) activity).getOptions();
        }
    }

    private boolean startFragment() {
        //Пробуем по очереди показать каждый тип попапа
        if (mOptions.premiumMessages != null && mOptions.premiumMessages.getPageId() != BaseFragment.FragmentId.TABBED_DIALOGS.getId() && showPromoPopup(AIR_MESSAGES)) {
            return true;
        } else if (showPromoPopup(AIR_VISITORS) && mOptions.premiumVisitors != null) {
            return true;
        } else if (!mOptions.isHideAdmirations) {
            if (showPromoPopup(AIR_ADMIRATIONS) && mOptions.premiumAdmirations != null) {
                return true;
            }
        }
        return false;
    }

    public boolean showPromoPopup(final int type) {
        PromoDialog promo = null;
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        Debug.log("Promo: try showPromoPopup #" + type);
        if (checkIsNeedShow(mOptions.getPremiumEntityByType(type))) {
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

        if (fragment != null && fragment.getPremiumEntity() == null) {
            return null;
        }
        return fragment;
    }

    private boolean checkIsNeedShow(Options.PromoPopupEntity entity) {
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
                if (CacheProfile.getProfile().premium) return false;
                return (checkIsNeedShow(mOptions.getPremiumEntityByType(AIR_MESSAGES)) &&
                        mOptions.getPremiumEntityByType(AIR_MESSAGES).getPageId() != BaseFragment.FragmentId.TABBED_DIALOGS.getId()) ||
                        checkIsNeedShow(mOptions.getPremiumEntityByType(AIR_VISITORS)) ||
                        checkIsNeedShow(mOptions.getPremiumEntityByType(AIR_ADMIRATIONS));
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
