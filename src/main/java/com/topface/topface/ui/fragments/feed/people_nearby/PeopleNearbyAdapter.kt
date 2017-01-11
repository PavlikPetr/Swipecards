package com.topface.topface.ui.fragments.feed.people_nearby

import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.databinding.PeopleNearbyListItemBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.utils.extensions.appContext

/**
 * Created by mbulgakov on 11.01.17.
 */
class PeopleNearbyAdapter : BaseRecyclerViewAdapter<PeopleNearbyListItemBinding, FeedGeo>() {

    override fun getUpdaterEmitObject(): Bundle? = null

    override fun getItemLayout(): Int = R.layout.people_nearby_list_item

    override fun bindData(binding: PeopleNearbyListItemBinding?, position: Int) = binding?.let { bind ->
        getDataItem(position)?.let {
            binding.viewModel = PeopleNearbyListItemViewModel(bind, it)
        }
    } ?: Unit
}