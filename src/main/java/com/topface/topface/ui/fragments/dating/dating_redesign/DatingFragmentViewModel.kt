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
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.RetryRequestReceiver
import com.topface.topface.Ssid
import com.topface.topface.data.*
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.OnUsersListEventsListener
import com.topface.topface.data.search.SearchUser
import com.topface.topface.data.search.UsersList
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.SendLikeRequest
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.statistics.AuthStatistics
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.TrialExperimentsRules.tryShowTrialPopup
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.edit.filter.model.FilterData
import com.topface.topface.ui.edit.filter.view.FilterFragment
import com.topface.topface.ui.fragments.dating.DatingFragmentViewModel
import com.topface.topface.ui.fragments.dating.IEmptySearchVisibility
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.PreloadManager
import com.topface.topface.utils.Utils
import com.topface.topface.utils.cache.SearchCacheManager
import com.topface.topface.utils.extensions.getDrawable
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.social.AuthToken
import com.topface.topface.viewModels.LeftMenuHeaderViewModel.AGE_TEMPLATE
import rx.Observer
import rx.Subscriber
import rx.Subscription
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * ВьюМодель ререредизайна экрана знакомств(19.01.17)
 */
class DatingFragmentViewModel(private val mContext: Context, val mNavigator: IFeedNavigator, private val mApi: FeedApi,
                              private val mEmptySearchVisibility: IEmptySearchVisibility,
                              private val mController: AlbumLoadController,
                              private val mUserSearchList: CachableSearchList<SearchUser>,
                              private val mLoadBackground: (link: String) -> Unit) : ILifeCycle, ViewPager.OnPageChangeListener,
        OnUsersListEventsListener<SearchUser> {

    val name = ObservableField<String>()
    val feedAge = ObservableField<String>()
    val feedCity = ObservableField<String>()
    val iconOnlineRes = ObservableField(0)
    val isDatingProgressBarVisible = ObservableField<Int>(View.VISIBLE)
    val statusText = object : ObservableField<String>() {
        override fun set(value: String?){
            val status = Profile.normilizeStatus(value)
            super.set(status)
            statusVisibility.set(if (status.isNullOrEmpty()) View.GONE else View.VISIBLE)
        }
    }
    val statusVisibility = ObservableField<Int>(View.GONE)
    val photoCounterVisibility = ObservableField<Int>(View.GONE)
    val photoCounter = object : ObservableField<String>() {
        override fun set(value: String?) {
            super.set(value)
            photoCounterVisibility.set(if (value.isNullOrEmpty()) View.GONE else View.VISIBLE)
        }
    }
    val isVisible = ObservableInt(View.VISIBLE)
    val isChatButtonsEnable = ObservableBoolean(true)
    val isLikeButtonsEnable = ObservableBoolean(true)
    val isSkipButtonsEnable = ObservableBoolean(true)
    val isProfileButtonsEnable = ObservableBoolean(true)
    val currentItem = ObservableInt(0)
    val albumData = ObservableField<Photos>()
    val isNeedAnimateLoader = ObservableBoolean(false)
    val albumDefaultBackground = ObservableField(R.drawable.bg_blur.getDrawable())

    @Inject lateinit internal var appState: TopfaceAppState

    private var mLoadedCount = 0
    private var mAlbumSubscription: Subscription? = null
    private var mNeedMore = false
    private var mCanSendAlbumReq = true
    private val mUpdateActionsReceiver: BroadcastReceiver
    private var mSkipSubscription: Subscription? = null
    private var mLikeSubscription: Subscription? = null
    private var mProfileSubscription: Subscription? = null
    private var mUpdateSubscription: Subscription? = null
    private var mLoadBackgroundSubscription: Subscription? = null
    private var mNewFilter = false
    private var mUpdateInProcess = false
    private var isLastUser = false
    private var mIsOnline by Delegates.observable(false) { prop, old, new ->
        iconOnlineRes.set(if (new) R.drawable.im_list_online else 0)
    }

    private var mCurrentPosition by Delegates.observable(0) { prop, old, new ->
        updatePhotosCounter(new)
        loadBluredBackground(new)
        currentItem.set(new)
    }

    private var mIsDatingButtonEnable by Delegates.observable(true) { prop, old, new ->
        isChatButtonsEnable.set(new)
        isLikeButtonsEnable.set(new)
        isSkipButtonsEnable.set(new)
        isProfileButtonsEnable.set(new)
    }
    private val mPreloadManager by lazy {
        PreloadManager<SearchUser>()
    }

    private companion object {
        private const val PHOTOS_COUNTER = "photos_counter"
        private const val NAME = "name"
        private const val AGE = "age"
        private const val CITY = "city"
        private const val ALBUM_DATA = "album_data"
        private const val ONLINE = "online"
        private const val STATUS = "status"
        private const val NEED_ANIMATE_LOADER = "need_animate_loader"
        private const val CURRENT_POSITION = "current_position"
        private const val CURRENT_USER = "current_user"
        private const val LOADED_COUNT = "loaded_count"
        private const val CAN_SEND_ALBUM_REQUEST = "can_send_album_request"
        private const val NEED_MORE = "need_more"
        private const val CHAT_BUTTONS_ENABLE = "chat_buttons_enable"
        private const val LIKE_BUTTONS_ENABLE = "like_buttons_enable"
        private const val SKIP_BUTTONS_ENABLE = "skip_buttons_enable"
        private const val PROFILE_BUTTONS_ENABLE = "profile_buttons_enable"
        private const val UPDATE_IN_PROCESS = "update_in_process"
        private const val NEW_FILTER = "new_filter"

        private const val MAX_LIKE_AMOUNT = 4
    }


    var currentUser: SearchUser? = null
        set(value) {
            field = value?.apply {
                name.set(firstName)
                feedAge.set(String.format(App.getCurrentLocale(), AGE_TEMPLATE, age))
                feedCity.set(city.name)
                mIsOnline = online
                statusText.set(status)
            }
            albumDefaultBackground.set(R.drawable.bg_blur.getDrawable())
            mCurrentPosition = 0
            hideProgress()
        }

    init {
        App.get().inject(this)
        mUpdateActionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mPreloadManager.checkConnectionType()
                val type = intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE) as BlackListAndBookmarkHandler.ActionTypes?
                if (type != null) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (type) {
                        BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST -> {
                            skip()
                        }
                        BlackListAndBookmarkHandler.ActionTypes.SYMPATHY -> {
                            isLikeButtonsEnable.set(false)
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
        mProfileSubscription = appState.getObservable(Profile::class.java).distinctUntilChanged { t1, t2 -> t1.dating == t2.dating }.subscribe { profile ->
            if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty) {
                if (currentUser == null) {
                    mUserSearchList.currentUser?.let {
                        albumData.set(it.photos)
                        currentUser = it
                        //todo
//                        binding.root.post {
//                            //если есть currentUser, например после востановления стейта, то работаем с ним
//                            (if (currentUser == null) it else currentUser)?.let {
//                                mDatingButtonsView.unlockControls()
//                            }
//                        }
                    }
                }
            }
        }
        mUserSearchList.setOnEmptyListListener(this)
        mUserSearchList.updateSignatureAndUpdate()
    }

    fun updatePhotosCounter(position: Int) = photoCounter.set("${position + 1}/${currentUser?.photosCount}")

    fun showChat() = if (App.get().profile.premium) {
        mNavigator.showChat(currentUser, null)
    } else {
        mNavigator.showPurchaseVip("dating_fragment")
    }

    fun showProfile() {
        mNavigator.showProfile(currentUser, "dating_fragment")
    }

    fun skip() = currentUser?.let {
        if (!it.skipped && !it.rated) {
            showNextUser()
            mSkipSubscription = mApi.callSkipRequest(it.id).subscribe(object : Subscriber<IApiResponse>() {
                override fun onCompleted() = mSkipSubscription.safeUnsubscribe()
                override fun onError(e: Throwable?) = e?.printStackTrace() ?: Unit
                override fun onNext(t: IApiResponse?) {
                    for (user in mUserSearchList) {
                        if (user.id == it.id) {
                            user.skipped = true
                            return
                        }
                    }
                    mIsDatingButtonEnable = true
                }
            })
        } else {
            showNextUser()
        }
    }

    fun sendLike() = sendSomething {
        if (!it.rated) {
            tryShowTrialPopup(navigator = mNavigator)
            mLikeSubscription = mApi.callSendLike(it.id, App.get().options.blockUnconfirmed,
                    getMutualId(it), SendLikeRequest.FROM_SEARCH)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : Subscriber<Rate>() {
                        override fun onCompleted() {
                            mLikeSubscription.safeUnsubscribe()
                            validateDeviceActivation()
                        }

                        override fun onError(e: Throwable?) {
                            it.rated = false
                            mIsDatingButtonEnable = true
                        }

                        override fun onNext(rate: Rate?) {
                            it.rated = true
                            SearchCacheManager.markUserAsRatedInCache(it.id)
                            mIsDatingButtonEnable = true
                        }
                    })
        } else {
            showNextUser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            showProgress()
        }
        if (requestCode == PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK && data != null) {
            mCurrentPosition = data.getIntExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, 0)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            mIsDatingButtonEnable = false
            mEmptySearchVisibility.hideEmptySearchDialog()
            if (data != null && data.extras != null) {
                sendFilterRequest(data.getParcelableExtra<FilterData>(FilterFragment.INTENT_DATING_FILTER))
                mNewFilter = true
                FlurryManager.getInstance().sendFilterChangedEvent()
            }
        }
        /*Ушли в другую активити во время апдейта. Реквест на апдейт накрылся.
        По возвращении если нет юзеров в кэше, нужно дернуть апдейт.*/
        if (mUserSearchList.isEnded && !mUpdateInProcess) {
            if (resultCode == Activity.RESULT_CANCELED
                    && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
                Debug.log("LOADER_INTEGRATION after filter need update")
                update(false, false)
            }
            if (requestCode == PhotoSwitcherActivity.PHOTO_SWITCHER_ACTIVITY_REQUEST_CODE) {
                Debug.log("LOADER_INTEGRATION after album")
                update(false, false)
            }

        }
    }

    fun onPhotoClick() = with(currentUser) {
        if (this != null && photos != null && photos.isNotEmpty()) {
            mNavigator.showAlbum(mCurrentPosition, id, photosCount, photos)
        }
    }

    override fun onSavedInstanceState(state: Bundle) = with(state) {
        putBoolean(NEW_FILTER, mNewFilter)
        putBoolean(CAN_SEND_ALBUM_REQUEST, mCanSendAlbumReq)
        putInt(LOADED_COUNT, mLoadedCount)
        putParcelable(CURRENT_USER, currentUser)
        putInt(CURRENT_POSITION, mCurrentPosition)
        putBoolean(ONLINE, mIsOnline)
        putString(NAME, name.get())
        putString(CITY, feedCity.get())
        putString(AGE, feedAge.get())
        putString(STATUS, statusText.get())
        putString(PHOTOS_COUNTER, photoCounter.get())
        putParcelableArrayList(ALBUM_DATA, albumData.get() as? ArrayList<Photo>)
        putBoolean(NEED_MORE, mNeedMore)
        putBoolean(NEED_ANIMATE_LOADER, isNeedAnimateLoader.get())
        putBoolean(CHAT_BUTTONS_ENABLE, isChatButtonsEnable.get())
        putBoolean(LIKE_BUTTONS_ENABLE, isLikeButtonsEnable.get())
        putBoolean(SKIP_BUTTONS_ENABLE, isSkipButtonsEnable.get())
        putBoolean(PROFILE_BUTTONS_ENABLE, isProfileButtonsEnable.get())
        putBoolean(UPDATE_IN_PROCESS, mUpdateInProcess)
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        albumData.set(getParcelableArrayList<Parcelable>(ALBUM_DATA) as? Photos)
        currentUser = getParcelable(CURRENT_USER)
        mNewFilter = getBoolean(NEW_FILTER)
        mCanSendAlbumReq = getBoolean(CAN_SEND_ALBUM_REQUEST)
        mLoadedCount = getInt(LOADED_COUNT)
        name.set(getString(NAME))
        feedCity.set(getString(CITY))
        feedAge.set(getString(AGE))
        mIsOnline = getBoolean(ONLINE)
        statusText.set(getString(STATUS))
        photoCounter.set(getString(PHOTOS_COUNTER))
        mUpdateInProcess = getBoolean(DatingFragmentViewModel.UPDATE_IN_PROCESS)
        mNeedMore = getBoolean(NEED_MORE)
        isNeedAnimateLoader.set(getBoolean(NEED_ANIMATE_LOADER))
        isChatButtonsEnable.set(getBoolean(CHAT_BUTTONS_ENABLE))
        isLikeButtonsEnable.set(getBoolean(LIKE_BUTTONS_ENABLE))
        isSkipButtonsEnable.set(getBoolean(SKIP_BUTTONS_ENABLE))
        isProfileButtonsEnable.set(getBoolean(PROFILE_BUTTONS_ENABLE))
        mCurrentPosition = getInt(CURRENT_POSITION)
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
                        if (mCurrentPosition > mLoadedCount + mController.itemsOffsetByConnectionType) {
                            sendAlbumRequest(data)
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

    fun release() {
        mAlbumSubscription.safeUnsubscribe()
        mSkipSubscription.safeUnsubscribe()
        mLikeSubscription.safeUnsubscribe()
        mUpdateSubscription.safeUnsubscribe()
        mLoadBackgroundSubscription.safeUnsubscribe()
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdateActionsReceiver)
    }

    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        if (position + mController.itemsOffsetByConnectionType == mLoadedCount - 1) {
            with(albumData.get()) {
                if (mNeedMore && mCanSendAlbumReq && !isEmpty()) {
                    mCanSendAlbumReq = false
                    sendAlbumRequest(this)
                }
            }
        }
    }

    private fun loadBluredBackground(position: Int) =
            albumData.get()?.getOrNull(position)?.getSuitableLink(Photo.SIZE_128)?.let {
                mLoadBackground(it)
            }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // если true, значит viewPager ожил после переворота экрана и можно засетить текущую позицию
        if (position == 0 && positionOffset == 0f && positionOffsetPixels == 0) {
            currentItem.notifyChange()
        }
    }

    private fun isNeedTakePhoto() = !App.getConfig().userConfig.isUserAvatarAvailable
            && App.get().profile.photo == null

    private fun showNextUser() {
        if (mUserSearchList.searchPosition == mUserSearchList.size - 1 && mUserSearchList.isNeedPreload) {
            showProgress()
            return
        } else {
            hideProgress()
        }
        mUserSearchList.nextUser()?.let {
            mEmptySearchVisibility.hideEmptySearchDialog()
            mIsDatingButtonEnable = true
            setUser(it)
            currentUser = it
        }
    }

    private fun showProgress() {
        isDatingProgressBarVisible.set(View.VISIBLE)
        albumDefaultBackground.set(R.drawable.bg_blur.getDrawable())
        isVisible.set(View.INVISIBLE)
    }

    private fun hideProgress() {
        isDatingProgressBarVisible.set(View.GONE)
        isVisible.set(View.VISIBLE)
    }


    private fun sendSomething(func: (SearchUser) -> Unit) {
        if (App.isOnline()) {
            if (!isNeedTakePhoto()) {
                currentUser?.let {
                    showNextUser()
                    func(it)
                }
            } else {
                mNavigator.showTakePhotoPopup()
            }
        }
    }

    private fun validateDeviceActivation() {
        val appConfig = App.getAppConfig()
        var counter = appConfig.deviceActivationCounter
        if (counter < MAX_LIKE_AMOUNT) {
            appConfig.deviceActivationCounter = ++counter
        } else {
            if (!appConfig.isDeviceActivated) {
                AuthStatistics.sendDeviceActivated()
                appConfig.setDeviceActivated()
            }
        }
        appConfig.saveConfig()
    }

    private fun getMutualId(user: SearchUser) = if (user.isMutualPossible)
        SendLikeRequest.DEFAULT_MUTUAL
    else
        SendLikeRequest.DEFAULT_NO_MUTUAL

    override fun onEmptyList(usersList: UsersList<SearchUser>?) = update(mNewFilter, false)


    override fun onPreload(usersList: UsersList<SearchUser>?) {
        if (!mNewFilter) {
            update(false, true)
        }
    }

    fun update(isNeedRefresh: Boolean, isAddition: Boolean, onlyOnline: Boolean = DatingFilter.getOnlyOnlineField()) {
        Debug.log("LOADER_INTEGRATION start update")
        if (!mUpdateInProcess) {
            mIsDatingButtonEnable = false
            if (isNeedRefresh) {
                mUserSearchList.clear()
                currentUser = null
            }
            mUpdateInProcess = true
            mUpdateSubscription = mApi.callDatingUpdate(onlyOnline, isNeedRefresh).subscribe(object : Observer<UsersList<SearchUser>> {
                override fun onCompleted() {
                    mUpdateInProcess = false
                    Debug.log("LOADER_INTEGRATION onCompleted $mUpdateInProcess")
                    mUpdateSubscription.safeUnsubscribe()
                }

                override fun onError(e: Throwable?) {
                    Debug.log("LOADER_INTEGRATION onError ${e?.message}")
                    mUpdateInProcess = false
                    mIsDatingButtonEnable = true
                    e?.printStackTrace()
                }

                override fun onNext(usersList: UsersList<SearchUser>?) {
                    Debug.log("LOADER_INTEGRATION onNext")
                    if (usersList != null && usersList.size != 0) {
                        val isNeedShowNext = if (isLastUser) false else mUserSearchList.isEnded
                        //Добавляем новых пользователей
                        mUserSearchList.addAndUpdateSignature(usersList)
                        mPreloadManager.preloadPhoto(mUserSearchList)
                        val user = if (isNeedShowNext) mUserSearchList.nextUser() else mUserSearchList.currentUser
                        if (user != null && currentUser !== user) {
                            albumData.set(user.photos)
                            currentUser = user
                            Debug.log("LOADER_INTEGRATION onNext onDataReceived")
                        } else if (mUserSearchList.isEmpty() || mUserSearchList.isEnded) {
                            mEmptySearchVisibility.showEmptySearchDialog()
                            isLastUser = true
                        }
                        mIsDatingButtonEnable = true
                    } else {
                        if (!isAddition || mUserSearchList.isEmpty()) {
                            mEmptySearchVisibility.showEmptySearchDialog()
                            isLastUser = true
                        }
                    }
                }

            })
        }
    }

    private fun sendFilterRequest(filter: FilterData) {
        mApi.callFilterRequest(filter).subscribe(object : Subscriber<DatingFilter>() {
            override fun onNext(filter: DatingFilter?) {
                val profile = App.get().profile
                profile.dating = filter
                appState.setData(profile)
                mUserSearchList.updateSignatureAndUpdate()
                update(false, false)
                mNewFilter = false
            }

            override fun onCompleted() {
                mNewFilter = false
                mIsDatingButtonEnable = false
            }

            override fun onError(e: Throwable?) {
                mIsDatingButtonEnable = false
                mNewFilter = false
                mEmptySearchVisibility.showEmptySearchDialog()
                Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG)
            }
        })
    }
}