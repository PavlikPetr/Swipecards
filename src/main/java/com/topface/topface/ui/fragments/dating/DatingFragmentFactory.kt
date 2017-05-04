package com.topface.topface.ui.fragments.dating

import com.topface.topface.App
import com.topface.topface.ui.fragments.BaseFragment

/**
 * Creates dating fargment based on current dating-fragment-design-version
 */
class DatingFragmentFactory(val isNeedTranslucent: Boolean) {
    companion object {
        /**
         * scrollable profile
         */
        const val DEFAULT_DESIGN = 0

        /**
         * dating with translucent status bar
         */
        const val V1_DESIGN = 1

        /**
         * scrollable profile without FAB ana with bigger buttons
         */
        const val V2_DESIGN = 2
    }

    fun construct(): BaseFragment = if (isNeedTranslucent) {
        com.topface.topface.ui.fragments.dating.design.v1.DatingFragment()
    } else {
        when(App.get().options.datingRedesign) {
            V2_DESIGN -> com.topface.topface.ui.fragments.dating.design.v2.DatingFragment()
            else -> DatingFragment()
        }
    }
}