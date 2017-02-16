package com.topface.topface.ui

import android.support.design.widget.TabLayout

/**
 * Interface for activities with tabs in toolbar
 * TODO burn it with fire when tabbed fragments became useless
 * Created by m.bayutin on 16.02.17.
 */
interface ITabLayoutHolder {
    fun getTabLayout(): TabLayout?
    fun showTabLayout(show: Boolean)
}