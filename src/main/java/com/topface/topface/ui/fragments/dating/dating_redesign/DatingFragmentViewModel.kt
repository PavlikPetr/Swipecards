package com.topface.topface.ui.fragments.dating.dating_redesign


import android.databinding.ObservableField
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

    var currentUser: SearchUser? = null
        set(value) {
            field = value
            value?.let {
                name.set(value.firstName ?: Utils.EMPTY)
                age.set(String.format(App.getCurrentLocale(), AGE_TEMPLATE, value.age))
                city.set(value.city?.name)
                iconOnlineRes.set(if (value.online) R.drawable.ico_online else 0)
            }
        }
}