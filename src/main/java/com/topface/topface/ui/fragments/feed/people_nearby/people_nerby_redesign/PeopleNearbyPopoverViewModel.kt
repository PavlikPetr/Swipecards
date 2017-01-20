package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.App
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator

/**
 * Моделька для popover экрана Люди рядом
 * Created by petrp on 20.01.2017.
 */
class PeopleNearbyPopoverViewModel(private val mNavigator: FeedNavigator,
                                   private val close: () -> Unit) {
    fun addToLeaderClick() {
        closeClick()
        if (App.get().profile.photo.isEmpty) {
            mNavigator.showTakePhotoPopup()
        } else {
            mNavigator.showAddToLeader()
        }
    }

    fun closeClick() {
        close.invoke()
    }
}