package com.topface.topface.experiments.fb_invitation

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.StyleRes
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.statistics.generated.QuestionnaireStatisticsGeneratedStatistics
import com.topface.topface.R
import com.topface.topface.databinding.FbInvitationBinding
import com.topface.topface.ui.dialogs.BaseDialog
import com.topface.topface.ui.dialogs.IDialogCloser
import org.jetbrains.anko.layoutInflater

class FBinvitationFragment : BaseDialog(), IDialogCloser {

    companion object {
        const val TAG = "FBInvitation"
    }

    private val mViewModel by lazy { FBInvitationViewModel(this) }

    private val mBinding by lazy {
        DataBindingUtil.inflate<FbInvitationBinding>(context.layoutInflater,
                dialogLayoutRes, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        QuestionnaireStatisticsGeneratedStatistics.sendNow_FB_INVITATION_SHOW()
        mBinding.viewModel = mViewModel
        with(mBinding.pagerIntroduction) {
            adapter = object : PagerAdapter() {

                override fun isViewFromObject(view: View?, blabla: Any?): Boolean = view == blabla

                private val sDrawables = kotlin.arrayOf(com.topface.topface.R.drawable.ic_clocks, com.topface.topface.R.drawable.ic_messages, com.topface.topface.R.drawable.ic_profiles, com.topface.topface.R.drawable.ic_people)

                override fun getCount(): Int = sDrawables.size

                override fun instantiateItem(container: ViewGroup, position: Int): View {
                    val itemViewModel = com.topface.topface.experiments.fb_invitation.FBInvitationItemViewModel(sDrawables[position])
                    val itemBinding = com.topface.topface.databinding.FbInvitationItemBinding.inflate(android.view.LayoutInflater.from(context))
                    itemBinding.viewModel = itemViewModel
                    container.addView(itemBinding.root)
                    return itemBinding.root
                }

                override fun destroyItem(container: ViewGroup, position: Int, blabla: Any) = container.removeView(blabla as View)

            }
            mBinding.indicator.setViewPager(this)
        }
        return mBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return NoBackStackDialog(activity, theme) {
            activity.finish()
        }
    }

    override fun initViews(root: View?) {}

    @StyleRes
    override fun getDialogStyleResId() = R.style.Theme_Topface_NoActionBar

    override fun getDialogLayoutRes() = R.layout.fb_invitation

    override fun closeIt() = dialog?.cancel() ?: Unit
}