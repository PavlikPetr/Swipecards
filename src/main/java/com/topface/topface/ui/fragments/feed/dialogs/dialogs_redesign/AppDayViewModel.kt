package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ObservableInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.statistics.AppBannerStatistics
import com.topface.topface.ui.fragments.feed.app_day.AppDayAdapter
import org.jetbrains.anko.doAsync

/**
 * VM для ленты рекламы приложений дня
 * Created by siberia87 on 06.10.16.
 */
class AppDayViewModel() : RecyclerView.OnScrollListener() {

    companion object {
        const val TYPE_FEED_FRAGMENT = "dialog"
        const val TAG_LOG = "app_of_the_day_banner_show"
    }

    var isProgressBarVisible = ObservableInt(View.VISIBLE)
    private var mRes = mutableListOf<Int>()

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        recyclerView ?: return
        val resultSequence = mutableListOf<Int>()
        (recyclerView.layoutManager as LinearLayoutManager).let {
            with(it) {
                findFirstCompletelyVisibleItemPosition()..findLastCompletelyVisibleItemPosition()
            }
                    .toMutableList()
                    .apply {
                        with(resultSequence) {
                            addAll(this@apply)
                            removeAll(mRes.intersect(this@apply))
                        }
                    }
                    .apply { mRes = this }
                    .forEach {
                        Debug.log(TAG_LOG, "id: $it")
                        doAsync {
                            (recyclerView.adapter as AppDayAdapter).getDataItem(it)?.let {
                                Debug.log(TAG_LOG, "send id: $it")
                                AppBannerStatistics.sendBannerShown(it.id)
                            }
                        }
                    }
        }
    }
}

