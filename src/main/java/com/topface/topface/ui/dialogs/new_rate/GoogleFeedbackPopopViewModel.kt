package com.topface.topface.ui.dialogs.new_rate

import android.databinding.ObservableField

class GoogleFeedbackPopopViewModel {

    val error = ObservableField<String>()
    val text = ObservableField<String>()

    fun okButtonClick(){
        if (!text.get().isNullOrEmpty()){
            //todo VSE NORM
        }
    }
    fun closeButtonClick(){

    }
}