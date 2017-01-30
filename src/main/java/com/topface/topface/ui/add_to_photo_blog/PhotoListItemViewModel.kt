package com.topface.topface.ui.add_to_photo_blog

import android.databinding.ObservableInt
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.Photo
import com.topface.topface.data.Profile
import com.topface.topface.requests.AlbumRequest
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Observable
import rx.Subscriber
import rx.Subscription
import java.util.*
import javax.inject.Inject

/**
 * View model for list of users photos
 * Created by mbayutin on 12.01.17.
 */
class PhotoListItemViewModel(private val mApi: FeedApi, updateObservable: Observable<Bundle>, val lastSelectedPhotoId: ObservableInt) {
    val data = MultiObservableArrayList<Any>()
    @Inject lateinit var appState: TopfaceAppState
    private var mUpdateSubscription: Subscription
    private var mPhotosLoadSubscription: Subscription? = null

    private var mProfileSubscription: Subscription? = null

    private var  mHasInitialData = true
    private var mUpdateInProgress = false
    private var mLastLoadedPhotoPosition = 0

    init {
        App.get().inject(this)
        mUpdateSubscription = updateObservable.subscribe {
                if (mHasInitialData) {
                    loadDataFromProfile()
                    mHasInitialData = false
                } else {
                    mLastLoadedPhotoPosition = if (data.isEmpty()) 0 else (data.getList().last() as Photo).getPosition() + 1
                    loadProfilePhotos()
                }
            }
        }

    private fun loadProfilePhotos() {
        if (!mUpdateInProgress) {
            mUpdateInProgress = true
            mPhotosLoadSubscription = mApi.callAlbumRequest(
                    uid = App.get().profile.uid,
                    loadedPosition = mLastLoadedPhotoPosition,
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
                        it.removeAll(profile.photos)
                        if (profile.photos.addAll(it)) appState.setData(profile)
                    }
                }
            })
        }
    }

    private fun loadDataFromProfile() {
        mProfileSubscription = appState.getObservable(Profile::class.java).subscribe {
            if (it.photos.isNotEmpty()) {
                mLastLoadedPhotoPosition = it.photos.last().position + 1
                val cleanPhotos = it.photos.photosForPhotoBlog()
                val wasEmpty = data.isEmpty()

                data.replaceData(arrayListOf<Any>().apply { addAll(cleanPhotos) })
                if (wasEmpty && data.isNotEmpty() && lastSelectedPhotoId.get() == 0) lastSelectedPhotoId.set((data[0] as Photo).id)
            } else{
                data.replaceData(ArrayList<Any>())
                lastSelectedPhotoId.set(0)
            }
        }
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
        mUpdateSubscription.safeUnsubscribe()
        mPhotosLoadSubscription.safeUnsubscribe()
    }
}