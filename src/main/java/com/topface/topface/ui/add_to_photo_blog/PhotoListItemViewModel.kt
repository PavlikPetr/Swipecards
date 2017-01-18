package com.topface.topface.ui.add_to_photo_blog

import com.topface.topface.App
import com.topface.topface.data.Profile
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.databinding.IOnListChangedCallbackBinded
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

/**
 * View model for list of users photos
 * Created by mbayutin on 12.01.17.
 */
class PhotoListItemViewModel(val lastSelectedPhotoId: Int) : IOnListChangedCallbackBinded {
    val data = SingleObservableArrayList<Any>()
    @Inject lateinit var appState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus


    private lateinit var mProfileSubscription: Subscription

    init {
        App.get().inject(this)
        data.onCallbackBinded = this
    }

    override fun onCallbackBinded() {
        mProfileSubscription = appState.getObservable(Profile::class.java).subscribe {
            data.observableList.clear()

            when(it.photosCount) {
                0 -> return@subscribe
                1 -> dispatchPhotoSelected(lastSelectedPhotoId)
                else -> {
                    data.observableList.addAll(it.photos)
                    dispatchPhotoSelected(lastSelectedPhotoId)
                }
            }
        }
    }

    private fun dispatchPhotoSelected(id: Int) {
        mEventBus.setData(PhotoSelectedEvent(id))
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
        data.removeListener()
    }
}