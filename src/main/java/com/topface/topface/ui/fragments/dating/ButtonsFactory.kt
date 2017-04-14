package com.topface.topface.ui.fragments.dating

import android.os.Build
import com.topface.topface.App
import com.topface.topface.R

/**
 * Constructs buttons drawables based on dating-design version
 */
internal class ButtonsFactory {
    private val mDesignVersion = App.get().options.dialogDesignVersion
    private val mLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    fun constructButtonMessage() = if (mDesignVersion == DatingFragmentFactory.V2_DESIGN) {
        if (mLollipop) R.drawable.dating_send_message_selector_v2_v21 else R.drawable.dating_send_message_selector_v2
    } else {
        if (mLollipop) R.drawable.dating_send_message_selector_v21 else R.drawable.dating_send_message_selector
    }

    fun constructButtonLike() = if (mDesignVersion == DatingFragmentFactory.V2_DESIGN) {
        if (mLollipop) R.drawable.dating_like_selector_v2_v21 else R.drawable.dating_like_selector_v2
    } else {
        if (mLollipop) R.drawable.dating_like_selector_v21 else R.drawable.dating_like_selector
    }

    fun constructButtonSkip() = if (mDesignVersion == DatingFragmentFactory.V2_DESIGN) {
        if (mLollipop) R.drawable.dating_skip_selector_v2_v21 else R.drawable.dating_skip_selector_v2
    } else {
        if (mLollipop) R.drawable.dating_skip_selector_v21 else R.drawable.dating_skip_selector
    }

    fun constructButtonAdmiration() = if (mDesignVersion == DatingFragmentFactory.V2_DESIGN) {
        if (mLollipop) R.drawable.dating_admiration_selector_v2_v21 else R.drawable.dating_admiration_selector_v2
    } else {
        if (mLollipop) R.drawable.dating_admiration_selector_v21 else R.drawable.dating_admiration_selector
    }
}