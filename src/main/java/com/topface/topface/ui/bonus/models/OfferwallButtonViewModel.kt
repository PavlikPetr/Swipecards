package com.topface.topface.ui.bonus.models

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.ui.bonus.OfferwallButton
import com.topface.topface.ui.external_libs.ironSource.IronSourceOfferwallEvent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel

/**
 * Created by ppavlik on 02.06.17.
 * Вьюмодель для кнопки офервола на "разводящем" экране
 */
class OfferwallButtonViewModel(private val settings: OfferwallButton) : BaseViewModel() {

    val text = ObservableField(settings.text)
    val image = ObservableInt(settings.imgRes)

    private val mIronSourceManager by lazy {
        App.getAppComponent().ironSourceManager()
    }

    fun showOffer() {
        mIronSourceManager.emmitNewState(IronSourceOfferwallEvent.getOnOfferwallCall())
        mIronSourceManager.showOfferwallByType(settings.type)
    }
}