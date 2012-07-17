package com.topface.topface.utils;

import java.util.HashMap;
import java.util.LinkedList;

import com.topface.topface.data.AbstractData;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

/*
 *  Менеджер аватарок, загрузает и кеширует изображения
 */
public class AvatarManager<T extends AbstractData> implements AbsListView.OnScrollListener {
    // Data
    private LinkedList<T> mDataList;
    private HashMap<Integer, Bitmap> mCache;
    private boolean mBusy;
    public int AVATAR_ROUND_RADIUS = 12;  // хард кор !!!!!!!

    public AvatarManager(LinkedList<T> dataList) {
        mDataList = dataList;
        mCache = new HashMap<Integer, Bitmap>();
    }

    public T get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    private void clear() {
        int size = mCache.size();
        for (int i = 0; i < size; ++i) {
            Bitmap bitmap = mCache.get(i);
            if (bitmap != null) {
                bitmap.recycle();
                mCache.put(i, null); // хз
            }
        }
        mCache.clear();
    }

    public void getImage(final int position, final ImageView imageView) {
        int uid = mDataList.get(position).getUid();
        Bitmap bitmap = mCache.get(uid);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            imageView.setImageBitmap(null);
            if (!mBusy)
                loadingImages(position, uid, imageView);
        }
    }

    private void loadingImages(final int position, final int uid, final ImageView imageView) {
        if (!mBusy) {
            SmartBitmapFactory.getInstance().loadBitmapByUrl(
                    mDataList.get(position).getSmallLink(),
                    new SmartBitmapFactory.BitmapHandler() {
                        @Override
                        public void handleBitmap(Bitmap bitmap) {
                            if (bitmap == null)
                                return;

                            // округляем
                            bitmap = Utils.getRoundedCornerBitmap(bitmap, imageView.getWidth(), imageView.getHeight(), AVATAR_ROUND_RADIUS);
                            imageView.setImageBitmap(bitmap);

                            if (mCache != null) {
                                mCache.put(uid, bitmap);
                            }
                        }
                    },
                    Thread.NORM_PRIORITY
            );
        }
    }

    public void release() {
        clear();
        mCache = null;
        mDataList = null;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_IDLE: {
                mBusy = false;
                view.invalidateViews(); //  ПРАВИЛЬНО ???
            }
            break;
        }
    }
}
