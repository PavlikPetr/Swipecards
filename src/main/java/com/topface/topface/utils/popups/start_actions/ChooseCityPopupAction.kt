package com.topface.topface.utils.popups.start_actions

import android.content.DialogInterface
import android.support.v4.app.FragmentManager
import android.widget.Toast
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.SettingsRequest
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.ui.dialogs.CitySearchPopup
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup
import com.topface.topface.utils.controllers.startactions.IStartAction
import com.topface.topface.utils.popups.PopupManager

/**
 * Выбор города
 * Created by tiberal on 31.08.16.
 */
class ChooseCityPopupAction(private val mFragmentManager: FragmentManager, private val mPriority: Int, private val mFrom: String) : IStartAction {

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }

    override fun callInBackground() {
    }

    override fun callOnUi() {
        var popup = mFragmentManager.findFragmentByTag(TakePhotoPopup.TAG) as CitySearchPopup?
        if (popup == null) {
            popup = CitySearchPopup()
        }
        popup.setOnCitySelected() {
            if (it != null) {
                val request = SettingsRequest(App.getContext())
                request.cityid = it.id
                request.callback(object : ApiHandler() {
                    override fun success(response: IApiResponse) {
                        val profile = App.get().profile
                        profile.city = it
                        mAppState.setData(profile)
                    }

                    override fun fail(codeError: Int, response: IApiResponse) {
                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show()
                    }
                }).exec()
            }
        }
        popup.setOnCancelListener(DialogInterface.OnCancelListener {
            PopupManager.informManager(mFrom)
        })
        popup.show(mFragmentManager, CitySearchPopup.TAG)
    }

    override fun isApplicable(): Boolean {
        val profile = App.get().profile
        return profile.city == null || profile.city.isEmpty
    }

    override fun getPriority() = mPriority

    override fun getActionName(): String? = this.javaClass.simpleName

}