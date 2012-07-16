package com.topface.topface.utils;

import java.util.HashMap;
import java.util.LinkedList;

import com.topface.topface.Data;
import com.topface.topface.data.AbstractData;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryGridManager<T extends AbstractData> implements OnScrollListener {

    // Data
    private LinkedList<T> mDataList;
    // кэш
    private MemoryCache mMemoryCache;
    private StorageCache mStorageCache;
    // размеры фотографии в гриде
    public int mBitmapWidth;
    public int mBitmapHeight;
    // скролинг
    public boolean mBusy;

    public GalleryGridManager(Context context, LinkedList<T> dataList) {
        mDataList = dataList;
        mMemoryCache = new MemoryCache();
        mStorageCache = new StorageCache(context, CacheManager.EXTERNAL_CACHE);

        int columnNumber = Data.GRID_COLUMN;
        mBitmapWidth = Device.getDisplay(context).getWidth() / (columnNumber);
        mBitmapHeight = (int) (mBitmapWidth * 1.25);
    }

    public void update() {
        mMemoryCache.clear();
    }

    public AbstractData get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    public void getImage(final int position, final ImageView imageView) {
        Bitmap bitmap = mMemoryCache.get(position);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            imageView.setImageBitmap(null); // хз ??
            if (!mBusy) {
                bitmap = mStorageCache.load(mDataList.get(position).getSmallLink());
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    mMemoryCache.put(position, bitmap);
                } else
                    loadingImages(position, imageView);
            }
        }
    }

    private void loadingImages(final int position, final ImageView imageView) {
        if (mBusy) return;

        final String smallLink = mDataList.get(position).getSmallLink();
        SmartBitmapFactory.getInstance().loadBitmapByUrl(
                smallLink,
                new SmartBitmapFactory.BitmapHandler() {
                    @Override
                    public void handleBitmap(Bitmap bitmap) {
                        if (bitmap == null) return;

                        // вырезаем
                        bitmap = Utils.clipping(bitmap, mBitmapWidth, mBitmapHeight);

                        // отображаем
                        imageView.setImageBitmap(bitmap);

                        // заливаем в кеш
                        if (mMemoryCache != null && mStorageCache != null) {
                            mMemoryCache.put(position, bitmap);
                            mStorageCache.save(smallLink, bitmap);
                        }
                    }
                }
        );
    }

    public void release() {
        mMemoryCache.clear();
        mMemoryCache = null;
        mStorageCache = null;
        if (mDataList != null)
            mDataList.clear();
        mDataList = null;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_IDLE:
                mBusy = false;
                view.invalidateViews(); //  ПРАВИЛЬНО ???
                break;
        }
    }

}




