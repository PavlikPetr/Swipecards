package com.topface.topface.utils.databinding.binding_adapters

import android.databinding.BindingAdapter
import android.text.Html
import android.text.util.Linkify
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
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
        (ComponentManager.componentsMap.getOrDefault(ChatComponent::class.java, null) as? ChatComponent)?.let {
            textView.movementMethod = it.customMovementMethod()
        }
        textView.isFocusable = false
    }
}

@BindingAdapter("backgroundColor")
fun setViewBackgroundColor(view: View, backgroundColor: Int) =
        view.setBackgroundColor(0xFF000000.toInt() or backgroundColor)

@BindingAdapter("glideFitCenter")
fun setImageByGlideWithFitCenter(view: ImageView, res: String) =
        Glide.with(view.context.applicationContext).load(res).fitCenter().into(view)

