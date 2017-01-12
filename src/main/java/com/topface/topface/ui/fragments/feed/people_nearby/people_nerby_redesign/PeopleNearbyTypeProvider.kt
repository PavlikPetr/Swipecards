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
    override fun getType(java: Class<*>): Int {
        if (java == PhotoBlogList::class.java) {
            return 1
        }
        if (java == PeopleNearbyList::class.java) {
            return 2
        }
        if (java == PeopleNearbyEmptyList::class.java) {
            return 3
        }
        if (java == PeopleNearbyVipOnly::class.java) {
            return 4
        }
        if (java == PeopleNearbyEmptyLocation::class.java) {
            return 5
        }
        if (java == PeopleNearbyPermissionDenied::class.java) {
            return 6
        }
        if (java == PeopleNearbyPermissionNeverAskAgain::class.java) {
            return 7
        }
        if (java == PeopleNearbyLoader::class.java) {
            return 8
        }
        return 0
    }
}