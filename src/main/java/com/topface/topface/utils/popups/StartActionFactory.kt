package com.topface.topface.utils.popups

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.topface.topface.ui.BaseFragmentActivity
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.dialogs.NotificationsDisableStartAction
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.ads.FullscreenController
import com.topface.topface.utils.controllers.ChosenStartAction
import com.topface.topface.utils.controllers.startactions.*
import com.topface.topface.utils.popups.start_actions.*

/**
 * Фабрика для создания действий на запуск попапов
 * Created by tiberal on 31.08.16.
 */
class StartActionFactory : IStartActionFactory {

    override fun construct(actionHolder: PopupSequence.ActionHolder, activity: FragmentActivity, from: String): IStartAction? {
        val action: IStartAction? = when (actionHolder.actionsClass) {
            ChosenStartAction::class.java ->
                if (!actionHolder.nestedActions.isEmpty()) {
                    val chosenAction = ChosenStartAction()
                    actionHolder.nestedActions.forEach {
                        //да, это она
                        (chosenAction).addAction(construct(PopupSequence.ActionHolder(it), activity, from))
                    }
                    chosenAction
                } else null

            OldVersionStartAction::class.java -> OldVersionStartAction(activity, PopupManager.AC_PRIORITY_HIGH, from)
            PromoPopupStartAction::class.java -> PromoPopupStartAction(activity, PopupManager.AC_PRIORITY_NORMAL, from)
            RatePopupStartAction::class.java -> RatePopupStartAction(activity, PopupManager.AC_PRIORITY_NORMAL, from)
            DatingLockPopupAction::class.java -> DatingLockPopupAction(activity.supportFragmentManager, PopupManager.AC_PRIORITY_HIGH, from)
            ExpressMessageAction::class.java -> ExpressMessageAction(activity.supportFragmentManager, PopupManager.AC_PRIORITY_HIGH, from)
            InvitePopupAction::class.java ->
                if (activity is IActivityDelegate) {
                    InvitePopupAction(activity, PopupManager.AC_PRIORITY_HIGH, from)
                } else null

            NotificationsDisableStartAction::class.java -> NotificationsDisableStartAction(activity, PopupManager.AC_PRIORITY_HIGH, from)
            TrialVipPopupAction::class.java ->
                if (activity is BaseFragmentActivity<*>) {
                    TrialVipPopupAction(activity, PopupManager.AC_PRIORITY_HIGH, from)
                } else null
            SelectPhotoStartAction::class.java -> SelectPhotoStartAction(activity.supportFragmentManager, PopupManager.AC_PRIORITY_HIGH, from)
            ChooseCityPopupAction::class.java -> ChooseCityPopupAction(activity.supportFragmentManager, PopupManager.AC_PRIORITY_HIGH, from)
            FullscreenController::class.java ->
                if (activity is NavigationActivity) {
                    activity.fullscreenController?.createFullscreenStartAction(PopupManager.AC_PRIORITY_HIGH, activity, from)
                } else null

            else -> null
        }
        return action
    }

    fun getVisibleFragment(activity: FragmentActivity): Fragment? {
        val fragmentManager = activity.supportFragmentManager
        val fragments = fragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible)
                    return fragment
            }
        }
        return null


    }
}