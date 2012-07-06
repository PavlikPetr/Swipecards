package com.topface.topface.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.topface.topface.data.AbstractData;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.utils.CacheManager;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.MemoryCache;
import com.topface.topface.utils.StorageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

/*
 *  РњРµРЅРµРґР¶РµСЂ РёР·РѕР±СЂР°Р¶РµРЅРёР№, Р·Р°РіСЂСѓР·Р°РµС‚ Рё РєРµС€РёСЂСѓРµС‚ РёР·РѕР±СЂР°Р¶РµРЅРёСЏ
 */
public class GiftGalleryManager<T extends AbstractData> implements OnScrollListener {
//    class Queue { // РЅРµ РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ
//        // Data
//        private HashMap<Integer, Bitmap> mQueue = new HashMap<Integer, Bitmap>(20);
//        // Methods
//        public Bitmap get(int key) {
//            return mQueue.get(key);
//        }
//        public void put(int key,Bitmap value) {
//            mQueue.put(key, value);
//        }
//        public void clear() {
//            Debug.log(this, "memory cache clearing");
//            int size = mQueue.size();
//            for (int i = 0; i < size; i++) {
//                Bitmap bitmap = mQueue.get(i);
//                if (bitmap != null) {
//                    bitmap.recycle();
//                    mQueue.put(i, null); // С…Р·
//                }
//            }
//            mQueue.clear();
//        }
//    }
    //---------------------------------------------------------------------------
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
//    public boolean mBusy = false;
    //---------------------------------------------------------------------------
    public GiftGalleryManager(Context context,LinkedList<T> dataList) {
        mDataList = dataList;
        mMemoryCache = new MemoryCache();
        mStorageCache = new StorageCache(context, CacheManager.EXTERNAL_CACHE);
        mWorker = Executors.newFixedThreadPool(3);

        int columnNumber = GiftsActivity.GIFTS_COLUMN;
        mBitmapWidth = Device.getDisplay(context).getWidth() / (columnNumber);        
        mBitmapHeight = (int)(mBitmapWidth);
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
    public void getImage(final int position,final ImageView imageView) {
        Bitmap bitmap = mMemoryCache.get(position);

//        if (bitmap != null) {
//            if (bitmap.isRecycled()) {
//                mMemoryCache.put(position, null);
//            }
//        }
        
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            imageView.setImageBitmap(null); // С…Р· ??
//            if (!mBusy) {
                bitmap = mStorageCache.load(((Gift)mDataList.get(position)).id);
                if (bitmap != null) {
                    bitmap = Utils.getRoundBitmap(bitmap, mBitmapWidth, mBitmapWidth, 1.2f);
                    imageView.setImageBitmap(bitmap);
                    mMemoryCache.put(position, bitmap);
                } else {
                    loadingImages(position, imageView);
                }
//            }
        }
        bitmap = null;
    }
    //---------------------------------------------------------------------------
    private void loadingImages(final int position,final ImageView imageView) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    if (mBusy)
//                        return;

                    // РєР°С‡Р°РµРј            
                    Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getBigLink()); // getBigLink() РѕРґРЅРѕ Рё С‚РѕР¶Рµ РІ Tops 

                    if (rawBitmap == null)
                        return;

                    // РІС‹СЂРµР·Р°РµРј
                    Bitmap roundBitmap = Utils.getRoundBitmap(rawBitmap, mBitmapWidth, mBitmapWidth, 1.2f);//Bitmap.createScaledBitmap(rawBitmap, mBitmapWidth, mBitmapWidth, true);

                    // РѕС‚РѕР±СЂР°Р¶Р°РµРј
                    imagePost(imageView, roundBitmap);

                    // Р·Р°Р»РёРІР°РµРј РІ РєРµС€
                    mMemoryCache.put(position, roundBitmap);
                    mStorageCache.save(Integer.toString(((Gift)mDataList.get(position)).id), rawBitmap, false);

                    roundBitmap = null;
                    //rawBitmap.recycle();
                    rawBitmap = null;

                } catch(Exception e) {
                    Debug.log(this, "thread error:" + e);
                }
            }
        });
    }
    //---------------------------------------------------------------------------
    private void imagePost(final ImageView imageView,final Bitmap bitmap) {
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
    }    
    //---------------------------------------------------------------------------
    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
    }
    //---------------------------------------------------------------------------
    @Override
    public void onScrollStateChanged(AbsListView view,int scrollState) {
//        switch (scrollState) {
//            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//                mBusy = true;
//                break;
//            case OnScrollListener.SCROLL_STATE_FLING:
//                mBusy = true;
//                break;
//            case OnScrollListener.SCROLL_STATE_IDLE:
//                mBusy = false;
//                view.invalidateViews(); //  РџР РђР’Р�Р›Р¬РќРћ ???
//                break;
//        }
//        mBusy = false;
    }
    //---------------------------------------------------------------------------
}




