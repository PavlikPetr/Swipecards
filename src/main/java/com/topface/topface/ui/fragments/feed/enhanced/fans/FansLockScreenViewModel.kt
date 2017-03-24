package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.databinding.ObservableField
import android.view.View
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked

class FansLockScreenViewModel(mIFeedUnlocked: IFeedUnlocked) :
        BaseLockScreenViewModel(mIFeedUnlocked) {

    private var onButtonClickListener: View.OnClickListener? = null
    val title = ObservableField<String>("")
    val buttonText = ObservableField<String>("")

    fun onButtonClick(view: View) {
        onButtonClickListener?.onClick(view)
    }

    fun setOnButtonClickListener(listener: View.OnClickListener) {
        onButtonClickListener = listener
    }

    override fun release() {
        super.release()
        onButtonClickListener = null
    }
}