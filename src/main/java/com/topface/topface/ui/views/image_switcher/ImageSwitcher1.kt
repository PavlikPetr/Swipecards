package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.utils.AnimationUtils
import com.topface.topface.utils.extensions.clear
import com.topface.topface.utils.extensions.getSuitableLink
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Observable
import rx.Subscription

/**
 * Created by petrp on 08.02.2017.
 */

class ImageSwitcher1(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    constructor(context: Context) : this(context, null)

    companion object {
        const val TAG = "NewImageLoader"
    }

    private var mOnClickListener: OnClickListener? = null

    private var mUpdatedHandler: Handler? = null
    private val VIEW_TAG = "view_container"
    private var mCurrentPhotoPosition = 0
    private var mPreviousPhotoPosition = 0
    private var mPrev = 0
    private var mNext = 0
    private var mIsNeedAnimateLoader: Boolean = false

    private val mGestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (mOnClickListener != null) mOnClickListener?.onClick(this@ImageSwitcher1)
                return false
            }
        })
    }

    private val mImageSwitcherAdapter: ImageSwitcherAdapter by lazy {
        ImageSwitcherAdapter()
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

    fun setUpdateHandler(handler: Handler) {
        mUpdatedHandler = handler
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

            internal var oldPosition = currentItem

            override fun onPageScrolled(i: Int, v: Float, i1: Int) {
                //При WiFi подключении это не нужно, т.к. фотографию мы уже прелоадим заранее, но нужно при 3G
                //Если показано больше 10% следующей фотографии, то начинаем ее грузить
                if (v > 0.1 && v < 0.6) {
                    if (i > oldPosition) {
                        val next: Int
                        next = i + 1
                        //Проверяем, начали ли мы грузить следующую фотографию
                        if (mNext != next) {
                            mNext = next
                            oldPosition = mNext
                            mImageSwitcherAdapter.setPhotoToPosition(mNext, false)
                        }
                    } else if (i < oldPosition) {
                        //Проверяем, не начали ли мы грузить предыдущую фотографию
                        if (mPrev != i) {
                            mPrev = i
                            oldPosition = mPrev
                            mImageSwitcherAdapter.setPhotoToPosition(mPrev, false)
                        }
                    }
                }
                finalListener.onPageScrolled(i, v, i1)
            }

            override fun onPageSelected(i: Int) {
                setSelectedPosition(i)
                mImageSwitcherAdapter.setPhotoToPosition(i, false)
                finalListener.onPageSelected(i)
            }

            override fun onPageScrollStateChanged(i: Int) {
                finalListener.onPageScrollStateChanged(i)
            }
        })
    }

    fun needAnimateLoader(animate: Boolean) {
        mIsNeedAnimateLoader = animate
    }

    inner class ImageSwitcherAdapter : PagerAdapter() {
        private var mPreloadLinks = mutableMapOf<Int, Pair<Photo, @Status.ImageLoaderStatus Long>>()
        private var mPreloadTarget: Target<Bitmap>? = null
        private var mLoadTarget: SimpleTarget<Bitmap>? = null

        /**
         * Создает слушателя загрузки фотки, через замыкание передавая позицию слушаемой фотографии

         * @param position изображение, загрузку которого мы слушаем
         * *
         * @return listener
         */
        private fun getListener(position: Int): ImageLoadingListener {
            return object : SimpleImageLoadingListener() {
                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    super.onLoadingComplete(imageUri, view, loadedImage)

                    val currentItem = currentItem
                    if (currentItem + 1 == position || currentItem - 1 == position) {
                        setPhotoToPosition(position, true)
                    }
                }
            }
        }

        var data: Photos?
            get() = getPhotos()
            set(photos) {
                Debug.error("$TAG new data")
                resetAll()
                fillData(photos)
                preloadIfNeed()
                notifyDataSetChanged()
            }

        private fun getPhotos() = Photos().apply {
            for (i in 0..mPreloadLinks.size - 1) {
                add(mPreloadLinks.get(i)?.first)
            }
        }

        private fun getPhoto(position: Int) = mPreloadLinks.get(position)?.first

        @Status.ImageLoaderStatus
        private fun getState(photo: Photo): Long {
            mPreloadLinks.forEach { if (it.value.first == photo) return it.value.second }
            return Status.UNDEFINED
        }

        @Status.ImageLoaderStatus
        private fun getState(position: Int) = mPreloadLinks.get(position)?.second ?: Status.UNDEFINED

        private fun setState(photo: Photo, @Status.ImageLoaderStatus status: Long) =
                getPosition(photo)?.let {
                    mPreloadLinks.put(it, Pair(photo, status))
                }

        private fun setState(position: Int, @Status.ImageLoaderStatus status: Long) =
                mPreloadLinks.get(position)?.first?.let {
                    mPreloadLinks.put(position, Pair(it, status))
                }

        private fun getPosition(photo: Photo): Int? {
            mPreloadLinks.forEach { if (it.value.first == photo) return it.key }
            return null
        }

        private fun isActivePreload(): Boolean {
            mPreloadLinks.forEach { if (it.value.second == Status.START_PRELOAD) return true }
            return false
        }

        private fun getNextPreloadPosition(): Int? {
            with(mPreloadLinks.filter {
                val state = it.value.second
                state == Status.UNDEFINED || state == Status.NOT_LOADED
            }) {
                if (isNotEmpty()) {
                    return getFirstOverlapKey(this, listOf(
                            listOf(mNext, mNext + 1, mNext + 2),
                            listOf(mNext - 1, mNext - 2),
                            (mNext..mPreloadLinks.size - 1).toList(),
                            (0..mNext - 1).toList()
                    ))
                }
            }
            return null
        }

        private fun getFirstOverlapKey(links: Map<Int, Pair<Photo,
                @Status.ImageLoaderStatus Long>>, listOfRange: List<List<Int>>): Int? {
            listOfRange.forEach {
                getKeyFromRange(links, it)?.let { return it }
            }
            return null
        }

        private fun getKeyFromRange(links: Map<Int, Pair<Photo,
                @Status.ImageLoaderStatus Long>>, range: List<Int>): Int? {
            range.forEach {
                if (links.containsKey(it)) return it
            }
            return null
        }

        private fun resetAll() {
            mPreloadLinks.clear()
            mPrev = -1
            mNext = 0
            mPreloadTarget.clear()
            mLoadTarget.clear()
        }

        private fun fillData(photoLinks: Photos?) {
            photoLinks?.forEachIndexed { i, photo ->
                mPreloadLinks.put(i, Pair(photo, Status.NOT_LOADED))
            }
        }

        fun getRealPosition(position: Int): Int {
            return position
        }

        override fun getCount() = mPreloadLinks.size

        override fun instantiateItem(pager: ViewGroup, position: Int): Any {
            val realPosition = getRealPosition(position)
            val inflater = pager.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.item_album, null)
            view.tag = VIEW_TAG + Integer.toString(position)
            val imageView = view.findViewById(R.id.ivPreView) as ImageView
            imageView.setOnClickListener { mOnClickListener?.onClick(this@ImageSwitcher1) }
            //Первую фотографию грузим сразу, или если фотографию уже загружена, то сразу показываем ее
            //Если это первая фото в списке или фотография уже загружена, то устанавливаем фото сразу
            setPhotoToView(position, view, imageView)
