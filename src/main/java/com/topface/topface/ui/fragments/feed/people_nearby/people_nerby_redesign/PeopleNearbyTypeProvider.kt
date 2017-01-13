package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.data.FeedGeo
import com.topface.topface.data.FeedListData
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogContactsStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.UForeverAloneStubItem
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
        PeopleNearbyVipOnly::class.java -> 4
        PeopleNearbyEmptyLocation::class.java -> 5
        PeopleNearbyPermissionDenied::class.java -> 6
        PeopleNearbyPermissionNeverAskAgain::class.java -> 7
        PeopleNearbyLoader::class.java -> 8
        else -> 0
    }
}