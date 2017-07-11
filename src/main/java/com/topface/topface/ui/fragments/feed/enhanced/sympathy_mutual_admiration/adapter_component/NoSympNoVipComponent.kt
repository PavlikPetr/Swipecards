package com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.adapter_component

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.BaseSympathyStubLayoutBinding
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.NoSympNoVipStub
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.stubs.BaseSympathyStubViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент дл стаба "У вас нет симпатий и нет Випа"
 */
class NoSympNoVipComponent(private val mFeedNavigator: FeedNavigator?) : AdapterComponent<BaseSympathyStubLayoutBinding, NoSympNoVipStub>() {

    companion object {
        const val TAG = "no_symphaties_no_vip_stub"
    }

    override val itemLayout: Int
        get() = com.topface.topface.R.layout.base_sympathy_stub_layout
    override val bindingClass: Class<BaseSympathyStubLayoutBinding>
        get() = BaseSympathyStubLayoutBinding::class.java

    private var mViewModel: BaseSympathyStubViewModel? = null

    override fun bind(binding: BaseSympathyStubLayoutBinding, data: NoSympNoVipStub?, position: Int) =
            with(binding) {
                mFeedNavigator?.let {
                    mViewModel = BaseSympathyStubViewModel(R.string.you_have_not_sympathies.getString(), R.string.become_a_vip_and_many_partners_will_see_you.getString(),
                            R.string.chat_auto_reply_button.getString(), R.string.go_to_dating.getString(),
                            { mFeedNavigator.showPurchaseVip(TAG) }, { mFeedNavigator.showDating() })
                }
                viewModel = mViewModel
                root.layoutParams = android.support.v7.widget.StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT).apply { isFullSpan = true }
            }
}