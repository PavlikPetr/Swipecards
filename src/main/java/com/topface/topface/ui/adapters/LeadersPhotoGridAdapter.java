package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.fragments.profile.ProfileGridAdapter;
import com.topface.topface.ui.views.ImageViewRemote;

/**
 * Photos adapter for photos which you can put into a leaders feed
 */
public class LeadersPhotoGridAdapter extends ProfileGridAdapter {

    private LayoutInflater mInflater;

    private int mSelectedId;

    public LeadersPhotoGridAdapter(Context context, Photos photoLinks) {
        super(photoLinks, photoLinks.size(), null);
        mInflater = LayoutInflater.from(context);
        mSelectedId = photoLinks.size() > 0 ? photoLinks.getFirst().getId() : -1;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Photo item = getItem(position);
        final int itemId = item.getId();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.leaders_photo_item, null, false);
            holder = new ViewHolder();
            holder.mPhoto = (ImageViewRemote) convertView.findViewById(R.id.ivLeadPhoto);
            holder.mCheckMark = (ImageView) convertView.findViewById(R.id.lpiCheckMark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mPhoto.setPhoto(item);
        holder.mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedId == itemId) {
                    mSelectedId = -1;
                } else {
                    mSelectedId = itemId;
                }
                notifyDataSetChanged();
            }
        });

        if (itemId == mSelectedId) {
            holder.mCheckMark.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckMark.setVisibility(View.GONE);
        }

        return convertView;
    }

    public int getSelectedPhotoId() {
        return mSelectedId;
    }

    private static class ViewHolder {
        ImageViewRemote mPhoto;
        ImageView mCheckMark;
    }
}
