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

public class ProfilePhotoGridAdapter extends PhotoGridAdapter {
    protected static final int T_ADD_BTN = 0;
    protected static final int T_PHOTO = 1;
    protected static final int T_COUNT = T_PHOTO + 1;
    // Data
    protected LayoutInflater mInflater;

    public ProfilePhotoGridAdapter(Context context, Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(photoLinks, totalPhotos, callback);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return T_ADD_BTN;
        } else {
            return T_PHOTO;
        }
    }

    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            if (type == T_ADD_BTN) {
                convertView = mInflater.inflate(R.layout.item_user_gallery_add_btn, null, false);
                fixAddButtonLayoutParams(convertView);
                return convertView;
            } else {
                convertView = mInflater.inflate(R.layout.item_user_gallery, null, false);
                holder = new ViewHolder();
                holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getGridItemWidth(), getGridItemWidth());
                holder.photo.setLayoutParams(lp);
                convertView.setTag(holder);
            }
        } else {
            if (type == T_ADD_BTN) {
                fixAddButtonLayoutParams(convertView);
                return convertView;
            }
            holder = (ViewHolder) convertView.getTag();
        }
        holder.photo.setPhoto(getItem(position));
        return convertView;
    }

    private void fixAddButtonLayoutParams(View convertView) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getGridItemWidth(), getGridItemWidth());
        convertView.findViewById(R.id.ivPhoto).setLayoutParams(lp);
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
