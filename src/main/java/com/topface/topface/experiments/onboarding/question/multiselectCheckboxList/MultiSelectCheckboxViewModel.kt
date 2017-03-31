package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.ObservableField
import android.os.Bundle
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.MultiselectListItem
import com.topface.topface.experiments.onboarding.question.QuestionTypeFourth
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONArray
import org.json.JSONObject
import rx.Subscription


class MultiSelectCheckboxViewModel(bundle: Bundle) {


    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private val mData: QuestionTypeFourth = bundle.getParcelable(MultiSelectCheckboxListFragment.EXTRA_DATA)
    val itemsList = mData.list.toList()
    val data = SingleObservableArrayList<MultiselectListItem>().apply { addAll(itemsList) }
    val title = ObservableField<String>(mData.title)
    var selectedCheckboxes = getSelectedCheckboxes(itemsList)
    private var mCheckboxSubscription: Subscription? = null


    fun onButtonClick() {
        val huial = JSONObject().apply {
            mData?.let {
                put(it.fieldName, JSONArray(selectedCheckboxes))
            }
        }
        App.getAppComponent().eventBus().setData(UserChooseAnswer(huial))

        Debug.error("                ответ в джейсоне           ${huial.toString()} ")
    }

    init {
        mCheckboxSubscription = mEventBus
                .getObservable(CheckboxSelected::class.java)
                .subscribe(shortSubscription {
                    addToValuesArray(selectedCheckboxes, it.value)
                })
    }

    fun getSelectedCheckboxes(itemsList: List<MultiselectListItem>): MutableList<String> {
        var selectedList = mutableListOf<String>()
        for (item in itemsList) {
            if (item.isSelected) {
                selectedList.add(item.value)
            }
        }
        return selectedList
    }

    fun addToValuesArray(valuesArray: MutableList<String>, value: String) {
        if (valuesArray.contains(value)) {
            valuesArray.remove(value)
        } else {
            valuesArray.add(value)
        }
    }
}