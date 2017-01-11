package com.topface.topface.ui.fragments.feed.people_nearby

import android.support.v7.widget.GridLayoutManager
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.databinding.PeopleNearbyListBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import java.util.*


/**
 * Created by mbulgakov on 11.01.17.
 */
class PeopleNearbyItemComponent(val mListOfPeopleNearby: MutableList<FeedGeo>) : AdapterComponent<PeopleNearbyListBinding, PeoplesNearbyStubItem>() {

    override val itemLayout: Int
        get() = R.layout.people_nearby_list
    override val bindingClass: Class<PeopleNearbyListBinding>
        get() = PeopleNearbyListBinding::class.java

    override fun bind(binding: PeopleNearbyListBinding, data: PeoplesNearbyStubItem?, position: Int) = data?.let {
        with(binding.peopleList) {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.add_to_people_nearby_count))
            adapter = PeopleNearbyAdapter().apply {
                addData(mListOfPeopleNearby as ArrayList<FeedGeo>)
            }
        }
    } ?: Unit

}