package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemEmptyPeopleNearbyBinding
import com.topface.topface.databinding.ItemLockedPeopleNearbyBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyLocation
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyViewModel
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyLocked
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyLockedViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент заглущки о пустом ответе от сервера на экране "Люди рядом"
 * Created by ppavlik on 11.01.17.
 */

class PeopleNearbyLockedComponent(private val mApi: FeedApi, private val mNavigator: FeedNavigator) : AdapterComponent<ItemLockedPeopleNearbyBinding, PeopleNearbyLocked>() {
    override val itemLayout: Int
        get() = R.layout.item_locked_people_nearby
    override val bindingClass: Class<ItemLockedPeopleNearbyBinding>
        get() = ItemLockedPeopleNearbyBinding::class.java

    private var mViewModel: PeopleNearbyLockedViewModel? = null

    override fun bind(binding: ItemLockedPeopleNearbyBinding, data: PeopleNearbyLocked?, position: Int) {
        mViewModel = PeopleNearbyLockedViewModel(mApi, mNavigator)
        binding.viewModel = mViewModel
        binding.root.layoutParams = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT).apply { isFullSpan = true }
    }

    override fun release() {
        super.release()
        mViewModel?.release()
    }
}