package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.data.FeedGeo
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for people nearby fragment items
 * Created by ppavlik on 11.01.17.
 */
class PeopleNearbyTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        PhotoBlogList::class.java -> 1
        PeopleNearbyList::class.java -> 2
        PeopleNearbyEmptyList::class.java -> 3
        PeopleNearbyLocked::class.java -> 4
        PeopleNearbyEmptyLocation::class.java -> 5
        PeopleNearbyPermissionDenied::class.java -> 6
        PeopleNearbyPermissionNeverAskAgain::class.java -> 7
        PeopleNearbyLoader::class.java -> 8
        FeedGeo::class.java -> 9
        else -> 0
    }
}