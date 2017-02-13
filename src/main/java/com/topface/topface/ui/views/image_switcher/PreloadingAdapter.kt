package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.databinding.ViewDataBinding
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.integration.volley.VolleyGlideModule
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.ui.new_adapter.enhanced.ViewHolder
import com.topface.topface.utils.Utils
import java.util.*

/**
 * Created by ppavlik on 13.02.17.
 */

class PreloadingAdapter(private val mRequest: GenericRequestBuilder<String, *, *, *>) : RecyclerView.Adapter<ViewHolder<ViewDataBinding>>(), PreloadModelProvider<String> {
    override fun onBindViewHolder(holder: ViewHolder<ViewDataBinding>?, position: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder<ViewDataBinding> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount() = mLinks.size

    private var mLinks = mutableListOf<String>()
    private val mSizeProvider: ViewPreloadSizeProvider<String> by lazy {
        ViewPreloadSizeProvider<String>()
    }

    fun setData(links: MutableList<String>) {
        mLinks = links
    }

    override fun getPreloadItems(position: Int): List<String> {
        Debug.error("TEST_TEST getPreloadItems")
        return Collections.singletonList(mLinks.getOrElse(position) { Utils.EMPTY })
    }

    override fun getPreloadRequestBuilder(item: String?): GenericRequestBuilder<String, *, *, *> {
        Debug.error("TEST_TEST getPreloadRequestBuilder")
        return mRequest.load(item)
    }

    fun preload(maxPreload: Int): AbsListView.OnScrollListener {
        Debug.error("TEST_TEST preload")
        return ListPreloader(this, mSizeProvider, maxPreload)
    }

}