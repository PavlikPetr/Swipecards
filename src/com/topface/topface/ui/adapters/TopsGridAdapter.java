package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.topface.topface.R;
import com.topface.topface.data.Top;
import com.topface.topface.ui.views.ThumbView;
import com.topface.topface.utils.GalleryGridManager;

public class TopsGridAdapter extends BaseAdapter {
    //---------------------------------------------------------------------------
    // class ViewHolder
    //---------------------------------------------------------------------------
    static class ViewHolder {
        ThumbView mThumbView;
    }

    ;
    //---------------------------------------------------------------------------
    // Data
    private LayoutInflater mInflater;
    private GalleryGridManager<Top> mGalleryManager;

    //---------------------------------------------------------------------------
    public TopsGridAdapter(Context context, GalleryGridManager<Top> galleryManager) {
        mInflater = LayoutInflater.from(context);
        mGalleryManager = galleryManager;
    }

    //---------------------------------------------------------------------------
    @Override
    public int getCount() {
        return mGalleryManager.size();
    }

    //---------------------------------------------------------------------------
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = (ViewGroup) mInflater.inflate(R.layout.item_grid_gallery, null, false);
            holder = new ViewHolder();
            holder.mThumbView = (ThumbView) convertView.findViewById(R.id.ivTG);
            holder.mThumbView.setMinimumWidth(mGalleryManager.mBitmapWidth);
            holder.mThumbView.setMinimumHeight(mGalleryManager.mBitmapHeight);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.mThumbView.mPercent = ((Top) mGalleryManager.get(position)).liked;

        mGalleryManager.getImage(position, holder.mThumbView);

        return convertView;
    }

    //---------------------------------------------------------------------------
    @Override
    public Object getItem(int position) {
        return null;
    }

    //---------------------------------------------------------------------------
    @Override
    public long getItemId(int position) {
        return position;
    }
    //---------------------------------------------------------------------------
}
