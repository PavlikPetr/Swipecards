package com.topface.topface.ui.fragments.feed.fans

import android.databinding.ObservableField
import android.view.View
import com.topface.topface.databinding.LayoutEmptyFansBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked


class FansLockScreenViewModel(binding: LayoutEmptyFansBinding, val mNavigator: IFeedNavigator, mIFeedUnlocked: IFeedUnlocked) :
        BaseLockScreenViewModel<LayoutEmptyFansBinding>(binding, mIFeedUnlocked) {

    private var onButtonClickListener: View.OnClickListener? = null
    val title = ObservableField<String>("")
    val buttonText = ObservableField<String>("")

    fun onButtonClick(view: View) {
        onButtonClickListener?.onClick(view)
    }

    fun setOnButtonClickListener(listener: View.OnClickListener) {
        onButtonClickListener = listener;
    }

    override fun release() {
        super.release()
        onButtonClickListener = null
    }
}