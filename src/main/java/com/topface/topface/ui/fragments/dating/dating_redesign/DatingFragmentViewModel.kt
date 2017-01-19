package com.topface.topface.ui.fragments.dating.dating_redesign


import android.databinding.ObservableField
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.search.SearchUser
import com.topface.topface.utils.Utils
import com.topface.topface.viewModels.LeftMenuHeaderViewModel.AGE_TEMPLATE

/**
 * ВьюМодель ререредизайна экрана знакомств(19.01.17)
 */
class DatingFragmentViewModel {

    val name = ObservableField<String>()
    val age = ObservableField<String>()
    val city = ObservableField<String>()
    val iconOnlineRes = ObservableField<Int>()
    val statusText = ObservableField<String>()
    val statusVisibility = ObservableField<Int>()
    val photoCounterVisibility = ObservableField<Int>()
    val photoCounter = ObservableField<String>()

    var currentUser: SearchUser? = null
        set(value) {
            field = value
            value?.let {
                name.set(value.firstName ?: Utils.EMPTY)
                age.set(String.format(App.getCurrentLocale(), AGE_TEMPLATE, value.age))
                city.set(value.city?.name)
                iconOnlineRes.set(if (value.online) R.drawable.ico_online else 0)
                statusText.set(value.getStatus())
                statusVisibility.set(if (value.getStatus().isEmpty()) View.GONE else View.VISIBLE)
                updatePhotosCounter(0)
            }
        }

    fun updatePhotosCounter(position: Int) {
        val user = currentUser
        if (user != null && user.photos != null && user.photos.isNotEmpty()) {
            if (photoCounterVisibility.get() == View.GONE) {
                photoCounterVisibility.set(View.VISIBLE)
            }
            photoCounter.set("${position + 1}/${user.photosCount}")
        } else {
            photoCounterVisibility.set(View.GONE)
        }
    }
}