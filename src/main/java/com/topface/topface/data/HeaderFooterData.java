package com.topface.topface.data;

import android.view.View;

/**
 * Created by ppavlik on 12.05.16.
 * Base data class for recyclerviews headers and footers with own clickListener
 */
public class HeaderFooterData<DataType> {

    private DataType mData;
    private OnViewClickListener<DataType> mListener;

    /**
     * Create new object with generic data and own clickListener
     *
     * @param data     your data object
     * @param listener own clickListener
     */
    public HeaderFooterData(DataType data, OnViewClickListener<DataType> listener) {
        mData = data;
        mListener = listener;
    }

    /**
     * Get header/footer data object
     *
     * @return your data object
     */
    public DataType getData() {
        return mData;
    }

    /**
     * Set header/footer data object
     *
     * @param data your data object
     */
    public void setData(DataType data) {
        mData = data;
    }

    /**
     * Get header/footer view clickListener
     *
     * @return views clickListener
     */
    public OnViewClickListener<DataType> getClickListener() {
        return mListener;
    }

    /**
     * Set header/footer view clickListener
     *
     * @param listener views own clickListener
     */
    public void setClickListener(OnViewClickListener<DataType> listener) {
        mListener = listener;
    }

    /**
     * Typed recyclerViews header/footer view clickListener interface
     *
     * @param <DataType>
     */
    public interface OnViewClickListener<DataType> {

        /**
         * Fire on header/footer view click
         *
         * @param v    clicked view
         * @param data typed data object
         */
        void onClick(View v, DataType data);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HeaderFooterData)) return false;
        HeaderFooterData data = (HeaderFooterData) o;
        if (mData != null ? !mData.equals(data.getData()) : data.getData() != null) return false;
        return mListener != null ? !mListener.equals(data.getClickListener()) : data.getClickListener() != null;
    }

    @Override
    public int hashCode() {
        int result = mData != null ? mData.hashCode() : 0;
        return 31 * result + (mListener != null ? mListener.hashCode() : 0);
    }
}
