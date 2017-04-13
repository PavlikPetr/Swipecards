package com.topface.topface.ui.fragments.feed.app_day

import android.content.Context
import android.databinding.ObservableField
import com.topface.billing.InstantPurchaseModel
import com.topface.framework.utils.Debug
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.utils.Utils

/**
 * VM итема рекламы
 * Created by siberia87 on 06.10.16.
 */
class AppDayItemViewModel(private val mContext: Context, val image: AppDayImage, private val mInstantPurchaseModel: InstantPurchaseModel) {
    companion object {
        const val TAG_LOG = "app_of_the_day_banner_clicked"
        private const val WEBVIEW = 1;
        private const val BROWSER = 2;
        private const val PRODUCT = 3;
    }

    val iconUrl = ObservableField(image.imgSrc)

    fun onBannerClick() {
        when (image.showType) {
            PRODUCT -> {
                if (image.sku.isEmpty()) openLink(image) else {
                    with(mInstantPurchaseModel) { navigator.showPurchaseProduct(image.sku, from) }
                }
            }
        // ранее тут должно было использоваться поле image.external {@link AppDayImage.external}
        // но оно не использовалось
        // видимо задел на будущее
        // теперь для разделения надо будет использовать WEBVIEW и BROWSER
            WEBVIEW, BROWSER -> {
                openLink(image)
            }
        // в непредвиденной ситуации попробуем тоже открыть линк
            else -> openLink(image)
        }
        AppBannerStatistics.sendBannerClicked(image.id)
        Debug.log(TAG_LOG, image.id)
    }

    private fun openLink(image: AppDayImage) = Utils.goToUrl(mContext, image.url)
}