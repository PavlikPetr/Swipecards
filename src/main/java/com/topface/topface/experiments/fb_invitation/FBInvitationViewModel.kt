package com.topface.topface.experiments.fb_invitation

import com.topface.topface.App
import com.topface.topface.ui.dialogs.IDialogCloser

class FBInvitationViewModel(private val close: IDialogCloser) {

    fun fbButtonClick() {
        close.closeIt()
        App.getAppComponent().eventBus().setData(AuthFB())
    }
}