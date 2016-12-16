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
    private var filterImgRes = 0

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu != null && inflater != null) {
            menu.clear()
            inflater.inflate(R.menu.actions_dating, menu)
            mFilterItem = menu.findItem(R.id.action_dating_filter)
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
        (if (isVisible) R.drawable.filter_gray else R.drawable.filter_white).let {
            if (filterImgRes != it && setOptionMenuImage(mFilterItem, it)) {
                filterImgRes = it
            }
        }
    }

    private fun setOptionMenuImage(menuItem: MenuItem?, imgRes: Int): Boolean {
        menuItem?.let { menu ->
            imgRes.getDrawable()?.let {
                menu.icon = it
                return true
            }
        }
        return false
    }
}