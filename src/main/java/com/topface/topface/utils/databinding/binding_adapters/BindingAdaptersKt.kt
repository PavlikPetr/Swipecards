package com.topface.topface.utils.databinding.binding_adapters

import android.databinding.BindingAdapter
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.graphics.Matrix
import android.graphics.PointF
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseAdapter
import com.topface.topface.ui.fragments.feed.enhanced.utils.ImprovedObservableList
import com.topface.topface.ui.views.image_switcher.ImageLoader

/**
 * Binding adapters using Kotlin
 */

@BindingAdapter("autoLinkText")
fun setAutoLinkText(textView: TextView, text: String) {
    textView.text = Html.fromHtml(text.replace("\n", "<br />"))
    // Проверяем наличие в textView WEB_URLS | EMAIL_ADDRESSES | PHONE_NUMBERS | MAP_ADDRESSES;
    // Если нашли, то добавим им кликабельность
    (ComponentManager.componentsMap.get(ChatComponent::class.java) as? ChatComponent)?.let {
        textView.movementMethod = it.customMovementMethod()
    }
    textView.isFocusable = false
}

@BindingAdapter("backgroundColor")
fun setViewBackgroundColor(view: View, backgroundColor: Int) =
        view.setBackgroundColor(0xFF000000.toInt() or backgroundColor)

@BindingAdapter("glideFitCenter")
fun setImageByGlideWithFitCenter(view: ImageView, res: String) =
        Glide.with(view.context.applicationContext).load(res).fitCenter().into(view)

@BindingAdapter(value = *arrayOf("glidePreloadedSrc", "imageCropType"), requireAll = false)
fun setPreloadedGlideImageWithCrop(view: ImageView, resource: GlideDrawable?,
                                   @ImageLoader.Companion.CropType cropType: Long = ImageLoader.CROP_TYPE_NONE) = resource?.let {

    fun getCropTopMatrix(dest: PointF, source: PointF) = Matrix().apply {
        val scale: Float
        var dx = 0f
        if (source.x * dest.y > dest.x * source.y) {
            scale = dest.y / source.y
            dx = (dest.x - source.x * scale) * 0.5f
        } else {
            scale = dest.x / source.x
        }

        setScale(scale, scale)
        postTranslate((dx + 0.5f).toInt().toFloat(), 0f)
    }

    view.post {
        if (cropType == ImageLoader.CROP_TYPE_MATCH_VIEW) {
            view.imageMatrix = getCropTopMatrix(PointF(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
                    , PointF(it.minimumWidth.toFloat(), it.minimumHeight.toFloat()))
            view.scaleType = ImageView.ScaleType.MATRIX
        }
        view.setImageDrawable(it)
    }
}

@BindingAdapter("bindDataToSwipeFlingView")
fun setBindDataToSwipeFlingView(view: SwipeFlingAdapterView, data: ImprovedObservableList<FeedBookmark>) {
    val adapter = (view.adapter as BaseAdapter<*, FeedBookmark>).update(data.observableList)
    data.canAddListener = true
    if (!data.isListenerAdded()) {
        data.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<FeedBookmark>>() {
            override fun onItemRangeMoved(list: ObservableArrayList<FeedBookmark>?, p1: Int, p2: Int, p3: Int) {
                list?.let {
                    adapter.update(it)
                }
            }

            override fun onChanged(list: ObservableArrayList<FeedBookmark>?) {
            }

            override fun onItemRangeInserted(list: ObservableArrayList<FeedBookmark>?, p1: Int, p2: Int) {
                list?.let {
                    adapter.update(it)
                }
            }

            override fun onItemRangeRemoved(list: ObservableArrayList<FeedBookmark>?, p1: Int, p2: Int) {
                list?.let {
                    adapter.update(it)
                }
            }

            override fun onItemRangeChanged(list: ObservableArrayList<FeedBookmark>?, p1: Int, p2: Int) {
                list?.let {
                    adapter.update(it)
                }
            }
        })
    }
}

private fun BaseAdapter<*, FeedBookmark>.update(list: ObservableArrayList<FeedBookmark>) = this.apply {
    data.clear()
    data.addAll(list)
    notifyDataSetChanged()
}

@BindingAdapter(value = *arrayOf("onSwipeFlingViewScroll", "swipeFlingViewBackgroundId", "swipeFlingViewRightIndicatorId", "swipeFlingViewLeftIndicatorId"))
fun onSwipeFlingViewScroll(view: SwipeFlingAdapterView, scrollProgressPercent: Float, backgroundId: Int, rightIndicatorId: Int, leftIndicatorId: Int) {
    view.selectedView?.let {
        it.findViewById(backgroundId)?.alpha = 0f
        it.findViewById(rightIndicatorId)?.alpha = if (scrollProgressPercent < 0) -scrollProgressPercent else 0f
        it.findViewById(leftIndicatorId)?.alpha = if (scrollProgressPercent > 0) scrollProgressPercent else 0f
    }
}