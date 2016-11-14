package com.topface.topface.ui.dialogs.take_photo

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.IntDef
import android.view.View
import android.widget.TextView
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.TakePhotoDialogBinding
import com.topface.topface.state.EventBus
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.utils.debug.FuckingVoodooMagic
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

/**
 * Выбираем фоточку, показывается если нет ни одной
 */
@RuntimePermissions
class TakePhotoPopup : AbstractDialogFragment(), View.OnClickListener {

    companion object {

        const val TAG = "take_photo_popup"
        const val EXTRA_PLC = "TakePhotoActivity.Extra.Plc"
        const val PLC_UNDEFINED = "plc_undefined"

        const val ACTION_UNDEFINED = 0L
        const val ACTION_CAMERA_CHOSEN = 1L
        const val ACTION_GALLERY_CHOSEN = 2L
        const val ACTION_CANCEL = 3L

        fun newInstance(plc: String) = TakePhotoPopup().apply {
            arguments = Bundle().apply { putString(EXTRA_PLC, plc) }
        }
    }

    @IntDef(ACTION_UNDEFINED, ACTION_CAMERA_CHOSEN, ACTION_GALLERY_CHOSEN, ACTION_CANCEL)
    annotation class TakePhotoPopupAction

    @Inject lateinit var mEventBus: EventBus
    private var mArgs: Bundle? = null
    private lateinit var mBinding: TakePhotoDialogBinding
    private val mViewModel by lazy {
        TakePhotoPopupViewModel(mBinding, {
            TakePhotoPopupPermissionsDispatcher.takePhotoWithCheck(this)
        }, {
            mEventBus.setData(TakePhotoActionHolder(ACTION_GALLERY_CHOSEN, mArgs!!.getString(EXTRA_PLC)))
            dialog.cancel()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
        mArgs = arguments
        mArgs = if (mArgs == null) savedInstanceState else mArgs
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = object : Dialog(activity, theme) {
        override fun onBackPressed() {
            super.onBackPressed()
            mArgs?.let {
                mEventBus.setData(TakePhotoActionHolder(ACTION_CANCEL, it.getString(EXTRA_PLC)))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        TakePhotoPopupPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
        App.getAppConfig().putPermissionsState(permissions, grantResults)
    }

    override fun initViews(root: View) {
        mBinding = TakePhotoDialogBinding.bind(root).apply {
            viewModel = mViewModel
            (root.findViewById(R.id.title) as TextView).setText(R.string.take_photo)
            root.findViewById(R.id.title_clickable).setOnClickListener(this@TakePhotoPopup)
        }
    }

    @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
    override fun onClick(v: View) {
        mEventBus.setData(TakePhotoActionHolder(ACTION_CANCEL, mArgs?.getString(EXTRA_PLC) ?: PLC_UNDEFINED))
        dialog.cancel()
    }

    override fun getDialogLayoutRes() = R.layout.take_photo_dialog

    override fun isModalDialog() = false

    override fun isUnderActionBar() = false

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePhoto() {
        mEventBus.setData(TakePhotoActionHolder(ACTION_CAMERA_CHOSEN, mArgs?.getString(EXTRA_PLC) ?: PLC_UNDEFINED))
        dialog.cancel()
    }
}
