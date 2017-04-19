package com.topface.topface.chat.dialogs.experiment57_2

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.DialogChat572Binding
import com.topface.topface.ui.PurchasesActivity
import com.topface.topface.ui.analytics.TrackedDialogFragment
import org.jetbrains.anko.layoutInflater

/**
 * HOW TO USE from ChatFragment
 * (Experiment572DialogFragment.Companion.newInstance(mUser)).show(getActivity().getSupportFragmentManager(), Experiment572DialogFragment.TAG);
 */
class Experiment572DialogFragment: TrackedDialogFragment() {
    companion object {
        const val TAG = "Experiment572DialogFragment.Tag"
        internal const val ARG_USER = "Experiment572DialogFragment.ArgUser"

        fun newInstance(user: FeedUser) = Experiment572DialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_USER, user)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<DialogChat572Binding>(context.layoutInflater, R.layout.dialog_chat_57_2, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.RoundedPopup)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View = with(mBinding) {
        viewModel = Experiment572DialogViewModel(arguments) {
            startActivity(PurchasesActivity.createVipBuyIntent(null, "PopularUserBlockDialog"))
            dialog.dismiss()
        }
        super.onCreateView(inflater, container, savedInstanceState)
        root
    }
}