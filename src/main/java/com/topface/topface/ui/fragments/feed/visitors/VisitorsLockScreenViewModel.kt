package com.topface.topface.ui.fragments.feed.visitors

import android.databinding.ObservableField
import android.view.View
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseLockScreenViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked

/**
 * Моделька локскрина гостей
 * Created by tiberal on 09.09.16.
 */
class VisitorsLockScreenViewModel(binding: LayoutEmptyVisitorsBinding, val mNavigator: IFeedNavigator, mIFeedUnlocked: IFeedUnlocked) :
        BaseLockScreenViewModel<LayoutEmptyVisitorsBinding>(binding, mIFeedUnlocked) {

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