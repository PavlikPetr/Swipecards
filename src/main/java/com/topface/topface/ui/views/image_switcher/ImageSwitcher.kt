package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.topface.topface.data.Photos
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos

/**
 * ImageSwitcher на Glide
 * Created by petrp on 08.02.2017.
 */

class ImageSwitcher(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    constructor(context: Context) : this(context, null)

    companion object {
        const val TAG = "NewImageLoader"
        const val VIEW_TAG = "view_container"
    }

    private var mOnClickListener: OnClickListener? = null


    private var mCurrentPhotoPosition = 0
    private var mPreviousPhotoPosition = 0

    private val mGestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                mOnClickListener?.onClick(this@ImageSwitcher)
                return false
            }
        })
    }

    private val mImageSwitcherAdapter: ImageSwitcherAdapter by lazy {
        ImageSwitcherAdapter(context, this,
                { currentItem },
                { mOnClickListener?.onClick(this@ImageSwitcher) })
    }

    init {
        adapter = mImageSwitcherAdapter
        setOnTouchListener({ v, event ->
            mGestureDetector.onTouchEvent(event)
            false
        })
        pageMargin = 40
    }

    fun setData(photoLinks: Photos) {
        this.adapter = mImageSwitcherAdapter.apply {
            data = photoLinks
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        val offset = computeHorizontalScrollOffset()
        val range = computeHorizontalScrollRange() - computeHorizontalScrollExtent()
        if (range == 0) return false
        if (direction < 0) {
            return offset > 0
        } else {
            return offset < range - 1
        }
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        mOnClickListener = onClickListener
    }

    override fun addOnPageChangeListener(finalListener: OnPageChangeListener) {
        super.addOnPageChangeListener(object : OnPageChangeListener {

            override fun onPageScrolled(i: Int, v: Float, i1: Int) {
                finalListener.onPageScrolled(i, v, i1)
            }

            override fun onPageSelected(i: Int) {
                setSelectedPosition(i)
                mImageSwitcherAdapter.setPhotoToPosition(i)
                finalListener.onPageSelected(i)
            }

            override fun onPageScrollStateChanged(i: Int) {
                finalListener.onPageScrollStateChanged(i)
            }
        })
    }

    fun needAnimateLoader(animate: Boolean) {
        mImageSwitcherAdapter.needAnimateLoader(animate)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            //Мы отлавливаем эту ошибку из-за бага в support library, который кидает такие ошибки:
            //IllegalArgumentException: pointerIndex out of range
            //Debug.error(e);
            return false
        } catch (e: IndexOutOfBoundsException) {
            return false
        }

    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (e: IndexOutOfBoundsException) {
            return false
        } catch (e: IllegalStateException) {
            return false
        } catch (e: IllegalArgumentException) {
            return false
        } catch (e: NullPointerException) {
            return false
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mImageSwitcherAdapter.release()
    }

    fun notifyDataSetChanged() {
        mImageSwitcherAdapter.notifyDataSetChanged()
    }

    fun setCurrentItemSmoothly(item: Int) {
        setCurrentItem(item, true)
    }

    fun setCurrentItemImmediately(item: Int) {
        setCurrentItem(item, false)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, smoothScroll)
        mPreviousPhotoPosition = item
        mCurrentPhotoPosition = item
    }

    private fun setSelectedPosition(i: Int) {
        mPreviousPhotoPosition = mCurrentPhotoPosition
        mCurrentPhotoPosition = i
    }

    fun getSelectedPosition(): Int {
        return mCurrentPhotoPosition
    }

    fun getPreviousSelectedPosition(): Int {
        return mPreviousPhotoPosition
    }

    fun setPreloadEnable(isEnable: Boolean) {
        mImageSwitcherAdapter.setPreloadEnable(isEnable)
    }

    fun setUploadListener(listener: IUploadAlbumPhotos) {
        mImageSwitcherAdapter.setUploadListener(listener)
    }

    fun addPhotos(photos: Photos?) {
        mImageSwitcherAdapter.addPhotos(photos)
    }

    fun release() {
        mImageSwitcherAdapter.release()
    }
}