package com.topface.topface.ui.views.image_switcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos
import com.topface.topface.ui.views.image_switcher.ImageSwitcher.Companion.TAG
import com.topface.topface.ui.views.image_switcher.ImageSwitcher.Companion.VIEW_TAG
import com.topface.topface.utils.AnimationUtils
import com.topface.topface.utils.extensions.clear
import com.topface.topface.utils.extensions.getSuitableLink


/**
 * Адаптер для альбома на базе Glide
 * Created by ppavlik on 13.02.17.
 */
class ImageSwitcherAdapter(private val mContext: Context, private val mViewPager: ViewPager,
                           private val currentItem: () -> Int,
                           private val onClickListener: () -> Unit) : PagerAdapter() {
    private var mPreloadLinks = mutableMapOf<Int, Pair<Photo, @Status.ImageLoaderStatus Long>>()
    private var mPreloadTarget: Target<Bitmap>? = null
    private var mLoadTarget: SimpleTarget<Bitmap>? = null
    private var mIsPreloadEnable = true
    private var mIsNeedAnimateLoader: Boolean = false
    private var mUploadListener: IUploadAlbumPhotos? = null

    var data: Photos?
        get() = getPhotos()
        set(photos) {
            Debug.error("$TAG new data")
            resetAll()
            fillData(photos)
            notifyDataSetChanged()
        }

    private fun getPhotos() = Photos().apply {
        for (i in 0..mPreloadLinks.size - 1) {
            add(mPreloadLinks[i]?.first)
        }
    }

    private fun getPhoto(position: Int) = mPreloadLinks[position]?.first

    @Suppress("unused")
    @Status.ImageLoaderStatus
    private fun getState(photo: Photo): Long {
        mPreloadLinks.forEach { if (it.value.first == photo) return it.value.second }
        return Status.UNDEFINED
    }

    @Status.ImageLoaderStatus
    private fun getState(position: Int) = mPreloadLinks[position]?.second ?: Status.UNDEFINED

    @Suppress("unused")
    private fun setState(photo: Photo, @Status.ImageLoaderStatus status: Long) =
            getPosition(photo)?.let {
                mPreloadLinks.put(it, Pair(photo, status))
            }

    private fun setState(position: Int, @Status.ImageLoaderStatus status: Long) =
            mPreloadLinks[position]?.first?.let {
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
            (state == Status.UNDEFINED || state == Status.NOT_LOADED) && state != Status.ALBUM_REQUEST_SENDED
        }) {
            if (isNotEmpty()) {
                return getFirstOverlapKey(this, listOf(
                        listOf(getCurrentItem(), getCurrentItem() + 1, getCurrentItem() + 2),
                        listOf(getCurrentItem() - 1, getCurrentItem() - 2),
                        (getCurrentItem()..mPreloadLinks.size - 1).toList(),
                        (0..getCurrentItem() - 1).toList()
                ))
            }
        }
        return null
    }

    private fun getCurrentItem() = currentItem.invoke()


    private fun getFirstOverlapKey(links: Map<Int, Pair<Photo,
            @Status.ImageLoaderStatus Long>>, listOfRange: List<List<Int>>): Int? {
        listOfRange.forEach {
            getKeyFromRange(links, it)?.let { return it }
        }
        return null
    }

    fun setPreloadEnable(isEnable: Boolean) {
        mIsPreloadEnable = isEnable
        preloadIfNeed()
    }

    fun needAnimateLoader(animate: Boolean) {
        mIsNeedAnimateLoader = animate
    }

    fun setUploadListener(listener: IUploadAlbumPhotos) {
        mUploadListener = listener
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
        mPreloadTarget.clear()
        mLoadTarget.clear()
    }

    fun addPhotos(photos: Photos?) {
        fillData(photos)
    }

    private fun fillData(photoLinks: Photos?) {
        var position: Int
        photoLinks?.forEachIndexed { i, photo ->
            position = if (photo.isFake || photo.isEmpty) i else photo.getPosition()
            // меняем/добавляем только те линки, которых еще нет в списке/если пусто/если фэйк
            if (mPreloadLinks[position]?.first?.let { it.isFake || it.isEmpty } ?: true) {
                mPreloadLinks.put(position, Pair(photo, Status.NOT_LOADED))
            }
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
        imageView.setOnClickListener { onClickListener.invoke() }
        preloadIfPosible(realPosition)
        pager.addView(view)
        // если создали текущую страницу, то надо вызвать ее отрисовку
        if (position == getCurrentItem()) {
            notifyDataSetChanged()
        }
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
     */
    fun setPhotoToPosition(position: Int) {
        val realPosition = getRealPosition(position)
        val baseLayout = mViewPager.findViewWithTag(VIEW_TAG + Integer.toString(position))
        //Этот метод может вызываться до того, как создана страница для этой фотографии
        baseLayout?.let {
            setPhotoToView(realPosition, it, it.findViewById(R.id.ivPreView) as ImageView)
        } ?: preloadIfPosible(realPosition)
    }

    fun getImageView(position: Int) =
            mViewPager.findViewWithTag(VIEW_TAG + Integer.toString(position))?.findViewById(R.id.ivPreView) as? ImageView

    @Suppress("unused")
    private fun getPhotoLink(position: Int) = getPhoto(getRealPosition(position))?.let {
        with(mViewPager.layoutParams) {
            if (Math.max(height, width) > 0) {
                it.getSuitableLink(height, width)
            } else {
                it.defaultLink
            }
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
            val state = getState(position)
            Debug.error("$TAG set photo state:$state position:$position")
            when (state) {
                Status.START_PRELOAD -> setState(position, Status.SET_IMAGE_ON_SUCCESS_PRELOAD)
                else -> {
                    mLoadTarget.clear()
                    loadImage(position, imageView)
                }
            }
        }
    }

    private fun preloadIfNeed() {
        Debug.error("$TAG preload if need")
        preloadIfEnabled {
            getNextPreloadPosition()?.let {
                if (getState(it) != Status.ALBUM_REQUEST_SENDED) {
                    Debug.error("$TAG preload photo $it")
                    preloadImage(it)
                }
            }
        }
    }

    private fun preloadIfEnabled(block: () -> Unit) {
        if (mIsPreloadEnable && !isActivePreload()) block.invoke()
    }

    private fun preloadIfPosible(position: Int) {
        preloadIfEnabled {
            if (getState(position) != Status.ALBUM_REQUEST_SENDED) {
                preloadImage(position)
            }
        }
    }


    private fun preloadImage(position: Int) = getPhoto(position)?.let {
        getImageView(position)?.getSuitableLink(it) ?: it.defaultLink?.let {
            Debug.error("$TAG start preload $it")
            setState(position, Status.START_PRELOAD)
            mPreloadTarget = Glide.with(mContext.applicationContext)
                    .load(it)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(object : RequestListener<String, Bitmap> {
                        override fun onException(e: Exception?, model: String?,
                                                 target: Target<Bitmap>?,
                                                 isFirstResource: Boolean): Boolean {
                            Debug.error("$TAG preload failed exception:$e isFirstResource:$isFirstResource")
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
        } ?: needLoadLinks(position)
    }

    private fun loadImage(position: Int, imageView: ImageView) = getPhoto(position)?.let {
        imageView.getSuitableLink(it)?.let {
            Debug.error("$TAG start load $it")
            setState(position, Status.START_LOAD)
            mLoadTarget = Glide.with(mContext.applicationContext)
                    .load(it)
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

        } ?: needLoadLinks(position)
        imageView.setTag(R.string.photo_is_set_tag, !it.isFake)
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        setPhotoToPosition(getCurrentItem())
    }

    private fun needLoadLinks(position: Int) {
        Debug.error("NewImageLoader1 needLoadLinks position:$position")
        setState(position, Status.ALBUM_REQUEST_SENDED)
        mUploadListener?.sendRequest(position)
    }

    fun release() {
        mLoadTarget.clear()
        mPreloadTarget.clear()
    }
}