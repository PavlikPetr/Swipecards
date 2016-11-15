package com.topface.topface.ui.fragments.dating

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.toolbar.IAppBarState
import com.topface.topface.utils.extensions.getDrawable

/**
 * Рулим options меню в знакомствах
 * Created by ppavlik on 11.11.16.
 */
//TODO закоментил кнопку cebab-menu до реализации выпадающего списка
class DatingOptionMenuManager(private val mNavigator: IFeedNavigator) : IAppBarState, IOptionMenuCallback {
    private var mFilterItem: MenuItem? = null
    //    private var mOptionMenuItem: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu != null && inflater != null) {
            menu.clear()
            inflater.inflate(R.menu.actions_dating, menu)
            mFilterItem = menu.findItem(R.id.action_dating_filter)
            //        mOptionMenuItem = menu.findItem(R.id.action_dating_options)
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

    override fun isScrimVisible(isVisible: Boolean) {
        //        mOptionMenuItem?.let {
//            it.icon = if (isVisible) R.drawable.ic_cebab_gray.getDrawable() else R.drawable.ic_cebab_white.getDrawable()
//        }
        mFilterItem?.let {
            it.icon = if (isVisible) R.drawable.filter_gray.getDrawable() else R.drawable.filter_white.getDrawable()
        }
    }
}