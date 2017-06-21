package com.topface.topface.utils

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import bolts.AppLinks
import com.facebook.applinks.AppLinkData
import com.facebook.share.model.AppInviteContent
import com.facebook.share.widget.AppInviteDialog
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.FBInvitesStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.data.Options
import com.topface.topface.state.EventBus
import com.topface.topface.utils.rx.shortSubscription
import com.topface.topface.utils.social.*
import rx.Observable
import rx.Subscription

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
     * При создании активити проверяем наличие AppLink в интенте
     */
    fun onCreateActivity(intent: Intent) = App.getAppConfig().run {
        if (AuthToken.getInstance().isEmpty && fbInviteAppLink.isNullOrEmpty()) {
            val context = App.getContext()
            FbAuthorizer.initFB()
            AppLinks.getTargetUrlFromInboundIntent(context, intent)?.let {
                verifyAppLink(it.toString())
            } ?:
                    AppLinkData.fetchDeferredAppLinkData(context) { appLinkData ->
                        val appLink = appLinkData?.targetUri?.toString()
                        if (appLink != null && appLink.isNotEmpty() && appLink != "null") {
                            verifyAppLink(appLink)
                        }
                    }
        }
    }

    fun createFbInvitesAppLinkSubscription(eventBus: EventBus): Subscription = Observable.combineLatest(
            eventBus.getObservable(FbAppLinkReadyEvent::class.java),
            eventBus.getObservable(FbInviteTemplatesEvent::class.java)) { event1, event2 ->
                object {
                    val appLink = event1.appLink
                    val templates = event2.inviteTemplates
                }
        }.first().subscribe(shortSubscription{
            if (it.templates.isLinkValid(it.appLink)) {
                with(App.getAppConfig()) {
                    fbInviteAppLink = it.appLink
                    App.getAppComponent().eventBus().setData(FbAppLinkStoredEvent(fbInviteAppLink))
                    saveConfig()
                }
            }
        })

    fun createSendAuthStatusStatisticSubscription(eventBus: EventBus): Subscription = Observable.combineLatest(
            eventBus.getObservable(FbAppLinkStoredEvent::class.java),
            eventBus.getObservable(AuthStatusReadyEvent::class.java)) { event1, event2 ->
                object {
                    val appLink = event1.appLink
                    val authStatus = event2.authStatus
                }
        }.first().subscribe(shortSubscription {
            if (!it.appLink.isNullOrEmpty()) {
                if (!it.authStatus.isNullOrEmpty()) {
                    val slice = Slices().putSlice("val", it.appLink)
                    when(it.authStatus) {
                        "regular" -> FBInvitesStatisticsGeneratedStatistics.sendNow_FB_INVITE_AUTHORIZE(slice)
                        "created" -> FBInvitesStatisticsGeneratedStatistics.sendNow_FB_INVITE_REGISTER(slice)
                    }
                }
                FBInvitesUtils.AppLinkSended()
            }
    })

    /**
     * Валидными считать линки вида: http://topface.com/landingtf/?uid=*
     * Клиентским передавать ссылку в параметре fbInvite в запросе к методу: referral.track
     * Если ссылка валидная: вернется CompletedResponse
     * Если ссылка не валидная: выкинется ошибка IncorrectValueLogicException (код 23)
     */
    private fun verifyAppLink(link: String) {
        App.get().onFbAppLinkReady(link)
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