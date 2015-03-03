package com.topface.topface.banners;


import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

/**
 * Created by kirussell on 11/01/15.
 * Interface for object instance (usually fragment) that can contain ad's banner
 * Page has to provide:
 * - name to find banner type in {@link com.topface.topface.data.Options#getPagesInfo()}
 * - container for banner's View
 * - reference to Activity to start other activities on banner click in some cases
 */
public interface IPageWithAds {

    /**
     * Name to find banner type in {@link com.topface.topface.data.Options#getPagesInfo()}
     *
     * @return page name
     */
    PageInfo.PageName getPageName();

    /**
     * Activity to start other activities on banner click in some cases
     *
     * @return reference to activity
     */
    FragmentActivity getActivity();

    /**
     * Container for banner's View
     *
     * @return container
     */
    ViewGroup getContainerForAd();

    /**
     * Determine whether container is still available for banner injection or not
     *
     * @return true if can inject banner to page
     */
    boolean isAdded();
}
