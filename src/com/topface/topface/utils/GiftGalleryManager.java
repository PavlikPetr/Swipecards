package com.topface.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.AbstractData;
import com.topface.topface.data.Gift;
import com.topface.topface.utils.http.Http;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * //TODO: АААА! ОНО В БИТОЙ КОДИРОВКИ! ЗБС! Удалить это РњРµРЅРµРґР
 *  РњРµРЅРµРґР¶РµСЂ РёР·РѕР±СЂР°Р¶РµРЅРёР№, Р·Р°РіСЂСѓР·Р°РµС‚ Рё РєРµС€РёСЂСѓРµС‚ РёР·РѕР±СЂР°Р¶РµРЅРёСЏ
 */
public class GiftGalleryManager<T extends Gift> implements OnScrollListener {
    // Data
    private LinkedList<T> mDataList;
    private ExecutorService mWorker;
    // РєСЌС€
    private MemoryCache mMemoryCache;
    private StorageCache mStorageCache;
    // СЂР°Р·РјРµСЂС‹ С„РѕС‚РѕРіСЂР°С„РёРё РІ РіСЂРёРґРµ
    public int mBitmapWidth;
    public int mBitmapHeight;
    // СЃРєСЂРѕР»РёРЅРі
    public boolean mBusy = false;
    private Handler mHandler;

    //---------------------------------------------------------------------------
    public GiftGalleryManager(Context context, LinkedList<T> dataList, Handler handler) {
        mHandler = handler;
        mDataList = dataList;
        if (mDataList == null)
            mDataList = new LinkedList<T>();
        mMemoryCache = new MemoryCache();
        mStorageCache = new StorageCache(context, StorageCache.INTERNAL_FILES);
        mWorker = Executors.newFixedThreadPool(3);

//        int orientation = context.getResources().getConfiguration().orientation;
//        int columnNumber = orientation == Configuration.ORIENTATION_PORTRAIT ? GiftsActivity.GIFTS_COLUMN_PORTRAIT : GiftsActivity.GIFTS_COLUMN_LANDSCAPE;
//        int columnNumber = GiftsActivity.GIFTS_COLUMN_PORTRAIT;

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.gift_frame);

        mBitmapWidth = bmp.getWidth() - 2;
        mBitmapHeight = bmp.getWidth() - 2;
        bmp.recycle();
        bmp = null;
    }

    //---------------------------------------------------------------------------
    public void update() {
        mMemoryCache.clear();
    }

    //---------------------------------------------------------------------------
    public AbstractData get(int position) {
        return mDataList.get(position);
    }

    //---------------------------------------------------------------------------
    public int size() {
        return mDataList.size();
    }

    //---------------------------------------------------------------------------
    public void getImage(final int position, final ImageView imageView) {
        Bitmap bitmap = mMemoryCache.get(position);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            imageView.setImageBitmap(null); // С…Р· ??
            if (!mBusy) {
                loadingImages(position, imageView);
            }
        }
        bitmap = null;
    }

    //---------------------------------------------------------------------------
    private void loadingImages(final int position, final ImageView imageView) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBusy)
                        return;
                    Bitmap rawBitmap = mStorageCache.load(((Gift) mDataList.get(position)).id);
                    Bitmap roundBitmap = null;
                    if (rawBitmap != null) {
                        roundBitmap = Utils.getScaleAndRoundBitmapOut(rawBitmap, mBitmapWidth, mBitmapWidth, 1.2f);
                    } else {
                        // РєР°С‡Р°РµРј
                        rawBitmap = Http.bitmapLoader(mDataList.get(position).getLargeLink()); // getBigLink() РѕРґРЅРѕ Рё С‚РѕР¶Рµ РІ Tops

                        if (rawBitmap == null)
                            return;

                        // РІС‹СЂРµР·Р°РµРј
                        roundBitmap = Utils.getScaleAndRoundBitmapOut(rawBitmap, mBitmapWidth, mBitmapWidth, 1.2f);

                        // Р·Р°Р»РёРІР°РµРј РІ РєРµС€
                        mStorageCache.save(Integer.toString(((Gift) mDataList.get(position)).id), rawBitmap, false);

                    }
                    imagePost(imageView, roundBitmap);
                    mMemoryCache.put(position, roundBitmap);
                    roundBitmap = null;
                    rawBitmap = null;
                } catch (Exception e) {
                    Debug.log(this, "thread error:" + e);
                }
            }
        });
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
        mWorker.shutdown();
        mWorker = null;
        mMemoryCache.clear();
        mMemoryCache = null;
        mStorageCache = null;
        if (mDataList != null)
            mDataList.clear();
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
                view.invalidateViews(); //  РџР РђР’Р�Р›Р¬РќРћ ???
                break;
        }
    }
    //---------------------------------------------------------------------------
}




