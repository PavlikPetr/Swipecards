package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.databinding.LayoutEmptyDialogsBinding
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.viewModels.BaseViewModel

class DialogsLockScreenViewModel(binding: LayoutEmptyDialogsBinding, private val mNavigator: IFeedNavigator, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<LayoutEmptyDialogsBinding>(binding) {

    fun showPurchaseVip() = mNavigator.showPurchaseCoins()

    fun showDating() = mNavigator.showDating()
}