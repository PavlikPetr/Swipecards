package com.topface.topface.experiments.onboarding.question

import android.os.Bundle
import android.os.PersistableBundle
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.AcFragmentFrameBinding
import com.topface.topface.experiments.onboarding.question.range.QRangeFragment
import com.topface.topface.ui.BaseFragmentActivity

/**
 * Activity with questions
 */
class QuestionaryActivity: BaseFragmentActivity<AcFragmentFrameBinding>() {
    override fun getToolbarBinding(binding: AcFragmentFrameBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.ac_fragment_frame

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Debug.log("---- on create")
        supportFragmentManager.beginTransaction().add(R.id.fragment_content, QRangeFragment(), "fffraaagment").commit()

    }
}