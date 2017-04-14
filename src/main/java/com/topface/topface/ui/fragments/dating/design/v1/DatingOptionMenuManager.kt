package com.topface.topface.ui.fragments.dating.design.v1

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.topface.topface.R
import com.topface.topface.ui.fragments.dating.IOptionMenuCallback
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.toolbar.IAppBarState
import com.topface.topface.utils.extensions.getDrawable

/**
 * Рулим options меню в новых знакомствах
 * Created by ppavlik on 11.11.16.
 */
class DatingOptionMenuManager(private val mNavigator: IFeedNavigator) : IOptionMenuCallback {

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu != null && inflater != null) {
            menu.clear()
            inflater.inflate(R.menu.actions_dating, menu)
            menu.findItem(R.id.action_dating_filter).apply { icon = R.drawable.filter_white.getDrawable() }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
            when (item?.itemId) {
                R.id.action_dating_filter -> {
                    mNavigator.showFilter()
                    true
                }
                else -> false
            }
}