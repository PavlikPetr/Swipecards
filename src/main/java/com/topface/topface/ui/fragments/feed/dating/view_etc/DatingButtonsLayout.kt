package com.topface.topface.ui.fragments.feed.dating.view_etc

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/**
 * Layout for buttons. Must hide/show dating buttons.
 * Created by tiberal on 24.10.16.
 */
class DatingButtonsLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var datingButtonsVisibility: IDatingButtonsVisibility? = null

    override fun setVisibility(visibility: Int) =
            when (visibility) {
                View.VISIBLE -> datingButtonsVisibility?.showDatingButtons() ?: Unit
                View.INVISIBLE -> datingButtonsVisibility?.hideDatingButtons() ?: Unit
                else -> super.setVisibility(visibility)
            }

    interface IDatingButtonsVisibility {
        fun showDatingButtons()
        fun hideDatingButtons()
    }

}