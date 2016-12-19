package com.topface.topface.ui.fragments.feed.app_day

import android.content.Context
import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.utils.Utils

/**
 * VM итема рекламы
 * Created by siberia87 on 06.10.16.
 */
class AppDayItemViewModel(private val mContext: Context, val image: AppDayImage) {
    val TAG_LOG = "app_of_the_day_banner_clicked"

    val iconUrl = ObservableField(image.imgSrc)
    fun onBannerClick() {
        Utils.goToUrl(mContext, image.url)
        AppBannerStatistics.sendBannerClicked(image.id)
        Debug.log(TAG_LOG, image.id)
    }
}