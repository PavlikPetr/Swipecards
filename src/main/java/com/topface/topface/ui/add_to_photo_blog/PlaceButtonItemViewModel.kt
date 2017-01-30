package com.topface.topface.ui.add_to_photo_blog

import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.state.EventBus
import com.topface.topface.utils.Utils
import javax.inject.Inject

/**
 * View model for button "Разместить"
 * Created by mbayutin on 11.01.17.
 */
class PlaceButtonItemViewModel(val price: Int, val lastSelectedPhotoId: ObservableInt) {
    @Inject lateinit var mEventBus: EventBus
    val isEnabled = ObservableBoolean(true)

    val priceText by lazy { ObservableField<String>(Utils.getQuantityString(R.plurals.add_to_photo_blog_coins, price, price)) }

    private val mOnPropertyChangedCallback: Observable.OnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) {
            (observable as ObservableInt)?.let { isEnabled.set(it.get() != 0) }
        }
    }

    init {
        App.get().inject(this)
        lastSelectedPhotoId.addOnPropertyChangedCallback(mOnPropertyChangedCallback)
        isEnabled.set(lastSelectedPhotoId.get() != 0)
    }

    fun onClick() = mEventBus.setData(PlaceButtonTapEvent())

    fun release() = lastSelectedPhotoId.removeOnPropertyChangedCallback(mOnPropertyChangedCallback)
}