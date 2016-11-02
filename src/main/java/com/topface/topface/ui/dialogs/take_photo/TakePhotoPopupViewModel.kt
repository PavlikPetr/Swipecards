package com.topface.topface.ui.dialogs.take_photo

import android.databinding.ObservableInt
import android.os.Bundle
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.TakePhotoDialogBinding
import com.topface.topface.state.EventBus
import com.topface.topface.viewModels.BaseViewModel
import javax.inject.Inject

/**
 * Вью-моделька для работы с попапом загрузки фото
 * Created by ppavlik on 28.10.16.
 */
class TakePhotoPopupViewModel(binding: TakePhotoDialogBinding,
                              val takeCameraPhoto: () -> Unit,
                              val takeAlbumPhoto: () -> Unit) : BaseViewModel<TakePhotoDialogBinding>(binding) {

    val mainImage = ObservableInt(if (App.get().profile.sex == Profile.GIRL) R.drawable.upload_photo_female else R.drawable.upload_photo_male)
}