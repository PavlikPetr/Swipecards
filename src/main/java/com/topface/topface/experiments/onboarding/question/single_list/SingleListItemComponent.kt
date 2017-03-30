package com.topface.topface.experiments.onboarding.question.single_list

import android.databinding.ObservableBoolean
import com.topface.topface.R
import com.topface.topface.databinding.OnboardingQSingleListItemBinding
import com.topface.topface.experiments.onboarding.question.Button
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

internal class SingleListItemComponent(val fieldName: String, val isEnabled: ObservableBoolean): AdapterComponent<OnboardingQSingleListItemBinding, Button>() {
    override val itemLayout: Int
        get() = R.layout.questionnaire_q_single_list_item
    override val bindingClass: Class<OnboardingQSingleListItemBinding>
        get() = OnboardingQSingleListItemBinding::class.java

    override fun bind(binding: OnboardingQSingleListItemBinding, data: Button?, position: Int) {
        data?.let {
            binding.viewModel = SingleListItemViewModel(it, fieldName, isEnabled)
        }
    }
}