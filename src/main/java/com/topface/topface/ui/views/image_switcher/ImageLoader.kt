package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.util.AttributeSet
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.glide.RecyclerViewPreloader
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos
import com.topface.topface.utils.Utils
import kotlin.properties.Delegates

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
    private var mOnPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mScrollDx = 0
    private var mSelectedPosition by Delegates.observable(0) { prop, old, new ->
        if (old != new) {
            mOnPageChangeListener?.onPageSelected(new)
        }
    }

    private val mPreloadingAdapter: PreloadingAdapter by lazy {
        PreloadingAdapter(mRequestBuilder)
    }

    private val mSnapHelper: SnapHelper by lazy {
        PagerSnapHelper()
    }

    private val mRequestBuilder: DrawableRequestBuilder<String> by lazy {
        Glide.with(context.applicationContext)
                .fromString()
                .fitCenter()
    }

    private val mLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private val mPreloader: RecyclerViewPreloader<String> by lazy {
        RecyclerViewPreloader(mPreloadingAdapter, mPreloadingAdapter, PRELOAD_SIZE)
    }

    init {
        mSnapHelper.attachToRecyclerView(this)
        layoutManager = mLayoutManager
        adapter = mPreloadingAdapter
        addOnScrollListener(mPreloader)
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        mScrollDx = dx
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        mOnPageChangeListener?.onPageScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            mSelectedPosition = mLayoutManager.findFirstVisibleItemPosition()
        }
        if (state == SCROLL_STATE_SETTLING) {
            mSelectedPosition = if (mScrollDx >= 0) mLayoutManager.findLastVisibleItemPosition() else mLayoutManager.findFirstVisibleItemPosition()
        }
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

    fun setData(photos: Photos?) {
        photos?.let {
            mLinks.clear()
            mPreloadingAdapter.clearData()
            it.forEach {
                it?.let {
                    val link = getPhotoLink(it)
                    mLinks.add(if (link.isNullOrEmpty()) Utils.EMPTY else link)
                } ?: mLinks.add(Utils.EMPTY)
            }
            mPreloadingAdapter.addData(mLinks)
            mPreloadingAdapter.notifyDataSetChanged()
//            mPreloader.startPreload(1, 2, mPreloadingAdapter.data.size)
        }
    }

    private fun getPhotoLink(photo: Photo) = photo.defaultLink
//            if (Math.max(getViewHeight(), getViewWidth()) > 0) {
//                photo.getSuitableLink(getViewHeight(), getViewWidth())
//            } else {
//                photo.defaultLink
//            }

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

    fun setPhotos(photos: Photos?) {
        mLinks.forEachIndexed { i, link ->
            if (link.isEmpty()) {
                mLinks.set(i, getLink(photos, i))
            }
        }
        mPreloadingAdapter.clearData()
        mPreloadingAdapter.addData(mLinks)
    }

    fun setCurrentItemSmoothly(position: Int) {
        smoothScrollToPosition(position)
    }

    fun setCurrentItemImmediately(position: Int) {
        scrollToPosition(position)
    }

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    fun getSelectedPosition() = mSelectedPosition
}