//            Glide.with(context.applicationContext).load(getPhotoLink(position)).preload()

//            //Если фото еще не загружено, то пытаемся его загрузить через прелоадер
//            if (!isLoadedPhoto && Glide.with(context.applicationContext).load(getPhotoLink(position)).preload() mPreloadManager ?. preloadPhoto (mPhotoLinks, realPosition, getListener(position)) ?: false) {
//                //Добавляем его в список загруженых
//                mLoadedPhotos?.put(realPosition, true)
//            }

            pager.addView(view)
            return view
        }

        override fun destroyItem(view: ViewGroup, position: Int, `object`: Any) {
            view.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        /**
         * Устанавливает фотографию в ImageView

         * @param position страница на которой находится ImageView
         * *
         * @param ifLoaded если true, то установить только если фотография уже загружена
         */
        fun setPhotoToPosition(position: Int, ifLoaded: Boolean) {
            val realPosition = getRealPosition(position)
                val baseLayout = this@ImageSwitcher1.findViewWithTag(VIEW_TAG + Integer.toString(position))
                //Этот метод может вызываться до того, как создана страница для этой фотографии
                baseLayout?.let {
                    setPhotoToView(position, it, it.findViewById(R.id.ivPreView) as ImageView)
                }
        }

        fun getImageView(position: Int) =
                findViewWithTag(VIEW_TAG + Integer.toString(position))?.findViewById(R.id.ivPreView) as? ImageView


        private fun getPhotoLink(position: Int) = getPhoto(getRealPosition(position))?.let {
            if (Math.max(layoutParams.height, layoutParams.width) > 0) {
                it.getSuitableLink(layoutParams.height, layoutParams.width)
            } else {
                it.defaultLink
            }
        }

        private fun setPhotoToView(position: Int, baseLayout: View, imageView: ImageView) {
            val tag = imageView.getTag(R.string.photo_is_set_tag)
            //Проверяем, не установленно ли уже изображение в ImageView
            if (tag == null || !(tag as Boolean)) {
                val progressBar = baseLayout.findViewById(R.id.pgrsAlbum)
                progressBar.visibility = View.VISIBLE
                if (mIsNeedAnimateLoader) {
                    AnimationUtils.createProgressBarAnimator(progressBar).start()
                }
                val realPosition = getRealPosition(position)
                val state = getState(realPosition)
                Debug.error("$TAG set photo state:$state position:$realPosition")
                when (state) {
                    Status.START_PRELOAD -> setState(realPosition, Status.SET_IMAGE_ON_SUCCESS_PRELOAD)
                    else -> {
                        loadImage(realPosition, imageView)
                        setState(realPosition, Status.START_LOAD)
                    }
                }
            }
        }

        private fun preloadIfNeed() {
            Debug.error("$TAG preload if need")
            if (!isActivePreload()) {
                getNextPreloadPosition()?.let {
                    Debug.error("$TAG preload photo $it")
                    preloadImage(it)
                }
            }
        }

        private fun preloadImage(position: Int) = getPhoto(position)?.let {
            val link = getImageView(position)?.getSuitableLink(it) ?: it.defaultLink
            Debug.error("$TAG start preload $link")
            mPreloadTarget = Glide.with(context.applicationContext)
                    .load(link)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(object : RequestListener<String, Bitmap> {
                        override fun onException(e: Exception?, model: String?,
                                                 target: Target<Bitmap>?,
                                                 isFirstResource: Boolean): Boolean {
                            Debug.error("$TAG preload failed exception:$e isFirstResource^$isFirstResource")
                            setState(position, Status.PRELOAD_FAILED)
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: String?,
                                                     target: Target<Bitmap>?,
                                                     isFromMemoryCache: Boolean,
                                                     isFirstResource: Boolean): Boolean {
                            Debug.error("$TAG preload succesfull model:$model isFromCache:$isFromMemoryCache isFirst:$isFirstResource")
                            if (getState(position) == Status.SET_IMAGE_ON_SUCCESS_PRELOAD) {
                                getImageView(position)?.setImageBitmap(resource)
                            }
                            setState(position, Status.PRELOAD_SUCCESS)
                            preloadIfNeed()
                            return false
                        }
                    })
                    .preload()
        }

        private fun loadImage(position: Int, imageView: ImageView) = getPhoto(position)?.let {
            val link = imageView.getSuitableLink(it)
            Debug.error("$TAG start load $link")
            mLoadTarget = Glide.with(context.applicationContext).load(link)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                            Debug.error("$TAG load successfull")
                            resource?.let {
                                setState(position, Status.LOAD_SUCCESS)
                                imageView.setImageBitmap(resource)
                                preloadIfNeed()
                            }
                        }

                        override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                            Debug.error("$TAG load failed exception:$e")
                            super.onLoadFailed(e, errorDrawable)
                            setState(position, Status.LOAD_FAILED)
                        }

                        override fun onLoadStarted(placeholder: Drawable?) {
                            Debug.error("$TAG start load")
                            super.onLoadStarted(placeholder)
                            setState(position, Status.START_LOAD)
                        }
                    })
            imageView.setTag(R.string.photo_is_set_tag, !it.isFake)
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            setPhotoToPosition(getSelectedPosition(), true)
        }
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
}