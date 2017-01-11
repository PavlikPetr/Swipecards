package com.topface.topface.ui.fragments.feed.people_nearby

import android.databinding.ObservableField
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by mbulgakov on 11.01.17.
 */
class PeopleNearbyListViewModel : RecyclerView.OnScrollListener() {

    var isProgressBarVisible: ObservableField<Int> = ObservableField<Int>(View.VISIBLE)

}