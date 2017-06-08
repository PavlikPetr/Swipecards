package com.topface.topface.ui.fragments.feed.enhanced.chat.stubs

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

class BuyVipStubViewModel(private var mFeedUser: FeedUser?, private var mFeedNavigator: FeedNavigator?) : BaseBuyVipViewModel(mFeedUser) {

    companion object {
        private const val TAG = "buy_vip_chat_stub"
    }

    private val mIsMan = mFeedUser?.sex == User.BOY

    override val title: ObservableField<String>
        get() = ObservableField(getTitle())
    override val stubText: ObservableField<String>
        get() = ObservableField(getStubText())
    override val buttonText: ObservableField<String>
        get() = ObservableField(R.string.get_vip.getString())

    override fun buttonAction() = mFeedNavigator?.showPurchaseVip(TAG)

    private fun getTitle() =
            String.format(if (mIsMan) R.string.chat_buy_vip_popular_male.getString()
                                else R.string.chat_buy_vip_popular_female.getString(), mFeedUser?.firstName)

    private fun getStubText() = if (mIsMan) R.string.write_to_him_only_vip.getString() else R.string.write_to_her_only_vip.getString()

    override fun unbind(){
        mFeedNavigator = null
    }

}