package com.topface.topface.ui.fragments.feed.app_day

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage
import com.topface.topface.viewModels.BaseViewModel

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel(binding: AppDayListBinding) : BaseViewModel<AppDayListBinding>(binding) {
	val isProgressBarVisible = ObservableInt(View.INVISIBLE)
	val mAdapter = AppDayAdapter()
	val imageArray by lazy {
		arrayListOf(
				AppDayImage("http://batona.net/uploads/posts/2011-08/1313578288_15.jpg",
						"http://batona.net/uploads/posts/2011-08/1313578288_15.jpg",
						false),
				AppDayImage("https://www.android.com/static/img/android.png",
						"https://www.android.com/static/img/android.png",
						false)
		)
	}

	init {
		mAdapter.addData(imageArray)
		binding.bannerList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
		binding.bannerList.adapter = mAdapter
	}
}