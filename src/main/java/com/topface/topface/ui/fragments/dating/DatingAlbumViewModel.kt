package com.topface.topface.ui.fragments.dating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity
import com.topface.topface.ui.views.image_switcher.ImageClick
import com.topface.topface.ui.views.image_switcher.PhotoAlbumAdapter
import com.topface.topface.ui.views.image_switcher.PreloadPhoto
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.*
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observer
import rx.Subscription
import java.util.*
import kotlin.properties.Delegates


/**
 * Моделька для альбома в дейтинге
 * Created by tiberal on 11.10.16.
 */
class DatingAlbumViewModel(private val mContext: Context, private val mApi: FeedApi,
                           private val mUserSearchList: CachableSearchList<SearchUser>,
                           private val mNavigator: IFeedNavigator,
                           private val mAlbumActionsListener: IDatingAlbumView,
                           private val mLoadBackground: (link: String) -> Unit) :
        ViewPager.OnPageChangeListener, ILifeCycle {

    val photosCounter = ObservableField<String>()
    val nameAgeOnline = ObservableField<String>()
    val albumData = ObservableField<Photos>()
    val isNeedPreloadOnStart = ObservableBoolean(false)
    val isOnline = ObservableBoolean()
    val isPhotosCounterVisible = ObservableBoolean(false)
    val isNeedAnimateLoader = ObservableBoolean(false)
    val currentItem = ObservableInt(0)
    val albumDefaultBackground = ObservableField(R.drawable.bg_blur.getDrawable())

    private var mPreloadTarget: Target<GlideDrawable>? = null
    private var mOnImageClickSubscription: Subscription? = null
    private var mLoadLinksSubscription: Subscription? = null
    private var mLoadBackgroundSubscription: Subscription? = null

    private var mCurrentPosition by Delegates.observable(0) { prop, old, new ->
        updatePhotosCounter(new)
        loadBluredBackground(new)
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    var currentUser: SearchUser? = null
        set(value) {
            field = value
            value?.let {
                mAlbumActionsListener.onUserShow(it)
            }
            updatePhotosCounter(0)
            nameAgeOnline.set(value?.nameAndAge ?: Utils.EMPTY)
            isOnline.set(value?.online ?: false)
            albumDefaultBackground.set(R.drawable.bg_blur.getDrawable())
            setCurrentUser(0)
            preloadPhoto()
        }
    private var mLoadedCount = 0
    private var mCanSendAlbumReq = true
    private var mNeedMore = false

    private var mAlbumSubscription: Subscription? = null

    private companion object {
        const val PHOTOS_COUNTER = "photos_counter"
        const val NAME_AGE_ONLINE = "name_age_online"
        const val ALBUM_DATA = "album_data"
        const val ONLINE = "online"
        const val PHOTOS_COUNTER_VISIBLE = "photos_counter_visible"
        const val NEED_ANIMATE_LOADER = "need_animate_loader"
        const val CURRENT_ITEM = "current_item"
        const val CURRENT_USER = "current_user_dating_album"
        const val LOADED_COUNT = "loaded_count"
        const val CAN_SEND_ALBUM_REQUEST = "can_send_album_request"
        const val NEED_MORE = "need_more"
    }

    init {
        subscribeIfNeeded()
    }

    private fun preloadPhoto() {
        mUserSearchList.getOrNull(mUserSearchList.searchPosition + 1)?.photo?.defaultLink?.let {
            mPreloadTarget.clear()
            mPreloadTarget = Glide.with(mContext)
                    .fromString()
                    .fitCenter()
                    .loadLinkToSameCache(it)
                    .listener(object : RequestListener<String, GlideDrawable> {
                        override fun onResourceReady(resource: GlideDrawable?, model: String?,
                                                     target: Target<GlideDrawable>?, isFromMemoryCache: Boolean,
                                                     isFirstResource: Boolean): Boolean {
                            isNeedPreloadOnStart.apply {
                                set(true)
                                notifyChange()
                            }
                            Debug.log("${PhotoAlbumAdapter.TAG} =======================onResourceReady=DatingPreload==========\nlink:$model\nisFirst:$isFirstResource\nisFromCache:$isFromMemoryCache\n===============================================")
                            return false
                        }

                        override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
                                                 isFirstResource: Boolean): Boolean {
                            isNeedPreloadOnStart.apply {
                                set(true)
                                notifyChange()
                            }
                            Debug.log("${PhotoAlbumAdapter.TAG} =======================onException=DatingPreload==========\n$e\nlink:$model\nisFirst:$isFirstResource\n===============================================")
                            return false
                        }
                    })
                    .preload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK && data != null) {
            setCurrentUser(data.getIntExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, 0))
        }
    }

    fun updatePhotosCounter(position: Int) {
        val user = currentUser
        if (user != null && user.photos != null && user.photos.isNotEmpty()) {
            if (!isPhotosCounterVisible.get()) {
                isPhotosCounterVisible.set(true)
            }
            photosCounter.set("${position + 1}/${user.photosCount}")
        } else {
            isPhotosCounterVisible.set(false)
        }
    }

    override fun onResume() {
        super.onResume()
        subscribeIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        arrayOf(mOnImageClickSubscription, mLoadLinksSubscription).safeUnsubscribe()
    }

    private fun subscribeIfNeeded() {
        if (mOnImageClickSubscription?.isUnsubscribed ?: true) {
            mOnImageClickSubscription = mEventBus.getObservable(ImageClick::class.java).subscribe(shortSubscription {
                with(currentUser) {
                    this?.photos?.let {
                        if (it.isNotEmpty()) {
                            mNavigator.showAlbum(mCurrentPosition, id, photosCount, it)
                        }
                    }
                }
            })
        }
        if (mLoadLinksSubscription?.isUnsubscribed ?: true) {
            mLoadLinksSubscription = mEventBus.getObservable(PreloadPhoto::class.java)
                    .distinctUntilChanged { t1, t2 -> t1.position == t2.position }
                    .subscribe(shortSubscription {
                        if (mCanSendAlbumReq) {
                            mCanSendAlbumReq = false
                            sendAlbumRequest(it.position)
                        }

                    })
        }
    }

    private fun sendAlbumRequest(position: Int) {
        mUserSearchList.currentUser?.let {
            mAlbumSubscription = mApi.callAlbumRequest(it, position).subscribe(object : Observer<AlbumPhotos> {
                override fun onCompleted() = mAlbumSubscription.safeUnsubscribe()
                override fun onNext(newPhotos: AlbumPhotos?) {
                    if (it.id == mUserSearchList.currentUser.id && newPhotos != null) {
                        albumData.addData(newPhotos)
                        loadBluredBackground(mCurrentPosition)
                        mNeedMore = newPhotos.more
                        mLoadedCount += newPhotos.size
                    }
                    mCanSendAlbumReq = true
                }

                override fun onError(e: Throwable?) {
                    mCanSendAlbumReq = true
                }
            })
        }
    }

    override fun onSavedInstanceState(state: Bundle) = with(state) {
        putString(PHOTOS_COUNTER, photosCounter.get())
        putString(NAME_AGE_ONLINE, nameAgeOnline.get())
        putParcelableArrayList(ALBUM_DATA, albumData.get() as? ArrayList<Photo>)
        putBoolean(ONLINE, isOnline.get())
        putBoolean(PHOTOS_COUNTER_VISIBLE, isPhotosCounterVisible.get())
        putBoolean(NEED_ANIMATE_LOADER, isNeedAnimateLoader.get())
        putInt(CURRENT_ITEM, mCurrentPosition)
        putParcelable(CURRENT_USER, currentUser)
        putInt(LOADED_COUNT, mLoadedCount)
        putBoolean(CAN_SEND_ALBUM_REQUEST, mCanSendAlbumReq)
        putBoolean(NEED_MORE, mNeedMore)
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        photosCounter.set(getString(PHOTOS_COUNTER))
        nameAgeOnline.set(getString(NAME_AGE_ONLINE))
        albumData.set(getParcelableArrayList<Parcelable>(ALBUM_DATA) as? Photos)
        isOnline.set(getBoolean(ONLINE, false))
        isPhotosCounterVisible.set(getBoolean(PHOTOS_COUNTER_VISIBLE, false))
        isNeedAnimateLoader.set(getBoolean(NEED_ANIMATE_LOADER, false))
        currentUser = getParcelable(CURRENT_USER)
        mLoadedCount = getInt(LOADED_COUNT, 0)
        mCanSendAlbumReq = getBoolean(CAN_SEND_ALBUM_REQUEST, false)
        mNeedMore = getBoolean(NEED_MORE, false)
        setCurrentUser(getInt(CURRENT_ITEM))
    }

    fun release() {
        arrayOf(mAlbumSubscription, mOnImageClickSubscription,
                mLoadLinksSubscription, mLoadBackgroundSubscription).safeUnsubscribe()
        mPreloadTarget.clear()
    }

    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
    }


    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // если true, значит viewPager ожил после переворота экрана и можно засетить текущую позицию
        if (position == 0 && positionOffset == 0f && positionOffsetPixels == 0) {
            currentItem.notifyChange()
        }
    }

    fun setUser(user: SearchUser?) = user?.let {
        currentUser = user.apply {
            mLoadedCount = photos?.realPhotosCount ?: 0
            photos?.let {
                albumData.set(it)
            }
            mNeedMore = photosCount > mLoadedCount
            val rest = photosCount - (photos?.count() ?: 0)
            for (i in 0..rest - 1) {
                photos?.add(Photo.createFakePhoto())
            }
        }
    }

    private fun setCurrentUser(position: Int) {
        currentItem.set(position)
        currentItem.notifyChange()
        mCurrentPosition = position
    }

    fun onShowProgress() {
        albumDefaultBackground.set(R.drawable.bg_blur.getDrawable())
    }

    private fun loadBluredBackground(position: Int) =
            albumData.get()?.getOrNull(position)?.getSuitableLink(Photo.SIZE_128)?.let {
                mLoadBackground(it)
            }
}