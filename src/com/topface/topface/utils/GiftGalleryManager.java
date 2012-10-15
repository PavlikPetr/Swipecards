package com.topface.topface.utils;

import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.views.ImageViewRemote;

import java.util.LinkedList;

public class GiftGalleryManager<T extends Gift> implements OnScrollListener {
    private LinkedList<T> mDataList;
    private Handler mHandler;

    //---------------------------------------------------------------------------
    public GiftGalleryManager(LinkedList<T> dataList, Handler handler) {
        mHandler = handler;
        mDataList = dataList;
        if (mDataList == null) {
            mDataList = new LinkedList<T>();
        }
    }

    public T get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    public void getImage(final int position, final ImageViewRemote imageView) {
        imageView.setRemoteSrc(mDataList.get(position).link);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    //---------------------------------------------------------------------------
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            if (mHandler != null && !mDataList.isEmpty() && mDataList.getLast().isLoader()) {
            	mHandler.sendEmptyMessage(0);
            }
        }
    }

}




