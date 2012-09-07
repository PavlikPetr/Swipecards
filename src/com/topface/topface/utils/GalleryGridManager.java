package com.topface.topface.utils;

import java.util.LinkedList;

import com.topface.topface.Data;
import com.topface.topface.data.AbstractData;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.imageloader.DefaultImageLoaderListener;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryGridManager<T extends AbstractData> {

    // Data
    private LinkedList<T> mDataList;
    // размеры фотографии в гриде
    public int mBitmapWidth;
    public int mBitmapHeight;

    public GalleryGridManager(Context context, LinkedList<T> dataList) {
        mDataList = dataList;
        int columnNumber = Data.GRID_COLUMN;
        mBitmapWidth = Device.getDisplay(context).getWidth() / (columnNumber);
        mBitmapHeight = (int) (mBitmapWidth * 1.25);
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
        DefaultImageLoader.getInstance().displayImage(smallLink, imageView, new DefaultImageLoaderListener(imageView) {
            @Override
            public void onLoadingComplete(Bitmap bitmap) {
                super.onLoadingComplete(bitmap);
                // вырезаем
                bitmap = Utils.clipping(bitmap, mBitmapWidth, mBitmapHeight);
                // отображаем
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public void release() {
        if (mDataList != null)
            mDataList.clear();
        mDataList = null;
    }


}




