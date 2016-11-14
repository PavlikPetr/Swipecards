package com.topface.topface.ui.fragments.dating

import com.topface.topface.data.search.SearchUser

/**
 * Listen actions in album viewModel
 * Created by ppavlik on 08.11.16.
 */
interface IDatingAlbumView {
    fun onUserShow(user: SearchUser)
}