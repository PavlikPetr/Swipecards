package com.topface.topface.utils.controllers.startactions;

import com.topface.topface.App;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.promo.dialogs.PromoExpressMessages;
import com.topface.topface.promo.dialogs.SimplePromoDialogEventsListener;
import com.topface.topface.utils.IActivityDelegate;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import static com.topface.topface.data.Options.PromoPopupEntity.AIR_MESSAGES;

/**
 * Created by ppavlik on 06.07.16.
 * PromoExpressMessages action
 */

public class ExpressMessageAction implements IStartAction {
    @Inject
    NavigationState mNavigationState;
    private int mPriority;
    private static OnNextActionListener mOnNextActionListener;
    private IActivityDelegate mIActivityDelegate;

    public ExpressMessageAction(@NotNull IActivityDelegate delegate, int priority) {
        App.get().inject(this);
        mIActivityDelegate = delegate;
        mPriority = priority;
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
                if (mOnNextActionListener != null) {
                    mOnNextActionListener.onNextAction();
                }
            }

            @Override
            public void onVipBought() {
                mNavigationState.emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.TABBED_DIALOGS), WrappedNavigationData.SELECT_EXTERNALY));
            }
        });
        popup.show(mIActivityDelegate.getSupportFragmentManager(), PromoExpressMessages.TAG);
    }

    @Override
    public boolean isApplicable() {
        return !App.get().getProfile().premium && PromoPopupManager.checkIsNeedShow(App.get().getOptions().getPremiumEntityByType(AIR_MESSAGES));
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return "PromoPopup";
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }
}
