package com.topface.topface.ui.fragments.feed.dating

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.topface.topface.R
import com.topface.topface.data.DatingFilter
import com.topface.topface.databinding.LayoutEmptyDatingBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.Utils
import rx.Subscriber


/**
 * Created by mbulgakov on 07.11.16.
 */
class DatingEmptyFragment(private val mApi: FeedApi) : AbstractDialogFragment(), View.OnClickListener {

    override fun initViews(root: View) = with(LayoutEmptyDatingBinding.bind(root)) {
        root.findViewById(R.id.btnChangeFilterDating).setOnClickListener(this@DatingEmptyFragment)
        root.findViewById(R.id.btnClearFilterDating).setOnClickListener(this@DatingEmptyFragment)
    }

    override fun isModalDialog(): Boolean {
        return false
    }

    override fun getDialogLayoutRes() = R.layout.layout_empty_dating

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnChangeFilterDating -> startDatingFilterActivity()
            R.id.btnClearFilterDating -> cleanDatingFilter()
        }
    }

    private fun cleanDatingFilter() {
        mApi.callResetFilterRequest().subscribe(object : Subscriber<DatingFilter>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG);
            }

            override fun onNext(t: DatingFilter?) {
                dialog.cancel()
            }

        })
    }

    private fun startDatingFilterActivity() {
        val intent = Intent(activity.applicationContext, EditContainerActivity::class.java)
        startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER)
    }
}