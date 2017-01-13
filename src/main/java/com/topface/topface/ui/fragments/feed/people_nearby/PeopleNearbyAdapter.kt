package com.topface.topface.ui.fragments.feed.people_nearby

import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.databinding.PeopleNearbyListItemBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * адаптер для списка людей рядом
 */
class PeopleNearbyAdapter : AdapterComponent<PeopleNearbyListItemBinding, FeedGeo>() {

    init {
        Debug.error("----------Конструктор--PeopleNearbyAdapter-------------")
    }
    override val itemLayout: Int
        get() = R.layout.people_nearby_list_item
    override val bindingClass: Class<PeopleNearbyListItemBinding>
        get() = PeopleNearbyListItemBinding::class.java

    override fun bind(binding: PeopleNearbyListItemBinding, data: FeedGeo?, position: Int) {
        data?.let {
            binding.viewModel = PeopleNearbyListItemViewModel(binding, data)
        }
    }


}