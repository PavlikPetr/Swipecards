package com.topface.topface.ui.fragments.feed.people_nearby

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutUnavailableGeoBinding
import com.topface.topface.utils.extensions.showAppSettings

/**
 * Вьюмодель попапа на случай, если пользователь нажал "Don't ask again" пермишин гео
 * Created by petrp on 31.10.2016.
 */
class DontAskGeoViewModel(binding: LayoutUnavailableGeoBinding) : UnavailableGeoBaseViewModel(binding,
        App.getContext().getString(R.string.geo_show_settings_explanation),
        App.getContext().getString(R.string.show_geo_settings)) {
    override fun onButtonClick() = App.getContext().showAppSettings()

}
