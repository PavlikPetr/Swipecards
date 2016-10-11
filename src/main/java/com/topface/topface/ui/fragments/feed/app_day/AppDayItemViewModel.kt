package com.topface.topface.ui.fragments.feed.app_day

import android.databinding.ObservableField
import com.topface.topface.databinding.ItemAppDayBinding
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage
import com.topface.topface.utils.Utils
import com.topface.topface.viewModels.BaseViewModel

/**
 * VM итема рекламы
 * Created by siberia87 on 06.10.16.
 */
class AppDayItemViewModel(val binding: ItemAppDayBinding,
                          val image: AppDayImage) :
		BaseViewModel<ItemAppDayBinding>(binding) {

	val iconUrl: ObservableField<String> = ObservableField(image.imgSrc)
	fun onBannerClick() = Utils.goToUrl(context, image.url)
}