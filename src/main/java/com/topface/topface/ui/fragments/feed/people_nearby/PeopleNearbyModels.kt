package com.topface.topface.ui.fragments.feed.people_nearby

import com.topface.topface.data.FeedGeo

/**
 * Модели для "Людей рядом"
 */

class PeoplesNearbyStubItem() : FeedGeo()

data class PeopleNearbyEvent(val isPossibleDownload: Boolean)
