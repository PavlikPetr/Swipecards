package com.topface.topface.ui.fragments.feed.dating

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

interface IOptionMenuCallback {
    fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?)
    fun onOptionsItemSelected(item: MenuItem?): Boolean
}