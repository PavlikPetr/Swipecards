package com.topface.topface.ui.views.image_switcher

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AlbumImageBinding
import com.topface.topface.glide.RecyclerViewPreloader
import com.topface.topface.state.EventBus
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.utils.extensions.getDrawable
import com.topface.topface.utils.extensions.loadLinkToSameCache
import javax.inject.Inject

/**
 * RV adapter for album
 * Created by ppavlik on 13.02.17.
 */

class PhotoAlbumAdapter(private val mRequest: DrawableRequestBuilder<String>, private val mPreloader: RecyclerViewPreloader<String>) : BaseRecyclerViewAdapter<AlbumImageBinding, String>(),
        PreloadModelProvider<String>, PreloadSizeProvider<String> {

    companion object {
        const val TAG = "PreloadingAdapter"
    }

    @Inject lateinit var eventBus: EventBus

    private var stolenSize: IntArray? = null
    private var mRecyclerView: RecyclerView? = null
    private var mIsOnlyOneItemBind = false
    private var mIsSecondItemPreloadAvailable = true

    init {
        App.get().inject(this)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun bindData(binding: AlbumImageBinding?, position: Int) {
        mIsOnlyOneItemBind = position != 0
        if (mIsOnlyOneItemBind && mIsSecondItemPreloadAvailable) {
            mPreloader.startPreloadSecondItem()
        }
        binding?.let { bind ->
            val viewModel = AlbumImageViewModel()
            bind.viewModel = viewModel
            getDataItem(position)?.let {
                val target = Glide.with(binding.image.context)
                        .loadLinkToSameCache(it)
                        .listener(object : RequestListener<String, GlideDrawable> {
                            override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
                                                     isFirstResource: Boolean): Boolean {
                                Debug.error("$TAG =======================onException========================\n$e\nlink:$model\nisFirst:$isFirstResource\n===============================================")
                                if (model.isNullOrEmpty()) {
                                    askToPreloadLinks(position)
                                    return true
                                } else {
                                    return false
                                }
                            }

                            override fun onResourceReady(resource: GlideDrawable?, model: String?,
                                                         target: Target<GlideDrawable>?, isFromMemoryCache: Boolean,
                                                         isFirstResource: Boolean): Boolean {
                                Debug.error("$TAG =======================onResourceReady========================\nlink:$model\nisFirst:$isFirstResource\nisFromCache:$isFromMemoryCache\n===============================================")
                                return false
                            }
                        })
                        .into(object : SimpleTarget<GlideDrawable>() {
                            override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
                                resource?.let {
                                    viewModel.isProgressVisible.set(View.GONE)
                                    binding.image.setImageDrawable(it)
                                }
                            }

                            override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                                super.onLoadFailed(e, errorDrawable)
                                viewModel.isProgressVisible.set(View.GONE)
                                binding.image.setImageDrawable(R.drawable.im_photo_error.getDrawable())
                            }

                            override fun onLoadStarted(placeholder: Drawable?) {
                                super.onLoadStarted(placeholder)
                                viewModel.isProgressVisible.set(View.VISIBLE)
                                binding.image.setImageDrawable(null)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                super.onLoadCleared(placeholder)
                                viewModel.isProgressVisible.set(View.VISIBLE)
                                binding.image.setImageDrawable(null)
                            }

                        })
                if (stolenSize == null) {
                    target.getSize { width, height -> stolenSize = intArrayOf(width, height) }
                }
            }
        }
    }

    override fun onViewRecycled(holder: ItemViewHolder?) {
        super.onViewRecycled(holder)
        (holder?.binding as? AlbumImageBinding)?.let {
            Debug.error("$TAG onViewRecycled")
            Glide.clear(it.image)
            it.unbind()
        }
    }

    override fun getItemLayout() = R.layout.album_image

    override fun getUpdaterEmitObject() = Bundle()

    override fun getPreloadItems(position: Int): List<String> {
        Debug.error("$TAG preload position:$position")
        val link = data.getOrNull(position)
        if (link.isNullOrEmpty()) {
            askToPreloadLinks(position)
        }
        // если ссылки на фото нет, то не буудем выполнять предзагрузку
        return if (link.isNullOrEmpty()) listOf<String>() else listOf(link!!)
    }

    override fun getPreloadRequestBuilder(item: String?): GenericRequestBuilder<String, *, *, *> {
        Debug.error("$TAG preload item:$item")
        return mRequest.loadLinkToSameCache(item.orEmpty())
    }

    override fun getPreloadSize(item: String, adapterPosition: Int, perItemPosition: Int) = stolenSize

    private fun askToPreloadLinks(position: Int) = eventBus.setData(PreloadPhoto(position))

    fun setIsSecondImagePreloadAvailable(isAvailable: Boolean) {
        if (mIsOnlyOneItemBind) {
            mPreloader.startPreloadSecondItem()
        }
    }
}