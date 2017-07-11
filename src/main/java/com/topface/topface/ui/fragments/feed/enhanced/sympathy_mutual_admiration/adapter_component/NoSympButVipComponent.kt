package com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.adapter_component

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.BaseSympathyStubLayoutBinding
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.NoSympButVipStub
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.stubs.BaseSympathyStubViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент дл стаба "У вас нет симпатий но есть вип"
 */
class NoSympButVipComponent(private val mFeedNavigator: FeedNavigator?) : AdapterComponent<BaseSympathyStubLayoutBinding, NoSympButVipStub>() {

    override val itemLayout: Int
        get() = com.topface.topface.R.layout.base_sympathy_stub_layout
    override val bindingClass: Class<BaseSympathyStubLayoutBinding>
        get() = BaseSympathyStubLayoutBinding::class.java

    private var mViewModel: BaseSympathyStubViewModel? = null

    override fun bind(binding: BaseSympathyStubLayoutBinding, data: NoSympButVipStub?, position: Int) =
            with(binding) {
                mFeedNavigator?.let {
                    mViewModel = BaseSympathyStubViewModel(R.string.you_have_not_sympathies.getString(), R.string.go_to_dating_and_rate_people.getString(),
                            R.string.go_to_dating.getString(), R.string.go_to_guests.getString(),
                            { mFeedNavigator.showDating() }, { mFeedNavigator.showVisitors() })
                }
                viewModel = mViewModel
                root.layoutParams = android.support.v7.widget.StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT).apply { isFullSpan = true }
            }
}