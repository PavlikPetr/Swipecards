package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.LeadersActivity;
import com.topface.topface.ui.views.ImageViewRemote;

public class LeadersPhotoAdapter extends BaseAdapter {
    private LeadersActivity.PhotoSelector mPhotoSelector;
    // class ViewHolder

    static class ViewHolder {
        ImageViewRemote imageView;
        ImageView checkbox;
    }

    private Photos mAlbumsList;
    private LayoutInflater mInflater;


    public LeadersPhotoAdapter(Context context, Photos albumList, LeadersActivity.PhotoSelector selector) {
        mAlbumsList = albumList;
        mInflater = LayoutInflater.from(context);
        mAlbumsList.addFirst(null);
        mPhotoSelector = selector;
    }

    @Override
    public int getCount() {
        return mAlbumsList.size();
    }


    @Override
    public Photo getItem(int position) {
        return mAlbumsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.leaders_album_item, parent, false);
            holder.imageView = (ImageViewRemote) convertView.findViewById(R.id.leaderAlbumPhoto);
            holder.checkbox = (ImageView) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0 && getItem(position) == null) {
            holder.imageView.setImageResource(R.drawable.btn_leaders_add_photo);
            holder.checkbox.setVisibility(View.GONE);
        } else {
            holder.checkbox.setVisibility(
                    mPhotoSelector.getItemId() == position ?
                            View.VISIBLE :
                            View.GONE
            );
            holder.imageView.setPhoto(getItem(position));
        }

        return convertView;
    }

}
