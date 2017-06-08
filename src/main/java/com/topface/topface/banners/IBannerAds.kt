package com.topface.topface.banners

import android.support.v4.app.FragmentActivity
import android.view.ViewGroup

/**
 * Created by ppavlik on 31.05.17.
 * Interface for object instance (usually fragment) that can contain ad's banner
 */
interface IBannerAds{
    /**
     * Container for banner's View

     * @return container
     */

    fun getContainerForAd(): android.view.ViewGroup?

    /**
     * Activity to start other activities on banner click in some cases

     * @return reference to activity
     */
    fun getActivity(): android.support.v4.app.FragmentActivity
}