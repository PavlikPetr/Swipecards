package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.PeopleNearbyListBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.IPopoverControl
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyList
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyListViewModel
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyTypeProvider
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.getInt


/**
 * Адаптер компонент для итема "Людей рядом", внутри которого
 */

class PeopleNearbyListComponent(val context: Context, private val mApi: FeedApi, private val mNavigator: FeedNavigator,
                                private val mPopoverControl: IPopoverControl) : AdapterComponent<PeopleNearbyListBinding, PeopleNearbyList>() {

    private lateinit var mViewModel: PeopleNearbyListViewModel
    private lateinit var mAdapter: CompositeAdapter

    override fun bind(binding: PeopleNearbyListBinding, data: PeopleNearbyList?, position: Int) {
        with(binding) {
            peopleList.layoutManager = StaggeredGridLayoutManager(R.integer.add_to_people_nearby_count.getInt(),
                    StaggeredGridLayoutManager.VERTICAL)
            mAdapter = CompositeAdapter(PeopleNearbyTypeProvider()) { Bundle() }
                    .addAdapterComponent(PeopleNearbyAdapter(mNavigator, mPopoverControl))
                    .addAdapterComponent(PeopleNearbyEmptyListComponent())
                    .addAdapterComponent(PeopleNearbyEmptyLocationComponent())
                    .addAdapterComponent(PeopleNearbyLockedComponent(mApi, mNavigator))
            peopleList.adapter = mAdapter
            mViewModel = PeopleNearbyListViewModel(mApi, data?.item)
            viewModel = mViewModel
            peopleList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
//                    mPopoverControl.close()
                }
            })
        }
    }

    override val itemLayout: Int
        get() = R.layout.people_nearby_list
    override val bindingClass: Class<PeopleNearbyListBinding>
        get() = PeopleNearbyListBinding::class.java

    override fun release() = mViewModel.release()

}