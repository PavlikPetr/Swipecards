package com.topface.topface.utils;

import java.util.HashMap;
import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.AbstractData;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

/* Менеджер аватарок, загрузает и кеширует изображения */
public class AvatarManager<T extends AbstractData> implements AbsListView.OnScrollListener {
    // Data
    private LinkedList<T> mDataList;
    private HashMap<Integer, Bitmap> mCache;
    private int mFrameWidth;
    private boolean mBusy;
    private Handler mHandler;
    //private int mRadius = 12; // хард кор !!!!!!!
    //---------------------------------------------------------------------------
    public AvatarManager(Context context, LinkedList<T> dataList, Handler handler) {
        mDataList = dataList;
        mCache = new HashMap<Integer, Bitmap>();
        mFrameWidth = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.im_avatar_list_frame)).getBitmap().getWidth();
//        mFrameWidth = BitmapFactory.decodeResource(context.getResources(), R.drawable.im_avatar_list_frame).getWidth();
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
    //---------------------------------------------------------------------------
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
    //---------------------------------------------------------------------------
    private void loadingImages(final int position, final int uid, final ImageView imageView) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap rawBitmap = null;
                try {
                    if (mBusy)
                        return;

                    //качаем
                    rawBitmap = Http.bitmapLoader(mDataList.get(position).getSmallLink()); //.getBigLink()
                    if (rawBitmap == null)
                        return;

                    // округляем
                    Bitmap roundBitmap = Utils.getRoundedBitmap(rawBitmap, mFrameWidth, mFrameWidth);  // !!!!!
                    imagePost(imageView, roundBitmap);
                    mCache.put(uid, roundBitmap);

                    roundBitmap = null;
                    if (rawBitmap != null) {
                        rawBitmap.recycle();
                        rawBitmap = null;
                    }
                } catch(Exception e) {
                    Debug.log(this, "thread error:" + e);
                }
            }
        });
        t.start();
    }
    //---------------------------------------------------------------------------
    private void imagePost(final ImageView imageView, final Bitmap bitmap) {
        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }
    //---------------------------------------------------------------------------
    public void release() {
        clear();
        mCache = null;
        mDataList = null;
        mHandler = null;
    }
    //---------------------------------------------------------------------------
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    	if (visibleItemCount != 0
                && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
    		if (mHandler != null)
    			mHandler.sendEmptyMessage(0);
    	}
    }
    //---------------------------------------------------------------------------
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
                break;
            }
                
        }
    }
    //---------------------------------------------------------------------------
}
