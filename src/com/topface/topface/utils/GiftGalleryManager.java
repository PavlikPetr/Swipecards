package com.topface.topface.utils;

import com.topface.topface.data.FeedGift;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.views.ImageViewRemote;

public class GiftGalleryManager<T extends FeedGift> {
    private FeedList<T> mDataList;

    public GiftGalleryManager(FeedList<T> dataList) {
        mDataList = dataList;
        if (mDataList == null) {
            mDataList = new FeedList<T>();
        }
    }

    public T get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    public void getImage(final int position, final ImageViewRemote imageView) {
        imageView.setRemoteSrc(mDataList.get(position).gift.link);
    }

    public boolean isEmpty() {
        return mDataList.isEmpty();
    }

    public T getLast() {
        return mDataList.getLast();
    }

    public FeedList<T> getData() {
        return mDataList;
    }
}




