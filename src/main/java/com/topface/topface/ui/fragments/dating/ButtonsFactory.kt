package com.topface.topface.ui.fragments.dating

import android.os.Build
import com.topface.topface.App
import com.topface.topface.R

/**
 * Constructs buttons drawables based on dating-design version
 */
internal class ButtonsFactory {
    companion object {
        private const val LIKE = 1
        private const val SKIP = 2
        private const val ADMIRATION = 3
        private const val MESSAGES = 4
    }

    private val mButtonDrawables: MutableMap<Int, Int> = mutableMapOf()

    init {
        val designVersion = App.get().options.dialogDesignVersion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // LOLLIPOP drawables
            when(designVersion) {
                DatingFragmentFactory.V2_DESIGN -> {
                    mButtonDrawables[LIKE] = R.drawable.dating_like_selector_v2_v21
                    mButtonDrawables[SKIP] = R.drawable.dating_skip_selector_v2_v21
                    mButtonDrawables[MESSAGES] = R.drawable.dating_send_message_selector_v2_v21
                    mButtonDrawables[ADMIRATION] = R.drawable.dating_admiration_selector_v2_v21
                }
                else -> {
                    mButtonDrawables[LIKE] = R.drawable.dating_like_selector_v21
                    mButtonDrawables[SKIP] = R.drawable.dating_skip_selector_v21
                    mButtonDrawables[MESSAGES] = R.drawable.dating_send_message_selector_v21
                    mButtonDrawables[ADMIRATION] = R.drawable.dating_admiration_selector_v21
                }
            }
        } else {
            // PRE-LOLLIPOP drawables
            when(designVersion) {
                DatingFragmentFactory.V2_DESIGN -> {
                    mButtonDrawables[LIKE] = R.drawable.dating_like_selector_v2
                    mButtonDrawables[SKIP] = R.drawable.dating_skip_selector_v2
                    mButtonDrawables[MESSAGES] = R.drawable.dating_send_message_selector_v2
                    mButtonDrawables[ADMIRATION] = R.drawable.dating_admiration_selector_v2
                }
                else -> {
                    mButtonDrawables[LIKE] = R.drawable.dating_like_selector
                    mButtonDrawables[SKIP] = R.drawable.dating_skip_selector
                    mButtonDrawables[MESSAGES] = R.drawable.dating_send_message_selector
                    mButtonDrawables[ADMIRATION] = R.drawable.dating_admiration_selector
                }
            }
        }
    }

    private fun constructButtonDrawable(id: Int) = mButtonDrawables[id]

    fun constructButtonMessage() = constructButtonDrawable(MESSAGES)

    fun constructButtonLike() = constructButtonDrawable(LIKE)

    fun constructButtonSkip() = constructButtonDrawable(SKIP)

    fun constructButtonAdmiration() = constructButtonDrawable(ADMIRATION)
}