package com.topface.topface.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.views.ImageViewRemote;

public class UserPhotoGridAdapter extends BaseAdapter {
    // Data
    private LayoutInflater mInflater;
    private Photos mPhotoLinks;

    // class ViewHolder
    static class ViewHolder {
        ImageViewRemote photo;
    }

    public UserPhotoGridAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mPhotoLinks = new Photos();
    }

    @Override
    public int getCount() {
        //noinspection ConstantConditions
        return mPhotoLinks != null ? mPhotoLinks.size() : null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_user_gallery, null, false);
            holder = new ViewHolder();
            holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.photo.setPhoto(getItem(position));

        return convertView;
    }

    @Override
    public Photo getItem(int position) {
        return mPhotoLinks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setUserData(Photos photos) {
        mPhotoLinks = photos;
        notifyDataSetChanged();
    }
}
