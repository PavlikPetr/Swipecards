package com.topface.topface.ui.fragments.feed.people_nearby

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutUnavailableGeoBinding

/**
 * Created by petrp on 31.10.2016.
 */
class DontAskGeoViewModel(binding: LayoutUnavailableGeoBinding) : UnavailableGeoBaseViewModel(binding,
        App.getContext().getString(R.string.geo_switched_off_explanation),
        App.getContext().getString(R.string.turn_on_geo)) {
    override fun onButtonClick() {
    }
}
