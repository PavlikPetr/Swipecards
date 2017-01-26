package com.topface.topface.ui.add_to_photo_blog

import com.topface.topface.App
import com.topface.topface.data.Profile
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.databinding.IOnListChangedCallbackBinded
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

/**
 * View model for list of users photos
 * Created by mbayutin on 12.01.17.
 */
class PhotoListItemViewModel: IOnListChangedCallbackBinded {
    val data = SingleObservableArrayList<Any>()
    @Inject lateinit var appState: TopfaceAppState

    private var mProfileSubscription: Subscription? = null

    init {
        App.get().inject(this)
        data.onCallbackBinded = this
    }

    override fun onCallbackBinded() {
        mProfileSubscription = appState.getObservable(Profile::class.java).subscribe {
            data.observableList.clear()
            val cleanPhotos = it.photos.photosForPhotoBlog()

            when(cleanPhotos.size) {
                0, 1 -> return@subscribe
                else -> {
                    data.observableList.addAll(cleanPhotos)
                }
            }
        }
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
        data.removeListener()
    }
}