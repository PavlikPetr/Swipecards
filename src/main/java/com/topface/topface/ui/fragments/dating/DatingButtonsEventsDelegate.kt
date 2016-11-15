package com.topface.topface.ui.fragments.dating

import com.topface.topface.data.search.SearchUser

/**
 * Created by tiberal on 12.10.16.
 */
interface DatingButtonsEventsDelegate {
    fun showTakePhoto()
    fun onNewSearchUser(user: SearchUser)
}
