package com.topface.topface.utils;

import android.content.Context;
import android.widget.ImageView;
import com.topface.topface.Data;
import com.topface.topface.data.AbstractData;
import com.topface.topface.imageloader.ClipPostProcessor;
import com.topface.topface.imageloader.DefaultImageLoader;

import java.util.LinkedList;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryGridManager<T extends AbstractData> {

    // Data
    private LinkedList<T> mDataList;
    // размеры фотографии в гриде
    public int mBitmapWidth;
    public int mBitmapHeight;
    private ClipPostProcessor mPostProcessor;

    public GalleryGridManager(Context context, LinkedList<T> dataList) {
        mDataList = dataList;
        //Процессор для кропа изображений под нужный размер после загрузки
        mPostProcessor = new ClipPostProcessor(
                Device.getDisplay(context).getWidth() / Data.GRID_COLUMN,
                (int) (mBitmapWidth * 1.25)
        );
    }

    public void update() {
    }

    public AbstractData get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    public void getImage(final int position, final ImageView imageView) {
        final String smallLink = mDataList.get(position).getSmallLink();
        DefaultImageLoader.getInstance().displayImage(smallLink, imageView, mPostProcessor);
    }

    public void release() {
        if (mDataList != null)
            mDataList.clear();
        mDataList = null;
    }


}




