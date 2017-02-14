package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import java.util.concurrent.TimeUnit

/**
 * New album with glide
 * Created by ppavlik on 13.02.17.
 */

class ImageLoader(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    constructor(context: Context) : this(context, null)

    companion object {
        const val PRELOAD_SIZE = 5
    }

    private var mLinks = arrayListOf<String>()
    private var mHeight = 0
    private var mWidth = 0

    private val mPreloadingAdapter: PreloadingAdapter by lazy {
        PreloadingAdapter(mRequestBuilder)
    }

    private val mSnapHelper: PagerSnapHelper by lazy {
        PagerSnapHelper()
    }

    private val mRequestBuilder: DrawableRequestBuilder<String> by lazy {
        Glide.with(context.applicationContext)
                .fromString()
                .fitCenter()
    }

    private val mPreloader: RecyclerViewPreloader<String> by lazy {
        RecyclerViewPreloader(mPreloadingAdapter, mPreloadingAdapter, PRELOAD_SIZE)
    }

    init {
        mSnapHelper.attachToRecyclerView(this)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = mPreloadingAdapter
        addOnScrollListener(mPreloader)
    }

    override fun scrollToPosition(position: Int) {
        super.scrollToPosition(position)
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
        mPreloadingAdapter.clearData()
        mPreloadingAdapter.addData(mLinks)
        mPreloadingAdapter.notifyDataSetChanged()
    }

    private fun getPhotoLink(photo: Photo) =
            if (Math.max(getViewHeight(), getViewWidth()) > 0) {
                photo.getSuitableLink(getViewHeight(), getViewWidth())
            } else {
                photo.defaultLink
            }

    fun setUploadListener(listener: IUploadAlbumPhotos) {
        mPreloadingAdapter.setUploadListener(listener)
    }

    private fun getLink(photos: Photos?, position: Int): String {
        photos?.let {
            it.find { it.getPosition() == position }?.let {
                return getPhotoLink(it)
            }
        }
        return Utils.EMPTY
    }

    fun addPhotos(photos: Photos?) {
        mLinks.forEachIndexed { i, link ->
            if (link.isEmpty()) {
                mLinks.set(i, getLink(photos, i))
            }
        }
        mPreloadingAdapter.clearData()
        mPreloadingAdapter.addData(mLinks)
    }
}