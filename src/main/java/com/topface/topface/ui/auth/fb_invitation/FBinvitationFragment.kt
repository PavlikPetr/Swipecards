package com.topface.topface.ui.auth.fb_invitation

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.FbInvitationBinding
import com.topface.topface.databinding.FbInvitationItemBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import kotlin.properties.Delegates

/**
 * Created by mbulgakov on 28.03.17.
 */
class FBinvitationFragment : AbstractDialogFragment() {

    companion object {
        const val TAG = "FBInvitation"
    }

    private var mBinding by Delegates.notNull<FbInvitationBinding>()

    private val mViewModel by lazy { FBInvitationViewModel() }

    override fun initViews(root: View?) {
        mBinding = FbInvitationBinding.bind(root)
        mBinding.setViewModel(mViewModel)
        with(mBinding.pagerIntroduction) {
            adapter = object : PagerAdapter() {

                override fun isViewFromObject(view: View?, blabla: Any?): Boolean = view == blabla

                private val sDrawables = arrayOf(R.drawable.ic_clocks, R.drawable.ic_messages, R.drawable.ic_profiles, R.drawable.ic_people)

                override fun getCount(): Int = sDrawables.size

                override fun instantiateItem(container: ViewGroup, position: Int): View {
                    val itemViewModel = FBInvitationItemViewModel(sDrawables[position])
                    val itemBinding = FbInvitationItemBinding.inflate(LayoutInflater.from(context))
                    itemBinding.setViewModel(itemViewModel)
                    container.addView(itemBinding.root)
                    return itemBinding.root
                }

                override fun destroyItem(container: ViewGroup, position: Int, blabla: Any) = container.removeView(blabla as View)

            }
            mBinding.indicator.setViewPager(this)
        }
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.fb_invitation

}
