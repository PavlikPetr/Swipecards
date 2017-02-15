package com.topface.topface.glide

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView

/**
 * Converts [android.support.v7.widget.RecyclerView.OnScrollListener] events to
 * [AbsListView] scroll events.

 *
 * Requires that the the recycler view be using a [LinearLayoutManager] subclass.
 */
class RecyclerToListViewScrollListener(private val scrollListener: AbsListView.OnScrollListener) : RecyclerView.OnScrollListener() {
    private var lastFirstVisible = -1
    private var lastVisibleCount = -1
    private var lastItemCount = -1

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        val listViewState: Int
        when (newState) {
            RecyclerView.SCROLL_STATE_DRAGGING -> listViewState = AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
            RecyclerView.SCROLL_STATE_IDLE -> listViewState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE
            RecyclerView.SCROLL_STATE_SETTLING -> listViewState = AbsListView.OnScrollListener.SCROLL_STATE_FLING
            else -> listViewState = UNKNOWN_SCROLL_STATE
        }

        scrollListener.onScrollStateChanged(null /*view*/, listViewState)
    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager

        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val visibleCount = Math.abs(firstVisible - layoutManager.findLastVisibleItemPosition())
        val itemCount = recyclerView.adapter.itemCount

        if (firstVisible != lastFirstVisible || visibleCount != lastVisibleCount
                || itemCount != lastItemCount) {
            scrollListener.onScroll(null, firstVisible, visibleCount, itemCount)
            lastFirstVisible = firstVisible
            lastVisibleCount = visibleCount
            lastItemCount = itemCount
        }
    }

    fun startPreload(firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) =
            scrollListener.onScroll(null, firstVisibleItem, visibleItemCount, totalItemCount)

    companion object {
        val UNKNOWN_SCROLL_STATE = Integer.MIN_VALUE
    }
}