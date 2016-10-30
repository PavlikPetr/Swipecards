package com.topface.topface.ui.fragments.feed.dating

import com.topface.topface.data.search.SearchUser

/**
 * dating fragment events
 * Created by tiberal on 12.10.16.
 */
interface IDatingViewModelEvents {
    fun onDataReceived(user: SearchUser)
}