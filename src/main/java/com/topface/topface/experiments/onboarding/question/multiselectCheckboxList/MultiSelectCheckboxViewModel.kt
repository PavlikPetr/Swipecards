package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import android.databinding.ObservableField
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.MultiselectListItem
import com.topface.topface.experiments.onboarding.question.QuestionTypeFourth
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONArray
import org.json.JSONObject
import rx.Subscription
import java.util.*

class MultiSelectCheckboxViewModel(bundle: Bundle): ILifeCycle {

    companion object{
        private const val TITLE = "MultiSelectCheckboxListFragment.Title"
        private const val SELECTED_CHECKBOXES = "MultiSelectCheckboxListFragment.SelectedCheckboxes"
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mData: QuestionTypeFourth = bundle.getParcelable(MultiSelectCheckboxListFragment.EXTRA_DATA)
    val itemsList = mData.list.toList()
    val data = SingleObservableArrayList<MultiselectListItem>().apply { addAll(itemsList) }
    val title = ObservableField<String>(mData.title)
    var selectedCheckboxes = getSelectedCheckboxes(itemsList)
    private var mCheckboxSubscription: Subscription? = null

    fun onButtonClick() {
        App.getAppComponent().eventBus().setData(UserChooseAnswer(JSONObject().apply {
            mData?.let {
                put(it.fieldName, JSONArray(selectedCheckboxes))
            }
        }))
    }

    init {
        mCheckboxSubscription = mEventBus
                .getObservable(CheckboxSelected::class.java)
                .subscribe(shortSubscription {
                    addToValuesArray(selectedCheckboxes, it.value)
                })
    }

    private fun getSelectedCheckboxes(itemsList: List<MultiselectListItem>): MutableList<String> {
        var selectedList = mutableListOf<String>()
        for (item in itemsList) {
            if (item.isSelected) {
                selectedList.add(item.value)
            }
        }
        return selectedList
    }

    private fun addToValuesArray(valuesArray: MutableList<String>, value: String) {
        if (valuesArray.contains(value)) {
            valuesArray.remove(value)
        } else {
            valuesArray.add(value)
        }
    }

    override fun onSavedInstanceState(state: Bundle) {
        super.onSavedInstanceState(state)
        with(state) {
            putParcelable(MultiSelectCheckboxListFragment.EXTRA_DATA, mData)
            putString(TITLE, title.get())
            putStringArrayList(SELECTED_CHECKBOXES, selectedCheckboxes as ArrayList<String>)
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        with(state) {
            mData = getParcelable(MultiSelectCheckboxListFragment.EXTRA_DATA)
            title.set(getString(TITLE))
            selectedCheckboxes = getStringArrayList(SELECTED_CHECKBOXES)
        }
    }


    fun release(){
        mCheckboxSubscription.safeUnsubscribe()
    }
}