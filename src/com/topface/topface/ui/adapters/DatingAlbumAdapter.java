package com.topface.topface.ui.adapters;

import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.data.Search;
import com.topface.topface.imageloader.DefaultImageLoaderListener;
import com.topface.topface.imageloader.FullSizeImageLoader;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.ui.views.ILocker;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

public class DatingAlbumAdapter extends BaseAdapter {

    /**
     * Параметр для статистики, что бы понять, пользовался ли пользователь галереей, или нет
     */
    public boolean showMoreThanOne = false;
    private Context mContext;

    // class ViewHolder
    static class ViewHolder {
        ProgressBar mProgressBar;
        ImageView mImageView;
    }

    // Data
    private int mPrevPosition;         // предыдущая позиция фото в альбоме
    private int mPreRunning;           // текущая пред загружаемое фото
    private Search mUserData;          // данные пользователя
    private ILocker mLocker;
    private LayoutInflater mInflater;
    private AlphaAnimation mAlphaAnimation;

    public DatingAlbumAdapter(Context context, ILocker locker) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(200L);
        mLocker = locker;
    }

    public void setUserData(Search user) {
        mUserData = user;
        showMoreThanOne = false;
        // очистка
        mPreRunning = 0;
        mPrevPosition = 0;
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

        loadingImage(position, holder.mImageView, holder.mProgressBar);

        int prePosition = position >= mPrevPosition ? position + 1 : position - 1;
        if (prePosition > 0 && position < (getCount() - 1))
            preLoading(prePosition);

        mPrevPosition = position;
        if (position > 0) {
            showMoreThanOne = true;
        }

        return convertView;
    }

    public void loadingImage(final int position, final ImageView view, final ProgressBar progressBar) {
        new FullSizeImageLoader(mContext).displayImage(mUserData.avatars_big[position], view, new DefaultImageLoaderListener(view) {
            @Override
            public void onLoadingComplete(Bitmap bitmap) {
                super.onLoadingComplete(bitmap);

                mLocker.unlockControls();
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                view.setAlpha(255);

                if (position == 0) {
                    view.startAnimation(mAlphaAnimation);
                }
            }
        });
    }

    public void preLoading(final int position) {
        if (position == mPreRunning || ConnectionChangeReceiver.isMobileConnection()) {
            return;
        }

        new FullSizeImageLoader(mContext).preloadImage(
                mUserData.avatars_big[position]
        );
    }

}
