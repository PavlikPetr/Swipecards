package com.topface.topface.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

public class ProfilePhotoGridAdapter extends ProfileGridAdapter {
    // Data
    protected LayoutInflater mInflater;

    protected static final int T_ADD_BTN = 0;
    protected static final int T_PHOTO = 1;
    protected static final int T_COUNT = T_PHOTO + 1;

    // class ViewHolder
    static class ViewHolder {
        ImageViewRemote photo;
    }

    public ProfilePhotoGridAdapter(Context context, Photos photoLinks) {
        this(context, photoLinks, photoLinks.size(), null);
    }

    public ProfilePhotoGridAdapter(Context context, Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(context, photoLinks, totalPhotos, callback);
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

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_user_gallery, null, false);
            holder = new ViewHolder();
            holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getItemViewType(position) == T_ADD_BTN) {
            holder.photo.setBackgroundResource(R.drawable.profile_add_photo_selector);
        } else {
            holder.photo.setPhoto(getItem(position));
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
