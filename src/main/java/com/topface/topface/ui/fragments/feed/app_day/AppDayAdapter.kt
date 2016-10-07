package com.topface.topface.ui.fragments.feed.app_day

import android.os.Bundle
import android.view.ViewGroup
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.ItemAppDayBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage

/**
 * Адаптер для итемов рекламы апы дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayAdapter : BaseRecyclerViewAdapter<ItemAppDayBinding, AppDayImage>() {

	override fun bindData(binding: ItemAppDayBinding?, position: Int) {
		Debug.log("AppDayAdapter","bindData position = $position")
		binding?.let { bind ->
			getDataItem(position)?.let {
				bind.viewModel = AppDayItemViewModel(bind, it)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
		Debug.log("AppDayAdapter","onCreateViewHolder")
		return super.onCreateViewHolder(parent, viewType)
	}

	override fun getItemCount(): Int {
		Debug.log("AppDayAdapter","getItemCount")
		return super.getItemCount()
	}

	override fun getItemLayout():Int {
		Debug.log("AppDayAdapter","getItemLayout")
		return R.layout.item_app_day
	}

	override fun getUpdaterEmitObject(): Bundle? = null
}