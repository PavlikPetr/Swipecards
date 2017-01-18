package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.databinding.PeopleNearbyListItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.IPopoverControl
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyListItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * адаптер для списка людей рядом
 */
class PeopleNearbyAdapter(val mNavigator: FeedNavigator,
                          private val mPopoverControl: IPopoverControl) : AdapterComponent<PeopleNearbyListItemBinding, FeedGeo>() {

    override val itemLayout: Int
        get() = R.layout.people_nearby_list_item
    override val bindingClass: Class<PeopleNearbyListItemBinding>
        get() = PeopleNearbyListItemBinding::class.java

    override fun bind(binding: PeopleNearbyListItemBinding, data: FeedGeo?, position: Int) {
        data?.let {
            binding.viewModel = PeopleNearbyListItemViewModel(data, mNavigator, mPopoverControl)
        }
    }
}