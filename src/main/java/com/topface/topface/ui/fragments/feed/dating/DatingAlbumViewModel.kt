package com.topface.topface.ui.fragments.feed.dating

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.ViewPager
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingAlbumLayoutBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.views.ImageSwitcher
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.viewModels.BaseViewModel
import rx.Observer
import rx.Subscription
import java.util.*

/**
 * Моделька для альбома в дейтинге
 * Created by tiberal on 11.10.16.
 */
class DatingAlbumViewModel(binding: DatingAlbumLayoutBinding, private val mApi: FeedApi,
                           private val mController: AlbumLoadController,
                           private val mUserSearchList: CachableSearchList<SearchUser>,
                           private val mAlbumActionsListener: IDatingAlbumView) :
        BaseViewModel<DatingAlbumLayoutBinding>(binding), ViewPager.OnPageChangeListener {

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

    fun updatePhotosCounter(position: Int) {
        val user = currentUser
        if (user != null && user.photos != null && user.photos.isNotEmpty()) {
            if (!isPhotosCounterVisible.get()) {
                isPhotosCounterVisible.set(true)
            }
            photosCounter.set("${position + 1}/${user.photos.count()}")
        } else {
            isPhotosCounterVisible.set(false)
        }
    }

    private fun sendAlbumRequest(data: Photos) {
        if (mLoadedCount - 1 >= data.size || data[mLoadedCount - 1] == null) {
            return
        }
        mUserSearchList.currentUser?.let {
            mAlbumSubscription = mApi.callAlbumRequest(it, data[mLoadedCount - 1].getPosition() + 1).subscribe(object : Observer<AlbumPhotos> {
                override fun onCompleted() = mAlbumSubscription.safeUnsubscribe()
                override fun onNext(newPhotos: AlbumPhotos?) {
                    if (it.id == mUserSearchList.currentUser.id && newPhotos != null) {
                        mNeedMore = newPhotos.more
                        var i = 0
                        for (photo in newPhotos) {
                            if (mLoadedCount + i < data.size) {
                                data[mLoadedCount + i] = photo
                                i++
                            }
                        }
                        mLoadedCount += newPhotos.size
                        binding.datingAlbum?.let {
                            if (it.selectedPosition > mLoadedCount + mController.itemsOffsetByConnectionType) {
                                sendAlbumRequest(data)
                            }
                            if (it.adapter != null) {
                                it.adapter.notifyDataSetChanged()
                            }
                        }
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
        putInt(CURRENT_ITEM, currentItem.get())
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
        currentItem.set(getInt(CURRENT_ITEM, 0))
        currentUser = getParcelable(CURRENT_USER)
        mLoadedCount = getInt(LOADED_COUNT, 0)
        mCanSendAlbumReq = getBoolean(CAN_SEND_ALBUM_REQUEST, false)
        mNeedMore = getBoolean(NEED_MORE, false)
    }

    override fun release() {
        super.release()
        mAlbumSubscription.safeUnsubscribe()
    }

    override fun onPageSelected(position: Int) {
        updatePhotosCounter(position)
        binding.datingAlbum?.let {
            if (position + mController.itemsOffsetByConnectionType == mLoadedCount - 1) {
                (it.adapter as ImageSwitcher.ImageSwitcherAdapter).data?.let {
                    if (mNeedMore && mCanSendAlbumReq && !it.isEmpty()) {
                        mCanSendAlbumReq = false
                        sendAlbumRequest(it)
                    }
                }
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

}