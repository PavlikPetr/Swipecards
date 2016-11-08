package com.topface.topface.ui.views.toolbar

import android.support.annotation.DrawableRes

data class ToolbarSettingsData @JvmOverloads constructor(val title: String? = null,
                                                         val subtitle: String? = null,
                                                         @DrawableRes val icon: Int? = null,
                                                         val isOnline: Boolean? = null)