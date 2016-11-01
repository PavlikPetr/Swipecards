package com.topface.topface.ui.fragments.profile

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.R
import com.topface.topface.databinding.FragmentProfilePhotosBinding
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.viewModels.BaseViewModel

/**
 * Created by ppavlik on 01.11.16.
 */

class ProfilePhotoFragmentViewModel(binding: FragmentProfilePhotosBinding,
                                    val takeCameraPhoto: () -> Unit) : BaseViewModel<FragmentProfilePhotosBinding>(binding) {

    fun onCancelClick() {
        binding.vfFlipper.displayedChild = 0
    }

    fun onTakeAlbumPhotoClick(){
        binding.vfFlipper.displayedChild = 0
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra(AddPhotoHelper.EXTRA_BUTTON_ID, R.id.btnAddPhotoAlbum))
    }
}