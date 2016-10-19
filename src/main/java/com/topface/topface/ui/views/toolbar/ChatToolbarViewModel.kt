package com.topface.topface.ui.views.toolbar

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.topface.topface.R
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding

/**
 * Created by petrp on 09.10.2016.
 * вью модель тулбара для чата
 */

class ChatToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
: BaseToolbarViewModel(binding, mNavigation) {
    var additionalViewBinding: CustomTitleAndSubtitleToolbarAdditionalViewBinding

    init {
        title.set("")
        additionalViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.custom_title_and_subtitle_toolbar_additional_view, null, false)
        binding.toolbarCustomView.addView(additionalViewBinding.root)
    }

    fun setTitle(title: String) {
        additionalViewBinding.title.text = title
    }

    fun setSubTitle(subtitle: String) {
        additionalViewBinding.subtitle.text = subtitle
    }

    fun setOnlineState(isOnline: Boolean) {
        additionalViewBinding.title.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                if (isOnline)
                    R.drawable.im_list_online
                else
                    0,
                0)
    }
}
