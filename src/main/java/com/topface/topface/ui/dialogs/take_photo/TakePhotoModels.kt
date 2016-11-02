package com.topface.topface.ui.dialogs.take_photo

data class TakePhotoActionHolder @JvmOverloads constructor(@TakePhotoPopup.TakePhotoPopupAction val action: Long = TakePhotoPopup.ACTION_UNDEFINED,
                                                           val plc: String = TakePhotoPopup.PLC_UNDEFINED) {
}