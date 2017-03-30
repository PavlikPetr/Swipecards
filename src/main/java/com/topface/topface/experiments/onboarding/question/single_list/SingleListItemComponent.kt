package com.topface.topface.experiments.onboarding.question.single_list

import com.topface.topface.R
import com.topface.topface.databinding.OnboardingQSingleListItemBinding
import com.topface.topface.experiments.onboarding.question.QuestionSingleChoiceButton
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

internal class SingleListItemComponent: AdapterComponent<OnboardingQSingleListItemBinding, QuestionSingleChoiceButton>() {
    override val itemLayout: Int
        get() = R.layout.onboarding_q_single_list_item
    override val bindingClass: Class<OnboardingQSingleListItemBinding>
        get() = OnboardingQSingleListItemBinding::class.java

    override fun bind(binding: OnboardingQSingleListItemBinding, data: QuestionSingleChoiceButton?, position: Int) {
        data?.let {
            binding.viewModel = SingleListItemViewModel(it)
        }
    }
}