package com.topface.topface.ui.views.image_switcher

import android.databinding.ObservableInt
import android.view.View

/**
 * VM для картинки альбома
 * Created by ppavlik on 15.02.17.
 */

class AlbumImageViewModel(val onClickListener: View.OnClickListener?) {
    val isProgressVisible = ObservableInt()

    fun onClick(view: View) {
        onClickListener?.onClick(view)
    }
}