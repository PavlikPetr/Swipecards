package com.topface.topface.ui.fragments.feed.dialogs

import android.content.Context
import com.topface.topface.data.Options
import com.topface.topface.utils.Utils

/**
 * VM приложения дня
 * Created by tiberal on 19.09.16.
 */
class DialogAppOfTheDayModel(val context: Context, val appOfTheDay: Options.AppOfTheDay) {

    fun onBannerClick() = Utils.goToUrl(context, appOfTheDay.targetUrl)

}