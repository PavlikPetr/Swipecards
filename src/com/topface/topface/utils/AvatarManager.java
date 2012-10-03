package com.topface.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.topface.topface.data.AbstractDataWithPhotos;

import java.util.HashMap;
import java.util.LinkedList;

/* Менеджер аватарок, загрузает и кеширует изображения */
public class AvatarManager<T extends AbstractDataWithPhotos> implements AbsListView.OnScrollListener {
    private LinkedList<T> mDataList;
    private HashMap<Integer, Bitmap> mCache;
    private Handler mHandler;

    public AvatarManager(Context context, LinkedList<T> dataList, Handler handler) {
        mDataList = dataList;
        mCache = new HashMap<Integer, Bitmap>();
        mHandler = handler;
    }

    //---------------------------------------------------------------------------
    public void setDataList(LinkedList<T> dataList) {
        clear();
        mDataList = dataList;
    }

    //---------------------------------------------------------------------------
    public T get(int position) {
        return mDataList.get(position);
    }

    //---------------------------------------------------------------------------
    public int size() {
        return mDataList.size();
    }

    //---------------------------------------------------------------------------
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

    public void release() {
        clear();
        mCache = null;
        mDataList = null;
        mHandler = null;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0
                && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            if (mHandler != null)
                mHandler.sendEmptyMessage(0);
        }
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                break;
            case OnScrollListener.SCROLL_STATE_IDLE: {
                view.invalidateViews(); //  ПРАВИЛЬНО ???
                break;
            }

        }
    }
}
