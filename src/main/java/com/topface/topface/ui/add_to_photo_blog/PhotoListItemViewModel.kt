package com.topface.topface.ui.add_to_photo_blog

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.Photo
import com.topface.topface.data.Profile
import com.topface.topface.requests.AlbumRequest
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.databinding.IOnListChangedCallbackBinded
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription
import javax.inject.Inject

/**
 * View model for list of users photos
 * Created by mbayutin on 12.01.17.
 */
class PhotoListItemViewModel(private val mApi: FeedApi, updateObservable: Observable<Bundle>): IOnListChangedCallbackBinded {
    val data = MultiObservableArrayList<Any>()
    @Inject lateinit var appState: TopfaceAppState
    private var mUpdateSubscription: Subscription
    private var mPhotosLoadSubscrioption: Subscription? = null

    private var mProfileSubscription: Subscription? = null

    private var  mHasInitialData = true
    private var mUpdateInProgress = false
    private var mLastLoadedPhotoPosition = 0

    init {
        App.get().inject(this)
        mUpdateSubscription = updateObservable.subscribe {
                if (mHasInitialData) {
                    onCallbackBinded()
                    mHasInitialData = false
                } else {
                    when {
                        data.getList().last() is Photo -> {
                            loadProfilePhotos(mLastLoadedPhotoPosition)
                        }
                    }
                }
            }
        }

    private fun loadProfilePhotos(loadedPosition: Int) {
        if (!mUpdateInProgress) {
            mUpdateInProgress = true
            mPhotosLoadSubscrioption = mApi.callAlbumRequest(
                    uid = App.get().profile.uid,
                    loadedPosition = loadedPosition,
                    mode = AlbumRequest.MODE_ALBUM,
                    type = AlbumLoadController.FOR_GALLERY
            ).subscribe(object: Subscriber<AlbumPhotos>() {
                override fun onCompleted() {
                    mUpdateInProgress = false
                    unsubscribe()
                }

                override fun onError(e: Throwable?) {
                    mUpdateInProgress = false
                }

                override fun onNext(data: AlbumPhotos?) {
                    mUpdateInProgress = false
                    data?.let {
                        val profile = App.get().profile
                        profile.photos.addAll(it)
                        appState.setData(profile)
                    }
                }
            })
        }
    }

    override fun onCallbackBinded() {
        mProfileSubscription = appState.getObservable(Profile::class.java).subscribe {
            mLastLoadedPhotoPosition = it.photos.last().position + 1
            val cleanPhotos = it.photos.photosForPhotoBlog()

            data.replaceData(arrayListOf<Any>().apply { addAll(cleanPhotos) })
        }
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
        mUpdateSubscription.safeUnsubscribe()
        mPhotosLoadSubscrioption.safeUnsubscribe()
    }
}