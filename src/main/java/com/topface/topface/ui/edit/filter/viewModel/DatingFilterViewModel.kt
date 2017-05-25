package com.topface.topface.ui.edit.filter.viewModel

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import android.widget.CheckBox
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.City
import com.topface.topface.data.Profile
import com.topface.topface.ui.dialogs.CitySearchPopup
import com.topface.topface.ui.edit.filter.model.FilterData
import com.topface.topface.ui.views.RangeSeekBar
import com.topface.topface.ui.views.RangeSeekBar.OnRangeSeekBarChangeListener
import com.topface.topface.utils.IActivityDelegate
import java.util.*

class DatingFilterViewModel(private var mIActivityDelegate: IActivityDelegate?, filter: FilterData) : OnRangeSeekBarChangeListener<Int> {

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

    init {
        setStartingValue(filter)
    }

    private fun setStartingValue(filter: FilterData) {
        city.set(filter.city)
        isMaleSelected.set(filter.sex == Profile.BOY)
        onlineOnly.set(filter.isOnlineOnly)
        ageStart.set(filter.ageStart)
        ageEnd.set(filter.ageEnd)
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

    fun release() {
        mIActivityDelegate = null
    }
}
