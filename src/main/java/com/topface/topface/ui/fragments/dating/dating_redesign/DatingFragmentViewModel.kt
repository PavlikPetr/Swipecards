package com.topface.topface.ui.fragments.dating.dating_redesign


import android.databinding.ObservableField
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.search.SearchUser
import com.topface.topface.viewModels.LeftMenuHeaderViewModel.AGE_TEMPLATE

/**
 * ВьюМодель ререредизайна экрана знакомств(19.01.17)
 */
class DatingFragmentViewModel {

    val name = ObservableField<String>()
    val feedAge = ObservableField<String>()
    val feedCity = ObservableField<String>()
    val iconOnlineRes = ObservableField<Int>()
    val statusText = ObservableField<String>()
    val statusVisibility = ObservableField<Int>()
    val photoCounter = ObservableField<String>()

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
}