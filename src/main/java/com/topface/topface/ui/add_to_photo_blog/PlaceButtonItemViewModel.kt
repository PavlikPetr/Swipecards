package com.topface.topface.ui.add_to_photo_blog

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.state.EventBus
import com.topface.topface.utils.Utils
import javax.inject.Inject

/**
 * View model for button "Разместить"
 * Created by mbayutin on 11.01.17.
 */
class PlaceButtonItemViewModel(val price: Int) {
    @Inject lateinit var mEventBus: EventBus

    val priceText : ObservableField<String> by lazy {
        val text : String = Utils.getQuantityString(R.plurals.add_to_photo_blog_coins, price, price)
        ObservableField(text)
    }

    init {
        App.get().inject(this)
    }

    fun onClick() {
        mEventBus.setData(PlaceButtonTapEvent())
    }
}