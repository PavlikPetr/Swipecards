package com.topface.topface.ui.fragments.feed.feed_base

import com.topface.topface.App
import com.topface.topface.data.FeedItem
import com.topface.topface.data.leftMenu.FragmentIdData
import com.topface.topface.data.leftMenu.LeftMenuSettingsData
import com.topface.topface.data.leftMenu.NavigationState
import com.topface.topface.data.leftMenu.WrappedNavigationData
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.PurchasesActivity
import com.topface.topface.ui.UserProfileActivity
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils
import javax.inject.Inject

/**
 * Класс для управления переходами между эркраними в фидах
 * Created by tiberal on 12.08.16.
 */
//todo раздавать через даггер 2, синглтон на фрагмент
class FeedNavigator(private val mActivityDelegate: IActivityDelegate) : IFeedNavigator {

    @Inject lateinit var mNavigationState: NavigationState

    init {
        App.get().inject(this)
    }

    override fun showPurchaseCoins() = mActivityDelegate.startActivity(PurchasesActivity
            .createBuyingIntent("EmptyLikes", App.get().options.topfaceOfferwallRedirect))

    override fun showPurchaseVip() = mActivityDelegate.startActivityForResult(PurchasesActivity
            .createVipBuyIntent(null, "Likes"), PurchasesActivity.INTENT_BUY_VIP)

    override fun <T : FeedItem> showProfile(item: T?) {
        item?.let {
            if (!it.user.isEmpty) {
                val user = it.user
                mActivityDelegate.startActivity(UserProfileActivity.createIntent(null, user.photo,
                        user.id, it.id, false, true, Utils.getNameAndAge(user.firstName, user.age),
                        user.city.getName()))
            }
        }
    }

    override fun <T : FeedItem> showChat(item: T?) {
        item?.let {
            if (!it.user.isEmpty) {
                val user = it.user
                val intent = ChatActivity.createIntent(user.id, user.sex, user.nameAndAge, user.city.name, null, user.photo, false, item.type, user.banned)
                mActivityDelegate.startActivityForResult(intent, ChatActivity.REQUEST_CHAT)
            }
        }
    }

    override fun showDating() = mNavigationState
            .emmitNavigationState(WrappedNavigationData(LeftMenuSettingsData(FragmentIdData.DATING),
                    WrappedNavigationData.SELECT_EXTERNALY))

}