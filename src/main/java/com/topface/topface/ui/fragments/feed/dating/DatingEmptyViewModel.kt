package com.topface.topface.ui.fragments.feed.dating

import android.view.View
import com.topface.topface.R
import kotlinx.android.synthetic.main.layout_empty_dating.*

/**
 * Created by mbulgakov on 07.11.16.
 */
class DatingEmptyViewModel {

    fun onButtonClick(v: View) {
        when (v.id) {
            R.id.btnChangeFilterDating -> doSomething()
            R.id.btnClearFilterDating -> doSomething()
        }
    }

    private fun doSomething() {

    }
}