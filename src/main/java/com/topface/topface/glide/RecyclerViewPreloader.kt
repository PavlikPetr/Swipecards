package com.topface.topface.glide

import android.support.v7.widget.RecyclerView
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.topface.topface.glide.module.ListPreloader


class RecyclerViewPreloader<T>(preloadModelProvider: PreloadModelProvider<T>,
                               preloadDimensionProvider: PreloadSizeProvider<T>, maxPreload: Int) : RecyclerView.OnScrollListener() {
    private var recyclerScrollListener: RecyclerToListViewScrollListener

    private val mListPreloader: ListPreloader<T> by lazy {
        ListPreloader<T>(preloadModelProvider,
                preloadDimensionProvider, maxPreload)
    }

    init {
        recyclerScrollListener = RecyclerToListViewScrollListener(mListPreloader);

    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        recyclerScrollListener.onScrolled(recyclerView, dx, dy)
    }

    fun startPreloadSecondItem() {
        mListPreloader.preloadSecondImage()
    }
}