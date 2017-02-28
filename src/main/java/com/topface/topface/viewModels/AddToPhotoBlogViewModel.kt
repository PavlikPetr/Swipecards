package com.topface.topface.viewModels

import android.content.Intent
import android.os.Message
import android.util.Log
import com.topface.topface.App
import com.topface.topface.data.*
import com.topface.topface.databinding.AddToPhotoBlogLayoutBinding
import com.topface.topface.requests.AlbumRequest
import com.topface.topface.requests.ApiResponse
import com.topface.topface.requests.DataApiHandler
import com.topface.topface.requests.IApiResponse
import com.topface.topface.ui.adapters.LeadersRecyclerViewAdapter
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.subscriptions.CompositeSubscription

/**
 * Модель для экрана постановки в фотоблог
 * Created by tiberal on 25.07.16.
 */
class AddToPhotoBlogViewModel(binding: AddToPhotoBlogLayoutBinding, private val mPhotoHelper: AddPhotoHelper) :
        BaseViewModel<AddToPhotoBlogLayoutBinding>(binding) {

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }
    private val mSubscriptions = CompositeSubscription()
    lateinit private var mBalance: BalanceData
    private val mAdapter: LeadersRecyclerViewAdapter? by lazy { binding.userPhotosGrid.adapter as LeadersRecyclerViewAdapter }

    init {
        Log.e("LEADER_PHOTO", "init AddToPhotoBlogViewModel")
        mSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(shortSubscription {
            it?.let {
                mBalance = it
            }
        }))
        mSubscriptions.add(mAppState.getObservable(Profile::class.java).subscribe(shortSubscription {
            it?.let {
                Log.e("LEADER_PHOTO", "profile onNext")
                handlePhotos(it)
            }
        }))
        mPhotoHelper.setOnResultHandler(object : android.os.Handler() {
            override fun handleMessage(msg: Message) {
                AddPhotoHelper.handlePhotoMessage(msg)
            }
        })
    }

    private fun handlePhotos(profile: Profile) {
        Log.e("LEADER_PHOTO", "updated profile")
        if (profile.photos.size == 0) {
            Log.e("LEADER_PHOTO", "clean adapter")
            with(mAdapter!!) {
                adapterData.clear()
                adapterData.add(Photo.createFakePhoto())
                notifyDataSetChanged()
            }
            return
        }
        Observable.just(profile)
                .flatMap { profile -> Observable.from(profile.photos) }
                .filter { photo ->
                    mAdapter != null && !mAdapter!!.adapterData.contains(photo)
                }
                .reduce(Photos()) { photos, photo ->
                    photos.add(photo)
                    photos
                }.subscribe(shortSubscription {
            it?.let { mAdapter?.addPhotos(it.photosForPhotoBlog(), profile.photos.size < profile.photosCount, false, true) }
        })
    }

    fun sendAlbumRequest() {
        val photoLinks: Photos? = mAdapter?.adapterData
        if (photoLinks == null || photoLinks.size < 2) {
            return
        }
        val position = mAdapter?.getItem(photoLinks.size - 2)?.getPosition() ?: 0
        AlbumRequest(context, App.get().profile.uid, position + 1, AlbumRequest.MODE_ALBUM,
                AlbumLoadController.FOR_GALLERY, true).callback(object : DataApiHandler<AlbumPhotos>() {
            override fun success(data: AlbumPhotos, response: IApiResponse) = mAdapter?.addPhotos(data, data.more, false, false) ?: Unit
            override fun parseResponse(response: ApiResponse) = AlbumPhotos(response)
            override fun fail(codeError: Int, response: IApiResponse) = Utils.showErrorMessage()
        }).exec()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    override fun release() {
        super.release()
        mPhotoHelper.releaseHelper()
        mSubscriptions.safeUnsubscribe()
        Log.e("LEADER_PHOTO", "release")
    }
}
