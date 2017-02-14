package com.topface.topface.ui.views.image_switcher

import android.os.Bundle
import android.view.View
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.AlbumImageBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos
import com.topface.topface.utils.Utils
import java.util.*

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
    private var isFirstBind = true

    override fun bindData(binding: AlbumImageBinding?, position: Int) {
        binding?.let {
            val target = mRequest.load(getDataItem(position))
                    .listener(object : RequestListener<String, GlideDrawable> {
                        override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
                                                 isFirstResource: Boolean): Boolean {
                            if (model.isNullOrEmpty()) {
                                return true

                            } else {
                                it.pgrsAlbum.visibility = View.GONE
                                return false
                            }
                        }

                        override fun onResourceReady(resource: GlideDrawable?, model: String?,
                                                     target: Target<GlideDrawable>?, isFromMemoryCache: Boolean,
                                                     isFirstResource: Boolean): Boolean {
                            it.pgrsAlbum.visibility = View.GONE
                            return false
                        }
                    })
                    .error(R.drawable.im_photo_error)
                    .into(it.image)
            if (stolenSize == null) {
                target.getSize { width, height -> stolenSize = intArrayOf(width, height) }
            }
        }
    }

    private fun preloadOnStart(position: Int) {
        if (isFirstBind) {
            isFirstBind = false
            for (i: Int in position + 1..position + ImageLoader.PRELOAD_SIZE) {
                data.getOrNull(i)?.let {
                } ?: return
            }
        }
    }

    override fun getItemLayout() = R.layout.album_image

    override fun getUpdaterEmitObject() = Bundle()

    override fun getPreloadItems(position: Int): List<String> {
        Debug.error("$TAG preload position:$position")
        if (data.getOrNull(position).isNullOrEmpty()) {
            mUploadListener?.sendRequest(position)
        }
        return Collections.singletonList(data.getOrElse(position) { Utils.EMPTY })
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