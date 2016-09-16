package com.topface.topface.utils.popups.start_actions

import android.content.DialogInterface
import android.support.v4.app.FragmentManager
import com.topface.topface.App
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.dialogs.TakePhotoPopup
import com.topface.topface.utils.Utils
import com.topface.topface.utils.controllers.startactions.IStartAction
import com.topface.topface.utils.popups.PopupManager
import com.topface.topface.utils.social.AuthToken

/**
 * Акшн для запуска попапа фоточки
 * Created by tiberal on 31.08.16.
 */
class SelectPhotoStartAction(private val mFragmentManager: FragmentManager, private val mPriority: Int, val mFrom: String) : IStartAction {


    override fun callInBackground() {
    }

    override fun callOnUi() {
        if (!NavigationActivity.isPhotoAsked) {
            NavigationActivity.isPhotoAsked = true
            var popup = mFragmentManager.findFragmentByTag(TakePhotoPopup.TAG) as TakePhotoPopup?
            if (popup == null) {
                popup = TakePhotoPopup.newInstance(Utils.EMPTY)
            }
            popup.setOnCancelListener(DialogInterface.OnCancelListener {
                PopupManager.informManager(mFrom)
            })
            popup.show(mFragmentManager, TakePhotoPopup.TAG)
        }
    }

    override fun isApplicable() = !AuthToken.getInstance().isEmpty && App.get().profile.photo == null
            && !App.getConfig().userConfig.isUserAvatarAvailable

    override fun getPriority() = mPriority

    override fun getActionName(): String? = this.javaClass.simpleName

}