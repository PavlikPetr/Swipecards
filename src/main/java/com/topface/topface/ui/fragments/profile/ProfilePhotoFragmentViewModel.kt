package com.topface.topface.ui.fragments.profile

import com.topface.topface.databinding.FragmentProfilePhotosBinding
import com.topface.topface.viewModels.BaseViewModel

class ProfilePhotoFragmentViewModel(binding: FragmentProfilePhotosBinding,
                                    val takeCameraPhoto: () -> Unit,
                                    val takeAlbumPhoto: () -> Unit) : BaseViewModel<FragmentProfilePhotosBinding>(binding) {
    fun onCancelClick() {
        binding.vfFlipper.displayedChild = 0
    }
}