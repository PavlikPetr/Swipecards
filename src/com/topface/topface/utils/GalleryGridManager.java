package com.topface.topface.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.topface.topface.Data;
import com.topface.topface.data.Top;
import com.topface.topface.ui.views.ImageViewRemote;

import java.util.LinkedList;

/* Менеджер изображений, загрузает и кеширует изображения */
public class GalleryGridManager {

    // Data
    private LinkedList<Top> mDataList;

    // размеры фотографии в гриде
    public int mBitmapWidth;
    public int mBitmapHeight;

    public GalleryGridManager(Context context, LinkedList<Top> dataList) {
        mDataList = dataList;
        int columnNumber = Data.GRID_COLUMN;
        mBitmapWidth = Device.getDisplay(context).getWidth() / (columnNumber);
        mBitmapHeight = (int) (mBitmapWidth * 1.25);
    }

    public Top get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    public void getImage(final int position, final ImageViewRemote imageView) {
        setImageViewSize(imageView);
        imageView.setRemoteSrc(mDataList.get(position).photo);
    }

    protected void setImageViewSize(ImageView imageView) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        if (params.height != mBitmapHeight || params.width != mBitmapWidth) {
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(mBitmapWidth, mBitmapHeight));
        }
    }
}
