package com.topface.topface.ui.fragments.feed.people_nearby

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.PeopleNearbyListBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyList
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter


/**
 * Адаптер компонент для итема "Людей рядом", внутри которого
 */
class PeopleNearbyListComponent(private val mContext: Context, val mApi: FeedApi) : AdapterComponent<PeopleNearbyListBinding, PeopleNearbyList>() {

    private lateinit var mViewModel: PeopleNearbyListViewModel
    private lateinit var mAdapter: CompositeAdapter

    override fun bind(binding: PeopleNearbyListBinding, data: PeopleNearbyList?, position: Int) {
        with(binding) {

            val manager = GridLayoutManager(mContext, mContext.resources.getInteger(R.integer.add_to_people_nearby_count))

            peopleList.layoutManager = manager
            mAdapter = CompositeAdapter(PeopleNearbyTypeProvider()) { Bundle() }
                    .addAdapterComponent(PeopleNearbyAdapter())
            peopleList.adapter = mAdapter
            mViewModel = PeopleNearbyListViewModel(mApi, data?.item)
            viewModel = mViewModel
        }
    }

    override val itemLayout: Int
        get() = R.layout.people_nearby_list
    override val bindingClass: Class<PeopleNearbyListBinding>
        get() = PeopleNearbyListBinding::class.java

    override fun release() {
        super.release()
        mViewModel.release()
    }

}