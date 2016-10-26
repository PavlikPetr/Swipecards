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
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.adapters.LeadersRecyclerViewAdapter
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import rx.Observable
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * Модель для экрана постановки в фотоблог
 * Created by tiberal on 25.07.16.
 */
class AddToPhotoBlogViewModel(binding: AddToPhotoBlogLayoutBinding, private val mPhotoHelper: AddPhotoHelper) :
        BaseViewModel<AddToPhotoBlogLayoutBinding>(binding) {

    @Inject lateinit internal var mAppState: TopfaceAppState
    private val mSubscriptions = CompositeSubscription()
    lateinit private var mBalance: BalanceData
    private val mAdapter: LeadersRecyclerViewAdapter? by lazy { binding.userPhotosGrid.adapter as LeadersRecyclerViewAdapter }

    init {
        Log.e("LEADER_PHOTO", "init AddToPhotoBlogViewModel")
        App.get().inject(this)
        mSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
            override fun onNext(balance: BalanceData?) = balance?.let {
                mBalance = it
            } ?: Unit
        }))
        mSubscriptions.add(mAppState.getObservable(Profile::class.java).subscribe(object : RxUtils.ShortSubscription<Profile>() {
            override fun onNext(profile: Profile?) = profile?.let {
                Log.e("LEADER_PHOTO", "profile onNext")
                handlePhotos(it)
            } ?: Unit

            override fun onCompleted() {
                super.onCompleted()
                Log.e("LEADER_PHOTO", "profile onCompleted")
            }

            override fun onError(e: Throwable?) {
                super.onError(e)
                Log.e("LEADER_PHOTO", "profile onError ${e}")
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
                    Log.e("LEADER_PHOTO", "filter")
                    mAdapter != null && !mAdapter!!.adapterData.contains(photo)
                }
                .reduce(Photos()) { photos, photo ->
                    photos.add(photo)
                    photos
                }.subscribe(object : RxUtils.ShortSubscription<Photos>() {
            override fun onNext(photos: Photos) {
                Log.e("LEADER_PHOTO", "add photos")
                mAdapter?.addPhotos(photos.photosForPhotoBlog(), profile.photos.size < profile.photosCount, false, true)
            }

            override fun onCompleted() {
                super.onCompleted()
            }

            override fun onError(e: Throwable?) {
                super.onError(e)
            }
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
            override fun success(data: AlbumPhotos, response: IApiResponse) = mAdapter?.addPhotos(data, data.more, false, false)?:Unit
            override fun parseResponse(response: ApiResponse) = AlbumPhotos(response)
            override fun fail(codeError: Int, response: IApiResponse) = Utils.showErrorMessage()
        }).exec()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mPhotoHelper.processRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun release() {
        super.release()
        mPhotoHelper.releaseHelper()
        mSubscriptions.safeUnsubscribe()
        Log.e("LEADER_PHOTO", "release")
    }
}
