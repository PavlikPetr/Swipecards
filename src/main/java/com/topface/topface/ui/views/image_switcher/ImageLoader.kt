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
import com.topface.framework.utils.Debug
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.glide.RecyclerViewPreloader
import com.topface.topface.utils.Utils
import kotlin.properties.Delegates

/**
 * New album with glide
 * Created by ppavlik on 13.02.17.
 */

class ImageLoader(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    constructor(context: Context) : this(context, null)

    companion object {
        const val PRELOAD_SIZE = 3
    }

    private var mHeight = 0
    private var mWidth = 0
    private var mOnPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mScrollDx = 0
    private var mSelectedPosition by Delegates.observable(0) { prop, old, new ->
        if (old != new) {
            mOnPageChangeListener?.onPageSelected(new)
        }
    }

    private val mPhotoAlbumAdapter: PhotoAlbumAdapter by lazy {
        PhotoAlbumAdapter(mRequestBuilder, mPreloader)
    }

    private val mSnapHelper: SnapHelper by lazy {
        PagerSnapHelper()
    }

    private val mRequestBuilder: DrawableRequestBuilder<String> by lazy {
        Glide.with(context)
                .fromString()
                .fitCenter()
    }

    private val mLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private val mPreloader: RecyclerViewPreloader<String> by lazy {
        RecyclerViewPreloader(mPhotoAlbumAdapter, mPhotoAlbumAdapter, PRELOAD_SIZE)
    }

    init {
        mSnapHelper.attachToRecyclerView(this)
        layoutManager = mLayoutManager
        adapter = mPhotoAlbumAdapter
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
            mSelectedPosition = mLayoutManager.findFirstVisibleItemPosition().apply { Debug.error("${PhotoAlbumAdapter.TAG} position:$this SCROLL_STATE_IDLE") }
        }
        if (state == SCROLL_STATE_SETTLING) {
            mSelectedPosition = if (mScrollDx >= 0) mLayoutManager.findLastVisibleItemPosition().apply { Debug.error("${PhotoAlbumAdapter.TAG} position:$this mScrollDx >= 0") } else mLayoutManager.findFirstVisibleItemPosition().apply { Debug.error("${PhotoAlbumAdapter.TAG} position:$this mScrollDx < 0") }
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
            val links = arrayListOf<String>()
            mPhotoAlbumAdapter.clearData()
            it.forEach {
                it?.let {
                    val link = getPhotoLink(it)
                    links.add(if (link.isNullOrEmpty()) Utils.EMPTY else link)
                } ?: links.add(Utils.EMPTY)
            }
            mPhotoAlbumAdapter.addData(links)
            mPhotoAlbumAdapter.notifyDataSetChanged()
        }
    }

    private fun getPhotoLink(photo: Photo) =
            if (Math.max(getViewHeight(), getViewWidth()) > 0) {
                photo.getSuitableLink(getViewHeight(), getViewWidth())
            } else {
                photo.defaultLink
            }

    private fun getLink(photos: Photos?, position: Int): String {
        photos?.let {
            it.find { it.getPosition() == position }?.let {
                return getPhotoLink(it)
            }
        }
        return Utils.EMPTY
    }

    fun setCurrentItemSmoothly(position: Int) {
        smoothScrollToPosition(position)
    }

    fun setCurrentItemImmediately(position: Int) {
        mSelectedPosition = position
        Debug.error("${PhotoAlbumAdapter.TAG} position:$position setCurrentItemImmediately")
        scrollToPosition(position)
    }

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    fun getSelectedPosition() = mSelectedPosition

    fun setIsSecondImagePreloadAvailable(isAvailable: Boolean) {
        mPhotoAlbumAdapter.setIsSecondImagePreloadAvailable(isAvailable)
    }
}