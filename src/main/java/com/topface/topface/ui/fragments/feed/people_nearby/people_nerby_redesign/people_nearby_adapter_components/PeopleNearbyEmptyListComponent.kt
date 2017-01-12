package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemEmptyPeopleNearbyBinding
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyList
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент заглущки о пустом ответе от сервера на экране "Люди рядом"
 * Created by ppavlik on 11.01.17.
 */

class PeopleNearbyEmptyListComponent : AdapterComponent<ItemEmptyPeopleNearbyBinding, PeopleNearbyEmptyList>() {
    override val itemLayout: Int
        get() = R.layout.item_empty_people_nearby
    override val bindingClass: Class<ItemEmptyPeopleNearbyBinding>
        get() = ItemEmptyPeopleNearbyBinding::class.java

    override fun bind(binding: ItemEmptyPeopleNearbyBinding, data: PeopleNearbyEmptyList?, position: Int) {
        binding.viewModel = PeopleNearbyEmptyViewModel(R.string.nobody_nearby.getString())
    }
}