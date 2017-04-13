package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

/**
 * Linear layout manager для RecyclerView с отключенными скролами. Для "Люди рядом"
 * Created by ppavlik on 16.01.17.
 */

class NonScrolledLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically() = false
    override fun canScrollHorizontally() = false
}