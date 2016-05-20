package com.topface.topface.data.leftMenu;

import com.topface.framework.imageloader.IPhoto;
import com.topface.topface.data.HeaderFooterData;

/**
 * Created by ppavlik on 12.05.16.
 * Left menu header data class. It contains custom view clickListener interface
 */
public class LeftMenuHeaderData extends HeaderFooterData<LeftMenuHeaderViewData> {

    /**
     * Create new data object for left menu header
     *
     * @param photo    interface for ImageLoader interaction with photos
     * @param name     users name
     * @param city     users city
     * @param listener typed header view clickListener
     */
    public LeftMenuHeaderData(IPhoto photo, String name, String city, OnViewClickListener<LeftMenuHeaderViewData> listener) {
        super(new LeftMenuHeaderViewData(photo, name, city), listener);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
