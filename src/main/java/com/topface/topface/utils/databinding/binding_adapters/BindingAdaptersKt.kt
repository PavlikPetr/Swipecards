package com.topface.topface.utils.databinding.binding_adapters

import android.databinding.BindingAdapter
import android.text.Html
import android.text.util.Linkify
import android.widget.TextView
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent

/**
 * Binding adapters using Kotlin
 */

@BindingAdapter("autoLinkText")
fun setAutoLinkText(textView: TextView, text: String) {
    textView.text = Html.fromHtml(text.replace("\n", "<br />"))
    // Проверяем наличие в textView WEB_URLS | EMAIL_ADDRESSES | PHONE_NUMBERS | MAP_ADDRESSES;
    // Если нашли, то добавим им кликабельность
    if (Linkify.addLinks(textView, Linkify.ALL)) {
        textView.movementMethod = ComponentManager.obtainComponent(ChatComponent::class.java).customMovementMethod()
        textView.isFocusable = false
    }

}