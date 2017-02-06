package com.topface.billing

import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Models for purchases
 */

data class InstantPurchaseModel(val navigator: IFeedNavigator, val from: String, val isSubscription: Boolean = false)
