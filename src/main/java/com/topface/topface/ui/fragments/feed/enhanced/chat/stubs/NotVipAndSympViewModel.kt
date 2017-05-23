package com.topface.topface.ui.fragments.feed.enhanced.chat.stubs

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

class NotVipAndSympViewModel(feedUser: FeedUser, private val mFeedNavigator: FeedNavigator?) : BaseBuyVipViewModel(feedUser) {

    companion object{
        private const val TAG = "not_vip_and_symp_chat_stub"
    }

    override val title: ObservableField<String>
        get() = ObservableField(R.string.chat_no_mutual_symphaty.getString())
    override val stubText: ObservableField<String>
        get() = ObservableField(R.string.buy_vip_to_chat.getString())
    override val buttonText: ObservableField<String>
        get() = ObservableField(R.string.chat_activate_vip.getString())

    override fun buttonAction() = mFeedNavigator?.showPurchaseVip(TAG)

}