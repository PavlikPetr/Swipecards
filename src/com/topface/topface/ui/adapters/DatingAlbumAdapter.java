package com.topface.topface.ui.adapters;

import com.topface.topface.R;
import com.topface.topface.data.Search;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.utils.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DatingAlbumAdapter extends BaseAdapter {

    // class ViewHolder
    static class ViewHolder {
        ProgressBar mProgressBar;
        ImageView mImageView;
    }

    // Data
    private int mPrevPosition;         // предыдущая позиция фото в альбоме
    private int mPreRunning;           // текущая пред загружаемое фото
    private Bitmap mMainBitmap;        // жесткая ссылка на оцениваемую фотографию
    private MemoryCache mCache;        // кеш фоток
    private Search mUserData;          // данные пользователя
    private ILocker mLocker;
    private LayoutInflater mInflater;
    private AlphaAnimation mAlphaAnimation;

    public DatingAlbumAdapter(Context context, ILocker locker) {
        mInflater = LayoutInflater.from(context);
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(200L);
        mCache = new MemoryCache();
        mLocker = locker;
    }

    public void setUserData(Search user) {
        mUserData = user;
        // очистка
        mPreRunning = 0;
        mPrevPosition = 0;
        if (mMainBitmap != null)
            mMainBitmap.recycle();
        mMainBitmap = null;
        mCache.clear();
    }

    public int getCount() {
        if (mUserData == null)
            return 0;
        return mUserData.avatars_big.length;
    }

    public Object getItem(int position) {
        if (mUserData == null)
            return null;
        return mUserData.avatars_big[position];
    }

    public long getItemId(int position) {
        if (mUserData == null)
            return 0;
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_album_gallery, null, false);
            holder.mImageView = (ImageView) convertView.findViewById(R.id.ivPreView);
            holder.mImageView.setMinimumWidth(50);
            holder.mImageView.setMinimumHeight(50);
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.pgrsAlbum);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        if (mUserData == null)
            return convertView;

        Bitmap bitmap = mCache.get(position);

        if (bitmap != null && position == 0) {
            holder.mProgressBar.setVisibility(View.INVISIBLE);
            holder.mImageView.setImageBitmap(bitmap);
        } else if (bitmap != null)
            holder.mImageView.setImageBitmap(bitmap);
        else
            loadingImage(position, holder.mImageView, holder.mProgressBar);

        int prePosition = position >= mPrevPosition ? position + 1 : position - 1;
        if (prePosition > 0 && position < (getCount() - 1))
            preLoading(prePosition);

        mPrevPosition = position;

        return convertView;
    }

    public void loadingImage(final int position, final ImageView view, final ProgressBar progressBar) {
        SmartBitmapFactory.getInstance().loadBitmapByUrl(
                mUserData.avatars_big[position],
                new SmartBitmapFactory.BitmapHandler() {
                    @Override
                    public void handleBitmap(Bitmap bitmap) {
                        mLocker.unlockControls();
                        if (mCache == null) return;
                        mCache.put(position, bitmap);

                        if (position == 0) {
                            progressBar.setVisibility(View.INVISIBLE);
                            view.setAlpha(255);
                            view.setImageBitmap(bitmap);
                            view.startAnimation(mAlphaAnimation);
                            mMainBitmap = bitmap;
                        } else {
                            view.setImageBitmap(bitmap);
                        }
                    }
                },
                Thread.MAX_PRIORITY
        );
    }

    public void preLoading(final int position) {
        if (position == mPreRunning)
            return;

        if (mCache.containsKey(position))
            return;

        mPreRunning = position;

        SmartBitmapFactory.getInstance().loadBitmapByUrl(
                mUserData.avatars_big[position],
                new SmartBitmapFactory.BitmapHandler() {
                    @Override
                    public void handleBitmap(Bitmap bitmap) {
                        if (bitmap == null || mCache == null) return;
                        mCache.put(position, bitmap);
                    }
                }
        );
    }

    public void release() {
        if (mMainBitmap != null)
            mMainBitmap.recycle();
        mMainBitmap = null;
        mCache.clear();
        mCache = null;
    }

}
