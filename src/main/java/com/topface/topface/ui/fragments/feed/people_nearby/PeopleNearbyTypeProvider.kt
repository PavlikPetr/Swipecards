package com.topface.topface.ui.fragments.feed.people_nearby

import com.topface.topface.data.FeedGeo
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Created by mbulgakov on 13.01.17.
 */
class PeopleNearbyTypeProvider: ITypeProvider {
    override fun getType(java: Class<*>): Int {
        if (java == FeedGeo::class.java) {
            return 1
        }
        return 0
    }

}