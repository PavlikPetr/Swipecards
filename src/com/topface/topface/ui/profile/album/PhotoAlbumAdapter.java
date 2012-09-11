package com.topface.topface.ui.profile.album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.Album;
import com.topface.topface.imageloader.FullSizeImageLoader;
import com.topface.topface.utils.Settings;

import java.util.LinkedList;

public class PhotoAlbumAdapter extends BaseAdapter {


    // class ViewHolder
    
    static class ViewHolder {
        ImageView mImageView;
    }

    
    // Data
    private int mPrevPosition;         // предыдущая позиция фото в альбоме
    private int mPreRunning;           // текущая пред загружаемое фото
    private LinkedList<Album> mAlbumsList;
    private LayoutInflater mInflater;

    
    public PhotoAlbumAdapter(Context context, LinkedList<Album> albumList) {
        mAlbumsList = albumList;
        mInflater = LayoutInflater.from(context);
    }

    
    public int getCount() {
        return mAlbumsList.size();
    }

    
    public Object getItem(int position) {
        return mAlbumsList.get(position);
    }

    
    public long getItemId(int position) {
        return position;
    }

    
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_album_gallery, null, false);
            holder.mImageView = (ImageView) convertView.findViewById(R.id.ivPreView);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        FullSizeImageLoader.getInstance().displayImage(mAlbumsList.get(position).getBigLink(), holder.mImageView);

        int prePosition = position >= mPrevPosition ? position + 1 : position - 1;
        if (prePosition > 0 && position < (getCount() - 1))
            preLoading(prePosition);

        mPrevPosition = position;

        return convertView;
    }

    public void preLoading(final int position) {
        if (position == mPreRunning || !Settings.getInstance().isPreloadPhoto()) {
            return;
        }

        FullSizeImageLoader.getInstance().preloadImage(mAlbumsList.get(position).getBigLink());

        mPreRunning = position;
    }


}
