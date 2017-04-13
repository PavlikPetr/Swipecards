package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.LayoutUnavailableGeoBinding
import com.topface.topface.ui.fragments.feed.people_nearby.DontAskGeoViewModel
import com.topface.topface.ui.fragments.feed.people_nearby.UnavailableGeoViewModel
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyPermissionDenied
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyPermissionNeverAskAgain
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент заглушки с информацией о том, что следует пройти в настройки приложения для активации
 * пермишина {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
 * Created by ppavlik on 11.01.17.
 */

class PeopleNearbyPermissionsNeverAskAgainComponent : AdapterComponent<LayoutUnavailableGeoBinding, PeopleNearbyPermissionNeverAskAgain>() {
    override val itemLayout: Int
        get() = R.layout.layout_unavailable_geo
    override val bindingClass: Class<LayoutUnavailableGeoBinding>
        get() = LayoutUnavailableGeoBinding::class.java

    override fun bind(binding: LayoutUnavailableGeoBinding, data: PeopleNearbyPermissionNeverAskAgain?, position: Int) {
        binding.setViewModel(DontAskGeoViewModel(binding))
    }
}