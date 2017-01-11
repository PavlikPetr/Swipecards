package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemEmptyPeopleNearbyBinding
import com.topface.topface.databinding.ItemLoaderBinding
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyList
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyViewModel
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyLoader
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент лоадера
 * Created by ppavlik on 11.01.17.
 */

class PeopleNearbyLoaderComponent : AdapterComponent<ItemLoaderBinding, PeopleNearbyLoader>() {
    override val itemLayout: Int
        get() = R.layout.item_loader
    override val bindingClass: Class<ItemLoaderBinding>
        get() = ItemLoaderBinding::class.java

    override fun bind(binding: ItemLoaderBinding, data: PeopleNearbyLoader?, position: Int) {
    }
}