package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

    private int mPhotoWidth;
    private int mSelectedPhotoId;

    public LeadersPhotoGridAdapter(Context context, Photos photoLinks) {
        super(photoLinks, photoLinks.size(), null);
        mInflater = LayoutInflater.from(context);
        setSelectedItemOnStart(photoLinks);
    }

    private void setSelectedItemOnStart(Photos photoLinks) {
        if (photoLinks.size() > 0) {
            setSelectedPhotoId(photoLinks.getFirst().getId());
        } else {
            setSelectedPhotoId(-1);
        }
    }

    public LeadersPhotoGridAdapter(Context context, Photos photoLinks, int photoLinksSize, int photoWidth, LoadingListAdapter.Updater callback) {
        super(photoLinks, photoLinksSize, callback);
        mInflater = LayoutInflater.from(context);
        mPhotoWidth = photoWidth;
        setSelectedItemOnStart(photoLinks);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Photo item = getItem(position);
        final int itemId = item.getId();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.photo_blog_leaders_photo_item, null, false);
            holder = new ViewHolder();
            holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivLeadPhoto);
            holder.checkMark = (ImageView) convertView.findViewById(R.id.lpiCheckMark);
            holder.checkLayout = (ImageView) convertView.findViewById(R.id.checkedPhoto);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mPhotoWidth, mPhotoWidth);
            holder.photo.setLayoutParams(lp);
            holder.checkLayout.setLayoutParams(lp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.photo.setPhoto(item);
        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPhotoId != itemId) {
                    mSelectedPhotoId = itemId;
                    notifyDataSetChanged();
                }
            }
        });

        holder.checkMark.setVisibility(itemId == mSelectedPhotoId ? View.VISIBLE : View.GONE);
        holder.checkLayout.setVisibility(itemId == mSelectedPhotoId ? View.VISIBLE : View.GONE);

        return convertView;
    }

    public int getSelectedPhotoId() {
        return mSelectedPhotoId;
    }

    public void setSelectedPhotoId(int id) {
        mSelectedPhotoId = id;
        notifyDataSetChanged();
    }


    private static class ViewHolder {
        ImageViewRemote photo;
        ImageView checkMark;
        ImageView checkLayout;
    }

}
