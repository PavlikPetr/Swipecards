package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.support.annotation.IntDef
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.util.AttributeSet
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.glide.RecyclerViewPreloader
import com.topface.topface.utils.Utils
import kotlin.properties.Delegates

/**
 * New album with glide
 * Created by ppavlik on 13.02.17.
 */

class ImageLoader : RecyclerView {

    companion object {
        const val PRELOAD_SIZE = 3 // указываем кол-во фоток которые будем предзагружать в кеш

        const val CROP_TYPE_NONE = 0L
        const val CROP_TYPE_MATCH_VIEW = 1L

        @IntDef(CROP_TYPE_NONE, CROP_TYPE_MATCH_VIEW)
        annotation class CropType
    }

    private var mHeight = 0
    private var mWidth = 0
    private var mOnPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mScrollDx = 0
    @CropType
    private var mCropType = CROP_TYPE_NONE
    private var mIsReadyToPreload = false
    // по умолчанию загрузку пачки фото на старте 1..PRELOAD_SIZE разрешаем
    private var mIsNeedToPreloadOnStart = true
    private var mSelectedPosition by Delegates.observable(0) { prop, old, new ->
        if (old != new) {
            mOnPageChangeListener?.onPageSelected(new)
        }
    }

    private val mPhotoAlbumAdapter: PhotoAlbumAdapter by lazy {
        PhotoAlbumAdapter(mRequestBuilder, mCropType) {
            mIsReadyToPreload = true
            preload()
        }
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

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr) {
        attrs?.let {
            parseAttribute(it, defStyleAttr)
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.let {
            parseAttribute(it, defStyleAttr)
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.let {
            parseAttribute(it, 0)
        }
    }

    constructor(context: Context) : super(context)

    private fun parseAttribute(attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ImageLoader, defStyleAttr, 0)
        setCropType(a.getInt(R.styleable.ImageLoader_cropType, CROP_TYPE_NONE.toInt()).toLong())
        a.recycle()
    }

    fun setCropType(@CropType cropType: Long) {
        mCropType = cropType
        mPhotoAlbumAdapter.setCropType(mCropType)
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
            mSelectedPosition = mLayoutManager.findFirstVisibleItemPosition()
        }
        if (state == SCROLL_STATE_SETTLING) {
            mSelectedPosition = if (mScrollDx >= 0) mLayoutManager.findLastVisibleItemPosition() else mLayoutManager.findFirstVisibleItemPosition()
        }
    }

    private fun getViewHeight() =
            if (mHeight == 0) {
                mHeight = measuredHeight
                mHeight
            } else mHeight

    private fun getViewWidth() =
            if (mWidth == 0) {
                mWidth = measuredHeight
                mWidth
            } else mWidth

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

    private fun getLink(photos: Photos?, position: Int) = photos?.let {
        it.find { it.getPosition() == position }?.let {
            getPhotoLink(it)
        }
    } ?: Utils.EMPTY

    fun setCurrentItemSmoothly(position: Int) {
        smoothScrollToPosition(position)
    }

    fun setCurrentItemImmediately(position: Int) {
        mSelectedPosition = position
        scrollToPosition(position)
    }

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    fun getSelectedPosition() = mSelectedPosition

    fun setNecessityPreloadOnStart(isNeedToPreloadOnStart: Boolean) {
        mIsNeedToPreloadOnStart = isNeedToPreloadOnStart
        preload()
    }

    private fun preload() {
        if (mIsReadyToPreload && mIsNeedToPreloadOnStart) {
            mIsNeedToPreloadOnStart = false
            mPreloader.startPreloadSecondItem(mPhotoAlbumAdapter.itemCount)
        }
    }

    override fun onDetachedFromWindow() {
        mPhotoAlbumAdapter.release()
        adapter = null
        super.onDetachedFromWindow()
    }
}