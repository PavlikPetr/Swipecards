package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.AbsListView
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.utils.Utils

/**
 * Created by ppavlik on 13.02.17.
 */

class ImageLoader(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    constructor(context: Context) : this(context, null)

    private var mLinks = mutableListOf<String>()
    private var mHeight = 0
    private var mWidth = 0

    private val mPreloadingAdapter: PreloadingAdapter by lazy {
        PreloadingAdapter(mRequestBuilder)
    }

    private val mScrollListener: AbsListView.OnScrollListener by lazy {
        mPreloadingAdapter.preload(3)
    }

    private val mRequestBuilder: DrawableRequestBuilder<String> by lazy {
        Glide.with(context.applicationContext)
                .fromString()
                .fitCenter() // must be explicit, otherwise there's a conflict between
                // into(ImageView) and into(Target) which may lead to cache misses
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })
    }


    init {
        adapter = mPreloadingAdapter
        setOnScrollChangeListener(RecyclerToListViewScrollListe(mPreloadingAdapter.preload(3)))
//        setOnScrollChangeListener(mPreloadingAdapter.preload(3))
    }

    private fun getViewHeight(): Int {
        if (mHeight == 0) {
            mHeight = measuredHeight
        }
        return mHeight
    }

    private fun getViewWidth(): Int {
        if (mWidth == 0) {
            mWidth = measuredHeight
        }
        return mWidth
    }

    fun setData(photos: Photos) = photos.forEach {
        it?.let {
            val link = getPhotoLink(it)
            mLinks.add(if (link.isNullOrEmpty()) Utils.EMPTY else link)
        } ?: mLinks.add(Utils.EMPTY)
    }.run {
        mPreloadingAdapter.setData(mLinks)
        mPreloadingAdapter.notifyDataSetChanged()
    }

    private fun getPhotoLink(photo: Photo) =
            if (Math.max(getViewHeight(), getViewWidth()) > 0) {
                photo.getSuitableLink(getViewHeight(), getViewWidth())
            } else {
                photo.defaultLink
            }
}