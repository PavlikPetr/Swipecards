package com.topface.topface.ui.fragments.feed.feed_utils

import android.support.annotation.DrawableRes
import com.topface.topface.data.Photo

/** Дата классы для фидов
 * Created by tiberal on 10.08.16.
 */

data class AgeAndNameData(val name: String?, val age: String?, val iconRes: Int)

data class AvatarHolder(val photo: Photo?,@DrawableRes val stubResource: Int)


