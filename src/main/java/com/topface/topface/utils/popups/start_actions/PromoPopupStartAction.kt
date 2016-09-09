package com.topface.topface.utils.popups.start_actions

import android.support.v4.app.FragmentActivity
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.data.Options
import com.topface.topface.data.Options.PromoPopupEntity.AIR_ADMIRATIONS
import com.topface.topface.data.Options.PromoPopupEntity.AIR_VISITORS
import com.topface.topface.promo.dialogs.OnPromoDialogEventsListener
import com.topface.topface.promo.dialogs.PromoDialog
import com.topface.topface.promo.dialogs.PromoKey71Dialog
import com.topface.topface.promo.dialogs.PromoKey81Dialog
import com.topface.topface.utils.controllers.startactions.IStartAction
import com.topface.topface.utils.popups.PopupManager

/**
 * Стартуем промо
 * Created by tiberal on 31.08.16.
 */
class PromoPopupStartAction(private val mActivity: FragmentActivity, private val mPriority: Int, private val mFrom: String) : IStartAction {


    companion object {
        val PROMO_POPUP_TAG = "promo_popup"
        fun checkIsNeedShow(entity: Options.PromoPopupEntity?) = entity != null && entity.isNeedShow
    }

    override fun callInBackground() {
    }

    override fun callOnUi() {
        startFragment()
    }

    override fun isApplicable(): Boolean {
        val options = App.get().options
        if (App.get().profile.premium) return false
        return checkIsNeedShow(options.getPremiumEntityByType(AIR_VISITORS)) ||
                checkIsNeedShow(options.getPremiumEntityByType(AIR_ADMIRATIONS));
    }

    override fun getPriority() = mPriority

    override fun getActionName(): String? = this.javaClass.simpleName

    private fun startFragment(): Boolean {
        //Пробуем по очереди показать каждый тип попапа
        val options = App.get().options
        if (showPromoPopup(AIR_VISITORS) && options.premiumVisitors != null) {
            return true
        } else if (!options.isHideAdmirations && showPromoPopup(AIR_ADMIRATIONS)
                && options.premiumAdmirations != null) {
            return true
        }
        return false
    }

    fun showPromoPopup(type: Int): Boolean {
        var promo: PromoDialog? = null
        val fragmentManager = mActivity.supportFragmentManager
        Debug.log("Promo: try showPromoPopup #$type")
        if (checkIsNeedShow(App.get().options.getPremiumEntityByType(type))) {
            Debug.log("Promo: need show popup #$type")
            promo = fragmentManager.findFragmentByTag(PROMO_POPUP_TAG) as PromoDialog?
            //Проверяем, показывается ли в данный момент попап
            if (promo != null) {
                Debug.log("Promo: promo is already exists #$type")
                //Если попап есть, но он не показывается пользователю, то удаляем его
                if (!promo.isAdded || promo.isHidden) {
                    Debug.log("Promo: promo is hidden #$type")
                    promo.dismissAllowingStateLoss()
                } else if (promo.isAdded && promo.isVisible) {
                    //Если попап уже показывается, то ничего не делаем
                    return true
                }
            }
            promo = getFragmentByType(type)
        }
        //Если удалось создать новый попап нужного типа, то показываем его
        if (promo != null) {
            //Показываем фрагмент, если он еще не показан
            if (promo.dialog == null) {
                Debug.log("Promo: promo show #$type")
                try {
                    promo.show(fragmentManager, PROMO_POPUP_TAG)
                } catch (e: Exception) {
                    Debug.error("Promo: show ecxeption $e")
                }

            }
            return true
        }
        return false
    }

    private fun getFragmentByType(type: Int): PromoDialog? {
        val fragment: PromoDialog? = when (type) {
            AIR_ADMIRATIONS -> PromoKey81Dialog()
            AIR_VISITORS -> PromoKey71Dialog()
            else -> null
        }
        return if (fragment != null && fragment.premiumEntity == null) {
            null
        } else {
            fragment?.apply {
                setPromoPopupEventsListener(object : OnPromoDialogEventsListener {
                    override fun onDeleteMessageClick() {
                        PopupManager.informManager(mFrom)
                    }
                })
            }
        }
    }
}