package com.topface.topface.ui.fragments.feed.dialogs

import android.content.Context
import android.databinding.ObservableField
import com.topface.topface.data.Options
import com.topface.topface.utils.Utils

/**
 * VM приложения дня
 * Created by tiberal on 19.09.16.
 */
class DialogAppOfTheDayModel(val context: Context, val appOfTheDay: Options.AppOfTheDay) {
    val title: ObservableField<String> = ObservableField(appOfTheDay.title)
    val description: ObservableField<String> = ObservableField(appOfTheDay.description)
    val iconUrl: ObservableField<String> = ObservableField(appOfTheDay.iconUrl)
    fun onBannerClick() = Utils.goToUrl(context, appOfTheDay.targetUrl)
}