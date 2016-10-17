package com.topface.topface.ui.views.toolbar

import android.support.annotation.DrawableRes

/**
 * Created by petrp on 09.10.2016.
 */
data class ToolbarSettingsData @JvmOverloads constructor(val title: String? = null,
                                                         val subtitle: String? = null,
                                                         @DrawableRes val icon: Int? = null,
                                                         val isOnline: Boolean? = null)