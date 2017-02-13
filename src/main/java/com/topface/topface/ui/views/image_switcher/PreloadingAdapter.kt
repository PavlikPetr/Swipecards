package com.topface.topface.ui.views.image_switcher

import android.support.v4.view.PagerAdapter
import android.view.View
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.ListPreloader

/**
 * Created by ppavlik on 13.02.17.
 */

class  PreloadingAdapter : PagerAdapter(), ListPreloader.PreloadModelProvider<String>{
    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCount(): Int {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPreloadItems(position: Int): MutableList<String> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPreloadRequestBuilder(item: String?): GenericRequestBuilder<*, *, *, *> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}