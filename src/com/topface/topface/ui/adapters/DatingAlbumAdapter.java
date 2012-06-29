package com.topface.topface.ui.adapters;

import com.topface.topface.R;
import com.topface.topface.data.Search;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.MemoryCache;
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
    //---------------------------------------------------------------------------
    // class ViewHolder
    //---------------------------------------------------------------------------
    static class ViewHolder {
        ProgressBar mProgressBar;
        ImageView mImageView;
    };
    //---------------------------------------------------------------------------
    // Data
    private int mPrevPosition; // предыдущая позиция фото в альбоме
    private int mPreRunning; // текущая пред загружаемое фото
    private Bitmap mMainBitmap; // жесткая ссылка на оцениваемую фотографию
    private MemoryCache mCache; // кеш фоток
    private Search mUserData; // данные пользователя
    private ILocker mLocker;
    private LayoutInflater mInflater;
    private AlphaAnimation mAlphaAnimation;
    //---------------------------------------------------------------------------
    public DatingAlbumAdapter(Context context,ILocker locker) {
        mInflater = LayoutInflater.from(context);
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(200L);
        mCache = new MemoryCache();
        mLocker = locker;
    }
    //---------------------------------------------------------------------------
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
    //---------------------------------------------------------------------------
    public int getCount() {
        if (mUserData == null)
            return 0;
        return mUserData.avatars_big.length;
    }
    //---------------------------------------------------------------------------
    public Object getItem(int position) {
        if (mUserData == null)
            return null;
        return mUserData.avatars_big[position];
    }
    //---------------------------------------------------------------------------
    public long getItemId(int position) {
        if (mUserData == null)
            return 0;
        return position;
    }
    //---------------------------------------------------------------------------
    public View getView(final int position,View convertView,ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = (ViewGroup)mInflater.inflate(R.layout.item_album_gallery, null, false);
            holder.mImageView = (ImageView)convertView.findViewById(R.id.ivPreView);
            holder.mImageView.setMinimumWidth(50);
            holder.mImageView.setMinimumHeight(50);
            holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.pgrsAlbum);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();

        if (mUserData == null)
            return convertView;

        Bitmap bitmap = mCache.get(position);

        if (bitmap != null && position == 0) {
            holder.mProgressBar.setVisibility(View.INVISIBLE);
            holder.mImageView.setImageBitmap(bitmap);
        } else if (bitmap != null && position != 0)
            holder.mImageView.setImageBitmap(bitmap);
        else
            loadingImage(position, holder.mImageView, holder.mProgressBar);

        int prePosition = position >= mPrevPosition ? position + 1 : position - 1;
        if (prePosition > 0 && position < (getCount() - 1))
            preLoading(prePosition);

        // кнопка back
        //if(position>2 && position==getCount()-1)
        //mDatingControl.controlVisibility(DatingControl.V_SHOW_BACK);

        mPrevPosition = position;

        return convertView;
    }
    //---------------------------------------------------------------------------
    public void loadingImage(final int position,final ImageView view,final ProgressBar progressBar) {
        Thread t = new Thread() {
            @Override
            public void run() {

                //if(view.getWidth()==0)
                //return;

                final Bitmap rawBitmap = Http.bitmapLoader(mUserData.avatars_big[position]);

                //if(rawBitmap!=null && position==0)
                //rawBitmap = Utils.clipping(rawBitmap,view.getWidth(),view.getHeight());

                //final Bitmap bitmap = rawBitmap;

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        mLocker.unlockControls();

                        if (rawBitmap == null) {
                            view.setImageResource(R.drawable.im_photo_error);
                            return;
                        }

                        if (mCache == null)
                            return;

                        mCache.put(position, rawBitmap);

                        if (position == 0) {
                            progressBar.setVisibility(View.INVISIBLE);
                            view.setAlpha(255);
                            view.setImageBitmap(rawBitmap);
                            view.startAnimation(mAlphaAnimation);
                            mMainBitmap = rawBitmap;
                        } else
                            view.setImageBitmap(rawBitmap);

                    }
                }); // view.post

                //rawBitmap = null;

            }
        };
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }
    //---------------------------------------------------------------------------
    public void preLoading(final int position) {
        if (position == mPreRunning)
            return;

        if (mCache.containsKey(position))
            return;

        Debug.log(this, "preloader:" + mPrevPosition + ":" + position);

        Thread t = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Http.bitmapLoader(mUserData.avatars_big[position]);
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
    //---------------------------------------------------------------------------
    public void release() {
        if (mMainBitmap != null)
            mMainBitmap.recycle();
        mMainBitmap = null;
        mCache.clear();
        mCache = null;
    }
    //---------------------------------------------------------------------------
}
