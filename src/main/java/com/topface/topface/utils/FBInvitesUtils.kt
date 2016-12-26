package com.topface.topface.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import bolts.AppLinks
import com.facebook.FacebookSdk
import com.facebook.share.model.AppInviteContent
import com.facebook.share.widget.AppInviteDialog
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.data.Options
import com.topface.topface.utils.social.AuthToken

object FBInvitesUtils {
    const val FB_APP_LINK_SENDED = "fb_app_link_sended"

    /**
     * билдим и запускаем фэйсбучный диалог для инвайта друзей в ТФ
     */
    fun showFBInvitePopup(activity: Activity, appLinkUrl: String, previewImageUrl: String) =
            AppInviteDialog.show(activity, AppInviteContent.Builder().apply {
                setApplinkUrl(appLinkUrl)
                if (!TextUtils.isEmpty(previewImageUrl)) {
                    setPreviewImageUrl(previewImageUrl)
                }
            }.build())

    /**
     * проверяем возможность показа FB диалога приглашения друзей
     */
    fun isFBInviteApplicable(options: Options) =
            AuthToken.getInstance().socialNet == AuthToken.SN_FACEBOOK &&
                    AppInviteDialog.canShow() && !options.fbInviteSettings.isEmpty() &&
                    options.fbInviteSettings.enabled

    /**
     * Инциализируем FB sdk и пробуем достать AppLink
     */
    fun getAppLinkFromIntent(intent: Intent, context: Context = App.getContext()): Uri? {
        FacebookSdk.sdkInitialize(context)
        return AppLinks.getTargetUrlFromInboundIntent(context, intent)
    }

    /**
     * При создании активити проверяем наличие AppLink в интенте
     */
    fun onCreateActivity(intent: Intent) = App.getAppConfig().apply {
        if (AuthToken.getInstance().isEmpty) {
            if (App.getAppConfig().fbInviteAppLink.isNullOrEmpty()) {
                fbInviteAppLink = getAppLinkFromIntent(intent)?.toString() ?: FB_APP_LINK_SENDED
            }
        } else {
            fbInviteAppLink = FB_APP_LINK_SENDED
        }
        Debug.error("FB_APP_LINK link = $fbInviteAppLink")
        saveConfig()
    }

    fun getAppLinkToSend() =
            with(App.getAppConfig().fbInviteAppLink) {
                if (!this.isNullOrEmpty() &&
                        this != FB_APP_LINK_SENDED) {
                    this
                } else Utils.EMPTY
            }

    fun AppLinkSended() = App.getAppConfig().apply {
        fbInviteAppLink = FB_APP_LINK_SENDED
        saveConfig()
    }
}