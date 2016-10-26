package com.topface.topface.ui.fragments.feed.app_day

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.ItemAppDayBinding
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.utils.Utils
import com.topface.topface.viewModels.BaseViewModel

/**
 * VM итема рекламы
 * Created by siberia87 on 06.10.16.
 */
class AppDayItemViewModel(val binding: ItemAppDayBinding, val image: AppDayImage) :
        BaseViewModel<ItemAppDayBinding>(binding) {
    val TAG_LOG = "banner_clicked"

    val iconUrl = ObservableField(image.imgSrc)
    fun onBannerClick() {
        Utils.goToUrl(context, image.url)
        AppBannerStatistics.sendBannerClicked(image.id)
        Debug.log(TAG_LOG, image.id)
    }
}