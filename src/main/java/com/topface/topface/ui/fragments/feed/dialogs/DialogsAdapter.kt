package com.topface.topface.ui.fragments.feed.dialogs

import android.content.Context
import android.databinding.ViewDataBinding
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.databinding.FeedItemDialogBinding
import com.topface.topface.ui.fragments.feed.app_day.AppDayAdapter
import com.topface.topface.ui.fragments.feed.app_day.AppDayViewModel
import com.topface.topface.ui.fragments.feed.app_day.models.AppDay
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import java.util.*

class DialogsAdapter(private val mNavigator: IFeedNavigator, private val contextTemp: Context) : BaseFeedAdapter<FeedItemDialogBinding, FeedDialog>() {

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
		binding?.let {
			it.setVariable(BR.viewModel, AppDayViewModel((it as AppDayListBinding), (getHeaderItem(position) as AppDay).result))
		}
	}
}