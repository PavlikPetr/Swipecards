package com.topface.topface.ui.fragments.dating.dating_redesign


import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.search.SearchUser
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.viewModels.LeftMenuHeaderViewModel.AGE_TEMPLATE

/**
 * ВьюМодель ререредизайна экрана знакомств(19.01.17)
 */
class DatingFragmentViewModel(val mNavigator: IFeedNavigator) {

    val name = ObservableField<String>()
    val feedAge = ObservableField<String>()
    val feedCity = ObservableField<String>()
    val iconOnlineRes = ObservableField<Int>()
    val statusText = ObservableField<String>()
    val statusVisibility = ObservableField<Int>()
    val photoCounter = ObservableField<String>()
//    лоадер крутится - INVISIBLE мутится
    val isVisible = ObservableInt(View.VISIBLE)
    val isDatingButtonsLocked = ObservableBoolean(false)

    var currentUser: SearchUser? = null
        set(value) {
            field = value?.apply {
                name.set(value.firstName)
                feedAge.set(String.format(App.getCurrentLocale(), AGE_TEMPLATE, value.age))
                feedCity.set(value.city.name)
                iconOnlineRes.set(if (value.online) R.drawable.ico_online else 0)
                statusText.set(value.getStatus())
                statusVisibility.set(if (value.getStatus().isEmpty()) View.GONE else View.VISIBLE)
                updatePhotosCounter(0)
            }
        }

    fun updatePhotosCounter(position: Int) = photoCounter.set("${position + 1}/${currentUser?.photosCount}")

    fun showChat() = if (App.get().profile.premium) {
        mNavigator.showChat(currentUser, null)
    } else {
        mNavigator.showPurchaseVip("dating_fragment")
    }

    fun sendAdmiration() {
        //todo отправка восхищения
    }

    fun skip() {
        //todo Скип фида и подгрузка следующего
    }

    fun sendLike() {
        //todo Лайк фида и подгрузка следующего
    }

}