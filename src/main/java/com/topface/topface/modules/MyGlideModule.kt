package com.topface.topface.modules

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.module.GlideModule



/**
 * Created by petrp on 14.02.2017.
 */
internal class MyGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Apply options to the builder here.
    }

    override fun registerComponents(context: Context, glide: Glide) {
        // register ModelLoaders here.
    }
}