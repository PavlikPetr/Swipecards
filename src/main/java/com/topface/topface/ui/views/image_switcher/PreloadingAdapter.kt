package com.topface.topface.ui.views.image_switcher

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.AlbumImageBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos
import com.topface.topface.utils.extensions.clear
import com.topface.topface.utils.extensions.loadLinkToSameCache

/**
 * RV adapter for album
 * Created by ppavlik on 13.02.17.
 */

class PreloadingAdapter(private val mRequest: DrawableRequestBuilder<String>) : BaseRecyclerViewAdapter<AlbumImageBinding, String>(),
        PreloadModelProvider<String>, PreloadSizeProvider<String> {

    companion object {
        const val TAG = "PreloadingAdapter"
    }

    private var stolenSize: IntArray? = null
    private var mUploadListener: IUploadAlbumPhotos? = null
    private var mTargets = arrayListOf<Target<GlideDrawable>>()

    override fun bindData(binding: AlbumImageBinding?, position: Int) {
        binding?.let { bind ->
            val viewModel = AlbumImageViewModel()
            bind.viewModel = viewModel
            getDataItem(position)?.let {
                val target = mRequest.loadLinkToSameCache(it)
                        .listener(object : RequestListener<String, GlideDrawable> {
                            override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
                                                     isFirstResource: Boolean): Boolean {
                                Debug.error("$TAG =======================onException========================\n$e\nlink:$model\nisFirst:$isFirstResource\n===============================================")
                                if (model.isNullOrEmpty()) {
                                    mUploadListener?.sendRequest(position)
                                    return true
                                } else {
                                    viewModel.isProgressVisible.set(View.GONE)
                                    return false
                                }
                            }

                            override fun onResourceReady(resource: GlideDrawable?, model: String?,
                                                         target: Target<GlideDrawable>?, isFromMemoryCache: Boolean,
                                                         isFirstResource: Boolean): Boolean {
                                Debug.error("$TAG =======================onResourceReady========================\nlink:$model\nisFirst:$isFirstResource\nisFromCache:$isFromMemoryCache\n===============================================")
                                viewModel.isProgressVisible.set(View.GONE)
                                return false
                            }
                        })
                        .error(R.drawable.im_photo_error)
                        .into(bind.image)
//                        .into(object : SimpleTarget<GlideDrawable>() {
//                            override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
//                                bind.image.setImageDrawable(resource)
//                                this.clear()
//                            }
//
//                        })
//                        .into(bind.image)
                if (stolenSize == null) {
                    target.getSize { width, height -> stolenSize = intArrayOf(width, height) }
                }
                mTargets.add(target)
            }
        }
    }

    override fun onViewRecycled(holder: ItemViewHolder?) {
        super.onViewRecycled(holder)

    }

    override fun clearData() {
        super.clearData()
        mTargets.forEach {
            it.clear()
        }
    }

    override fun getItemLayout() = R.layout.album_image

    override fun getUpdaterEmitObject() = Bundle()

    override fun getPreloadItems(position: Int): List<String> {
        Debug.error("$TAG preload position:$position")
        if (data.getOrNull(position).isNullOrEmpty()) {
            mUploadListener?.sendRequest(position)
        }
        val link = data.getOrNull(position)
        // если ссылки на фото нет, то не буудем выполнять предзагрузку
        return if (link.isNullOrEmpty()) listOf<String>() else listOf(link!!)
    }

    override fun getPreloadRequestBuilder(item: String?): GenericRequestBuilder<String, *, *, *> {
        Debug.error("$TAG preload item:$item")
        return mRequest.load(item)
    }

    override fun getPreloadSize(item: String, adapterPosition: Int, perItemPosition: Int) = stolenSize

    fun setUploadListener(listener: IUploadAlbumPhotos) {
        mUploadListener = listener
    }
}