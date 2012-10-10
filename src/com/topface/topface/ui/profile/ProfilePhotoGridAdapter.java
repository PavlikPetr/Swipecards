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

public class ProfilePhotoGridAdapter extends BaseAdapter {
    // Data
    protected LayoutInflater mInflater;
    private Photos mPhotoLinks;

    // class ViewHolder
    static class ViewHolder {
        ImageViewRemote photo;
    }

    public ProfilePhotoGridAdapter(Context context, Photos photoLinks) {
        mInflater = LayoutInflater.from(context);
        mPhotoLinks = photoLinks;
    }

    @Override
    public int getCount() {
        return mPhotoLinks.size();
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

        if (position == 0) {
            holder.photo.setBackgroundResource(R.drawable.profile_add_photo_selector);
        } else {
            holder.photo.setPhoto(getItem(position));
        }

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

}
