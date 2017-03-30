package com.topface.topface.experiments.onboarding.question.single_list

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.Bundle
import com.topface.topface.experiments.onboarding.question.Button
import com.topface.topface.experiments.onboarding.question.QuestionTypeSecond
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.SingleObservableArrayList

/**
 * VM for question with list of answers and single choice
 */
class QSingleListFragmentViewModel(bundle: Bundle) {
    private val mData: QuestionTypeSecond? = bundle.getParcelable(QSingleListFragment.EXTRA_DATA)
    val data = SingleObservableArrayList<Any>()

    val title = ObservableField<String>(mData?.title ?: Utils.EMPTY)
    val fieldName: String = mData?.fieldName ?: Utils.EMPTY
    val isEnabled = ObservableBoolean(true)

    init {
        mData?.let {
            val l = mutableListOf<Button>()
            it.buttons.forEach { l.add(it) }
            data.addAll(l)
        }
    }
}