package com.topface.topface.ui.views.image_switcher

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.StringSignature
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.AlbumImageBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos

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
    private var mOnImageClickListener: OnClickListener? = null

    override fun bindData(binding: AlbumImageBinding?, position: Int) {
        binding?.let {
            val viewModel = AlbumImageViewModel(mOnImageClickListener)
            it.viewModel = viewModel
            val link = getDataItem(position)
            val target = mRequest.load(link)
                    .signature(StringSignature(link))
                    .listener(object : RequestListener<String, GlideDrawable> {
                        override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
                                                 isFirstResource: Boolean): Boolean {
                            Debug.error("$TAG =======================onException========================\n$e\nlink:$model\nisFirst:$isFirstResource\n===============================================")
                            if (model.isNullOrEmpty()) {
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
                    .into(it.image)
            if (stolenSize == null) {
                target.getSize { width, height -> stolenSize = intArrayOf(width, height) }
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

    fun setOnClickListener(l: OnClickListener?) {
        mOnImageClickListener = l
    }
}