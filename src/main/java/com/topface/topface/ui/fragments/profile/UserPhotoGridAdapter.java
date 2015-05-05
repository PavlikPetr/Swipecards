package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.views.ImageViewRemote;

public class UserPhotoGridAdapter extends PhotoGridAdapter {
    // Data
    private LayoutInflater mInflater;

    public UserPhotoGridAdapter(Context context, Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(photoLinks, totalPhotos, callback);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_user_gallery, null, false);
            holder = new ViewHolder();
            holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getGridItemWidth(), getGridItemWidth());
            holder.photo.setLayoutParams(lp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // проверка нужна для исключения краша в адаптере при добавлении
        // GridViewWithHeaderAndFooter header и/или footer
        if (holder != null && holder.photo != null) {
            holder.photo.setPhoto(getItem(position));
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // class ViewHolder
    static class ViewHolder {
        ImageViewRemote photo;
    }
}