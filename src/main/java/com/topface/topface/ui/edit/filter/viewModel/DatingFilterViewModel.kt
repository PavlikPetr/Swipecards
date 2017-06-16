package com.topface.topface.ui.edit.filter.viewModel

import android.content.Intent
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import android.widget.CheckBox
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.City
import com.topface.topface.data.Profile
import com.topface.topface.experiments.AttractionExperiment
import com.topface.topface.ui.PurchasesActivity
import com.topface.topface.ui.dialogs.CitySearchPopup
import com.topface.topface.ui.edit.filter.model.FilterData
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.views.RangeSeekBar
import com.topface.topface.ui.views.RangeSeekBar.OnRangeSeekBarChangeListener
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import java.util.*

class DatingFilterViewModel(private var mIActivityDelegate: IActivityDelegate?, filter: FilterData)
    : OnRangeSeekBarChangeListener<Int>, ILifeCycle {

    companion object {
        const val MIN_AGE = 16
        const val MAX_AGE = 99
        const val MAX_AGE_TITLE = MAX_AGE.toString()
        const val MIN_AGE_TITLE = MIN_AGE.toString()
    }

    val isMaleSelected = ObservableBoolean(App.get().profile.sex == Profile.GIRL)
    val city = ObservableField(App.get().profile.city)
    val onlineOnly = ObservableBoolean()
    var isEnabled = ObservableBoolean(true)
    val ageStart = ObservableInt()
    val ageEnd = ObservableInt()
    val defaultCities = ObservableField<MutableList<City>>(prepareDefaultCityList())
    val isPrettyVisible = ObservableBoolean(AttractionExperiment.isSwitchPrettyControlVisible())
    val isPrettyOnly = ObservableBoolean()
    private val mFeedNavigator = mIActivityDelegate?.let { FeedNavigator(it) }

    init {
        setStartingValue(filter)
        mIActivityDelegate?.registerLifeCycleDelegate(this)
    }

    private fun setStartingValue(filter: FilterData) {
        city.set(filter.city)
        isMaleSelected.set(filter.sex == Profile.BOY)
        onlineOnly.set(filter.isOnlineOnly)
        ageStart.set(filter.ageStart)
        ageEnd.set(filter.ageEnd)
        isPrettyOnly.set(filter.isPrettyOnly)
    }

    override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>?, minValue: Int, maxValue: Int, thumbId: RangeSeekBar.Thumb?) {
        if (thumbId != null) {
            when (thumbId) {
                RangeSeekBar.Thumb.MAX -> ageEnd.set(maxValue)
                RangeSeekBar.Thumb.MIN -> ageStart.set(minValue)
            }
        } else {
            ageEnd.set(maxValue)
            ageStart.set(minValue)
        }
    }

    private fun prepareDefaultCityList(): MutableList<City> {
        val defCityName = App.getContext().resources.getString(R.string.filter_cities_all)
        return mutableListOf(City.createCity(City.ALL_CITIES, defCityName, defCityName))
    }

    fun onMaleCheckBoxClick() =
            with(isMaleSelected) {
                set(true)
                notifyChange()
            }

    fun onFemaleCheckBoxClick() =
            with(isMaleSelected) {
                set(false)
                notifyChange()
            }

    fun onSelectCityClick() = mIActivityDelegate?.let {
        with(CitySearchPopup.newInstance(null, defaultCities.get() as ArrayList<City>)) {
            setOnCitySelected { city -> this@DatingFilterViewModel.city.set(city) }
            show(mIActivityDelegate?.supportFragmentManager, CitySearchPopup.TAG)
        }
    }

    fun onOnlineOnlyClick(view: View) = onlineOnly.set((view as CheckBox).isChecked)

    fun onPrettyOnlyClick() {
        AttractionExperiment.doClickAction(
                {
                    // do nothing when group is unknown
                },
                {
                    // switch checkBox value
                    isPrettyOnly.set(!isPrettyOnly.get())
                },
                {
                    // this user must buy vip
                    mFeedNavigator?.showPurchaseVip("Dating Filter")
                }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PurchasesActivity.INTENT_BUY_VIP) {
            // мистическая ситуация, вернулись из окна покупки, но хз, купили вип или нет
            // на всякий случай посмотрим, изменился стасус вип или нет
            // и если да, то включим ему "только красивых", ведь покупать вип с этого экрана
            // он пойдет только нажав на этот контрол.. пока во всяком случае
            // если позже будут еще "платные" опции, то здесь будет говно
            if (App.get().profile.premium) {
                isPrettyOnly.set(true)
            }
        }
    }

    fun release() {
        mIActivityDelegate?.unregisterLifeCycleDelegate(this)
        mIActivityDelegate = null
    }
}
