package com.topface.topface.ui.views.image_switcher

import android.support.v7.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.ListPreloader.PreloadModelProvider


class RecyclerViewPreloader<T>(preloadModelProvider: PreloadModelProvider<T>,
                               preloadDimensionProvider: PreloadSizeProvider<T>, maxPreload: Int) : RecyclerView.OnScrollListener() {
    private var recyclerScrollListener: RecyclerToListViewScrollListener

    init {
        val listPreloader = ListPreloader<T>(preloadModelProvider,
                preloadDimensionProvider, maxPreload);
        recyclerScrollListener = RecyclerToListViewScrollListener(listPreloader);

    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        recyclerScrollListener.onScrolled(recyclerView, dx, dy)
    }
}