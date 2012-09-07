package com.topface.topface.utils;

import java.util.LinkedList;

import com.topface.topface.data.AbstractData;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.imageloader.DefaultImageLoaderListener;

/*
 *  Менеджер аватарок, загрузает и кеширует изображения
 */
public class AvatarManager<T extends AbstractData> {
    // Data
    private LinkedList<T> mDataList;
    public static final int AVATAR_ROUND_RADIUS = 12;  // хард кор !!!!!!!

    public AvatarManager(LinkedList<T> dataList) {
        mDataList = dataList;
    }

    public T get(int position) {
        return mDataList.get(position);
    }

    public int size() {
        return mDataList.size();
    }

    public void getImage(final int position, final ImageView imageView) {
        DefaultImageLoader.getInstance().displayImage(mDataList.get(position).getSmallLink(), imageView, new DefaultImageLoaderListener(imageView) {
            @Override
            public void onLoadingComplete(Bitmap loadedImage) {
                super.onLoadingComplete(loadedImage);
                // округляем
                loadedImage = Utils.getRoundedCornerBitmap(loadedImage, loadedImage.getWidth(), loadedImage.getHeight(), AVATAR_ROUND_RADIUS);
                imageView.setImageBitmap(loadedImage);
            }
        });
    }
}
