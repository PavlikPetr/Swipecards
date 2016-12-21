package com.topface.topface.ui.fragments.feed.dialogs

import android.content.Context
import android.databinding.ViewDataBinding
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.databinding.FeedItemDialogBinding
import com.topface.topface.ui.fragments.feed.app_day.AppDayViewModel
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class DialogsAdapter(private val mNavigator: IFeedNavigator, private val contextTemp: Context) :
        BaseFeedAdapter<FeedItemDialogBinding, FeedDialog>() {

    override fun getItemLayout() = R.layout.feed_item_dialog

    override fun bindData(binding: FeedItemDialogBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                binding.model = DialogsItemViewModel(bind, it, mNavigator) { isActionModeEnabled }
            }
        }
    }

    override fun bindHeader(binding: ViewDataBinding?, position: Int) {
        binding?.let { bind ->
            (getHeaderItem(position) as? List<AppDayImage>)?.let {
                bind.setVariable(BR.viewModel, AppDayViewModel(bind as AppDayListBinding, it))
            }
        }
    }
}