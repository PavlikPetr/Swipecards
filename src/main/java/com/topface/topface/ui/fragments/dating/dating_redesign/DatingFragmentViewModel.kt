package com.topface.topface.ui.fragments.dating.dating_redesign


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.RetryRequestReceiver
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.LocaleConfig
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import com.topface.topface.viewModels.LeftMenuHeaderViewModel.AGE_TEMPLATE
import rx.Observer
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*
import javax.inject.Inject

/**
 * ВьюМодель ререредизайна экрана знакомств(19.01.17)
 */
class DatingFragmentViewModel(private val mContext: Context, val mNavigator: IFeedNavigator, private val mApi: FeedApi) : ILifeCycle, ViewPager.OnPageChangeListener {

    val name = ObservableField<String>()
    val feedAge = ObservableField<String>()
    val feedCity = ObservableField<String>()
    val iconOnlineRes = ObservableField<Int>(0)
    val statusText = ObservableField<String>()
    val statusVisibility = ObservableField<Int>(View.VISIBLE)
    val photoCounter = ObservableField<String>()
    //    лоадер крутится - INVISIBLE мутится
    val isVisible = ObservableInt(View.VISIBLE)
    val isDatingButtonsLocked = ObservableBoolean(false)
    val currentItem = ObservableInt(0)
    val albumData = ObservableField<Photos>()
    val isNeedAnimateLoader = ObservableBoolean(false)

    @Inject lateinit internal var mAppState: TopfaceAppState

    private var mLoadedCount = 0
    private var mAlbumSubscription: Subscription? = null
    private var mNeedMore = false
    private var mCanSendAlbumReq = true
    private var mBalanceDataSubscription: Subscription? = null
    private var mBalance: BalanceData? = null
    private val mUpdateActionsReceiver: BroadcastReceiver

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

    private val mUserSearchList: CachableSearchList<SearchUser> = CachableSearchList<SearchUser>(SearchUser::class.java)

    var currentUser: SearchUser? = null
        set(value) {
            field = value?.apply {
                name.set(value.firstName)
                feedAge.set(String.format(App.getCurrentLocale(), AGE_TEMPLATE, value.age))
                feedCity.set(value.city.name)
                iconOnlineRes.set(if (value.online) R.drawable.ico_online else 0)
                statusText.set(value.getStatus())
                statusVisibility.set(if (value.getStatus().isEmpty()) View.GONE else View.VISIBLE)
                updatePhotosCounter(0)
            }
            currentItem.set(0)
        }

    init {
        App.get().inject(this)
        mBalanceDataSubscription = mAppState.getObservable(BalanceData::class.java).subscribe(shortSubscription {
            mBalance = it
        })

        mUpdateActionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val type = intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE) as BlackListAndBookmarkHandler.ActionTypes?
                if (type != null) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (type) {
                        BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST -> {
                            skip()
                        }
                        BlackListAndBookmarkHandler.ActionTypes.SYMPATHY -> {
                            //todo
//                            binding.sendLike.isEnabled = false
//                            binding.sendAdmiration.isEnabled = false
                            currentUser?.let {
                                it.rated = true
                            }
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(mUpdateActionsReceiver, IntentFilter(RetryRequestReceiver.RETRY_INTENT))
    }

    fun updatePhotosCounter(position: Int) = photoCounter.set("${position + 1}/${currentUser?.photosCount}")

    fun showChat() = if (App.get().profile.premium) {
        mNavigator.showChat(currentUser, null)
    } else {
        mNavigator.showPurchaseVip("dating_fragment")
    }

    fun sendAdmiration() {
        //todo отправка восхищения
    }

    fun skip() {
        //todo Скип фида и подгрузка следующего
    }

    fun sendLike() {
        //todo Лайк фида и подгрузка следующего
    }

    fun onPhotoClick() = with(currentUser) {
        if (this != null && photos != null && photos.isNotEmpty()) {
            //todo binding
//            mNavigator.showAlbum(binding.datingAlbum.selectedPosition,
//                    id, photosCount, photos)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK && data != null) {
            currentItem.set(data.getIntExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, 0))
        }
    }

    override fun onSavedInstanceState(state: Bundle) = with(state) {
        putParcelableArrayList(ALBUM_DATA, albumData.get() as? ArrayList<Photo>)
        putBoolean(NEED_MORE, mNeedMore)
        putBoolean(NEED_ANIMATE_LOADER, isNeedAnimateLoader.get())
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        albumData.set(getParcelableArrayList<Parcelable>(ALBUM_DATA) as? Photos)
        mNeedMore = getBoolean(NEED_MORE, false)
        isNeedAnimateLoader.set(getBoolean(NEED_ANIMATE_LOADER, false))
    }

    private fun setUser(user: SearchUser?) = user?.let {
        currentUser = user.apply {
            mLoadedCount = photos.realPhotosCount
            albumData.set(photos)
            mNeedMore = photosCount > mLoadedCount
            val rest = photosCount - photos.count()
            for (i in 0..rest - 1) {
                photos.add(Photo.createFakePhoto())
            }
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
                        //todo binding
//                        binding.datingAlbum?.let {
//                            if (it.selectedPosition > mLoadedCount + mController.itemsOffsetByConnectionType) {
//                                sendAlbumRequest(data)
//                            }
//                            if (it.adapter != null) {
//                                it.adapter.notifyDataSetChanged()
//                            }
//                        }
                    }
                    mCanSendAlbumReq = true
                }

                override fun onError(e: Throwable?) {
                    mCanSendAlbumReq = true
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        if (LocaleConfig.localeChangeInitiated) {
            mUserSearchList.removeAllUsers()
            mUserSearchList.saveCache()
        } else {
            mUserSearchList.saveCache()
        }
    }

    fun release() {
        mAlbumSubscription.safeUnsubscribe()
        mBalanceDataSubscription.safeUnsubscribe()
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdateActionsReceiver)
    }

    override fun onPageSelected(position: Int) {
        updatePhotosCounter(position)
        //todo binding
//        binding.datingAlbum?.let {
//            if (position + mController.itemsOffsetByConnectionType == mLoadedCount - 1) {
//                (it.adapter as ImageSwitcher.ImageSwitcherAdapter).data?.let {
//                    if (mNeedMore && mCanSendAlbumReq && !it.isEmpty()) {
//                        mCanSendAlbumReq = false
//                        sendAlbumRequest(it)
//                    }
//                }
//            }
//        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

}