package com.topface.topface.ui.profile.album;

import java.util.HashMap;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.MemoryCache;
import com.topface.topface.utils.http.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class PhotoAlbumAdapter extends BaseAdapter {
    // Data
    private int mPrevPosition;   // предыдущая позиция фото в альбоме
    private int mPreRunning;     // текущая пред загружаемое фото
    private MemoryCache mCache;  // кеш фоток
    private SparseArray<HashMap<String, String>> mPhotoLinks;
    private LayoutInflater mInflater;
    
    // class ViewHolder
    static class ViewHolder {
        ImageView mImageView;
        ProgressBar mProgressBar;
    };

    public PhotoAlbumAdapter(Context context, SparseArray<HashMap<String, String>> photoLinks) {
        mPhotoLinks = photoLinks;
        mInflater = LayoutInflater.from(context);
        mCache = new MemoryCache();
    }

    public int getCount() {
        return mPhotoLinks.size();
    }

    public Object getItem(int position) {
        return mPhotoLinks.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position,View convertView,ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = (ViewGroup)mInflater.inflate(R.layout.item_album_gallery, null, false);
            holder.mImageView = (ImageView)convertView.findViewById(R.id.ivPreView);
            holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.pgrsAlbum);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();

        Bitmap bitmap = mCache.get(position);
        if (bitmap != null) {
        	holder.mProgressBar.setVisibility(View.INVISIBLE);
            holder.mImageView.setImageBitmap(bitmap);
    	} else {
        	holder.mProgressBar.setVisibility(View.VISIBLE);
            loadingImage(position, holder.mImageView, holder.mProgressBar);
        }

        int prePosition = position >= mPrevPosition ? position + 1 : position - 1;
        if (prePosition > 0 && position < (getCount() - 1))
            preLoading(prePosition);

        mPrevPosition = position;

        return convertView;
    }

    public void loadingImage(final int position,final ImageView view,final ProgressBar progress) {
        Thread t = new Thread() {
            @Override
            public void run() {
                HashMap<String, String> photo = mPhotoLinks.get(mPhotoLinks.keyAt(position));
                final Bitmap bitmap = Http.bitmapLoader((String)photo.values().toArray()[0]);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null) {
                        	progress.setVisibility(View.GONE);
                            view.setImageBitmap(bitmap);
                        } else {
                            view.setImageResource(R.drawable.im_photo_error);
                        }
                    }
                });
                if (bitmap == null || mCache == null)
                    return;
                mCache.put(position, bitmap);
            }
        };
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public void preLoading(final int position) {
        if (position == mPreRunning)
            return;

        if (mCache.containsKey(position))
            return;

        Debug.log(this, "preloader:" + mPrevPosition + ":" + position);

        Thread t = new Thread() {
            @Override
            public void run() {
                HashMap<String, String> photo = mPhotoLinks.get(mPhotoLinks.keyAt(position));
                Bitmap bitmap = Http.bitmapLoader((String)photo.values().toArray()[0]);
                if (bitmap == null || mCache == null)
                    return;
                mCache.put(position, bitmap);
                bitmap = null;
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        mPreRunning = position;
    }

    public void release() {
        mCache.clear();
        mCache = null;
    }
}
