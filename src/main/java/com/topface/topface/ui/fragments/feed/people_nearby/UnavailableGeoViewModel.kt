package com.topface.topface.ui.fragments.feed.people_nearby

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutUnavailableGeoBinding
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.showAppSettings

/**
 * Вьюмодель попапа когда пользователь не заапрувил пермишин гео
 * Created by petrp on 31.10.2016.
 */
class UnavailableGeoViewModel(binding: LayoutUnavailableGeoBinding, val showSystemPermissionPopup: () -> Unit) : UnavailableGeoBaseViewModel(binding,
        R.string.geo_switched_off_explanation.getString(),
        R.string.turn_on_geo.getString()) {
    override fun onButtonClick() {
        showSystemPermissionPopup.invoke()
    }
}
