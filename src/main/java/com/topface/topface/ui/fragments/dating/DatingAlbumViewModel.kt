package com.topface.topface.ui.fragments.dating

import android.app.Activity
import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.ViewPager
import com.topface.topface.App
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingAlbumLayoutBinding
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity
import com.topface.topface.ui.views.image_switcher.ImageClick
import com.topface.topface.ui.views.image_switcher.PreloadPhoto
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.addData
import com.topface.topface.utils.extensions.isNotEmpty
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import com.topface.topface.viewModels.BaseViewModel
import rx.Observer
import rx.Subscription
import java.util.*
import javax.inject.Inject


/**
 * Моделька для альбома в дейтинге
 * Created by tiberal on 11.10.16.
 */
class DatingAlbumViewModel(binding: DatingAlbumLayoutBinding, private val mApi: FeedApi,
                           private val mController: AlbumLoadController,
                           private val mUserSearchList: CachableSearchList<SearchUser>,
                           private val mNavigator: IFeedNavigator,
                           private val mAlbumActionsListener: IDatingAlbumView) :
        BaseViewModel<DatingAlbumLayoutBinding>(binding), ViewPager.OnPageChangeListener {

    @Inject lateinit var eventBus: EventBus

    val photosCounter = ObservableField<String>()
    val nameAgeOnline = ObservableField<String>()
    val albumData = ObservableField<Photos>()
    val isOnline = ObservableBoolean()
    val isPhotosCounterVisible = ObservableBoolean(false)
    val isNeedAnimateLoader = ObservableBoolean(false)
    val currentItem = ObservableInt(0)

    var currentUser: SearchUser? = null
        set(value) {
            field = value
            value?.let {
                mAlbumActionsListener.onUserShow(it)
            }
            currentItem.set(0)
            updatePhotosCounter(0)
            nameAgeOnline.set(value?.nameAndAge ?: Utils.EMPTY)
            isOnline.set(value?.online ?: false)
        }
    private var mLoadedCount = 0
    private var mCanSendAlbumReq = true
    private var mNeedMore = false

    private var mAlbumSubscription: Subscription? = null
    private var mOnImageClickSubscription: Subscription? = null
    private var mLoadLinksSubscription: Subscription? = null

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
        App.get().inject(this)
        subscribeIfNeeded()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK && data != null) {
            currentItem.set(data.getIntExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, 0))
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

    private fun sendAlbumRequest(position: Int) {
        mUserSearchList.currentUser?.let {
            mAlbumSubscription = mApi.callAlbumRequest(it, position).subscribe(object : Observer<AlbumPhotos> {
                override fun onCompleted() = mAlbumSubscription.safeUnsubscribe()
                override fun onNext(newPhotos: AlbumPhotos?) {
                    if (it.id == mUserSearchList.currentUser.id && newPhotos != null) {
                        albumData.addData(newPhotos)
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
        putInt(CURRENT_ITEM, binding.datingAlbum.getSelectedPosition())
        putParcelable(CURRENT_USER, currentUser)
        putInt(LOADED_COUNT, mLoadedCount)
        putBoolean(CAN_SEND_ALBUM_REQUEST, mCanSendAlbumReq)
        putBoolean(NEED_MORE, mNeedMore)
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        photosCounter.set(getString(PHOTOS_COUNTER))
        nameAgeOnline.set(getString(NAME_AGE_ONLINE))
        (getParcelableArrayList<Parcelable>(ALBUM_DATA) as? Photos)?.let {
            albumData.set(it)
        }
        isOnline.set(getBoolean(ONLINE, false))
        isPhotosCounterVisible.set(getBoolean(PHOTOS_COUNTER_VISIBLE, false))
        isNeedAnimateLoader.set(getBoolean(NEED_ANIMATE_LOADER, false))
        currentItem.set(getInt(CURRENT_ITEM, 0))
        currentUser = getParcelable(CURRENT_USER)
        mLoadedCount = getInt(LOADED_COUNT, 0)
        mCanSendAlbumReq = getBoolean(CAN_SEND_ALBUM_REQUEST, false)
        mNeedMore = getBoolean(NEED_MORE, false)
    }

    override fun onPause() {
        super.onPause()
        mOnImageClickSubscription.safeUnsubscribe()
    }

    override fun onResume() {
        super.onResume()
        subscribeIfNeeded()
    }

    private fun subscribeIfNeeded() {
        if (mOnImageClickSubscription?.isUnsubscribed ?: true) {
            mOnImageClickSubscription = eventBus.getObservable(ImageClick::class.java).subscribe(shortSubscription {
                with(currentUser) {
                    this?.photos?.let {
                        if (it.isNotEmpty()) {
                            mNavigator.showAlbum(0, id, photosCount, it)
                        }
                    }
                }
            })
        }
        if (mLoadLinksSubscription?.isUnsubscribed ?: true) {
            mLoadLinksSubscription = eventBus.getObservable(PreloadPhoto::class.java)
                    .distinctUntilChanged { t1, t2 -> t1.position == t2.position }
                    .subscribe(shortSubscription {
                        if (mCanSendAlbumReq) {
                            mCanSendAlbumReq = false
                            sendAlbumRequest(it.position)
                        }
                    })
        }
    }

    override fun release() {
        super.release()
        mAlbumSubscription.safeUnsubscribe()
        mOnImageClickSubscription.safeUnsubscribe()
        mLoadLinksSubscription.safeUnsubscribe()
    }

    override fun onPageSelected(position: Int) {
        updatePhotosCounter(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
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
}