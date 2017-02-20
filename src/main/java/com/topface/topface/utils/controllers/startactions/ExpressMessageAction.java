package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.promo.dialogs.PromoExpressMessages;
import com.topface.topface.promo.dialogs.SimplePromoDialogEventsListener;
import com.topface.topface.utils.popups.PopupManager;
import com.topface.topface.utils.popups.start_actions.PromoPopupStartAction;

import org.jetbrains.annotations.NotNull;

import static com.topface.topface.data.Options.PromoPopupEntity.AIR_MESSAGES;

/**
 * Created by ppavlik on 06.07.16.
 * PromoExpressMessages action
 */

public class ExpressMessageAction implements IStartAction {
    private int mPriority;
    private FragmentManager mFragmentManager;
    private String mFrom;

    public ExpressMessageAction(@NotNull FragmentManager fragmentManager, int priority, String from) {
        mFragmentManager = fragmentManager;
        mPriority = priority;
        mFrom = from;
    }

    @Override
    public void callInBackground() {

    }

    @Override
    public void callOnUi() {
        PromoExpressMessages popup = new PromoExpressMessages();
        popup.setPromoPopupEventsListener(new SimplePromoDialogEventsListener() {
            @Override
            public void onDeleteMessageClick() {
                PopupManager.INSTANCE.informManager(mFrom);
            }

            @Override
            public void onVipBought() {
                App.getAppComponent().navigationState()
                        .emmitNavigationState(new WrappedNavigationData(
                                new LeftMenuSettingsData(FragmentIdData.TABBED_DIALOGS), WrappedNavigationData.SELECT_EXTERNALY));
            }
        });
        popup.show(mFragmentManager, PromoExpressMessages.TAG);
    }

    @Override
    public boolean isApplicable() {
        return !App.get().getProfile().premium
                && PromoPopupStartAction.Companion.checkIsNeedShow(App.get().getOptions().getPremiumEntityByType(AIR_MESSAGES));
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

}
