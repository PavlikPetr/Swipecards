package com.topface.topface.ui.profile;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.topface.topface.R;
import com.topface.topface.utils.CacheManager;
import com.topface.topface.utils.MemoryCache;
import com.topface.topface.utils.StorageCache;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class UserPhotoGridAdapter extends BaseAdapter {
    // Data
    private LayoutInflater mInflater;    
    private ExecutorService mWorker;
    private MemoryCache mMemoryCache;
    private StorageCache mStorageCache;
    private SparseArray<HashMap<String, String>> mPhotoLinks;
    private Bitmap mMask;

    // class ViewHolder
    static class ViewHolder {
        ImageView mPhoto;
    };

    public UserPhotoGridAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mWorker = Executors.newFixedThreadPool(3);
        mMemoryCache  = new MemoryCache();
        mStorageCache = new StorageCache(context, CacheManager.EXTERNAL_CACHE);
        mPhotoLinks   = new SparseArray<HashMap<String,String>>();
        mMask = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_mask_album);
    }

    @Override
    public int getCount() {
        return mPhotoLinks.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = (ViewGroup)mInflater.inflate(R.layout.item_user_gallery, null, false);
            holder = new ViewHolder();
            holder.mPhoto = (ImageView)convertView.findViewById(R.id.ivPhoto);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        fetchImage(position, holder.mPhoto);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // что с потоковой безопасностью ?
    private void fetchImage(final int position, final ImageView imageView) {
        Bitmap bitmap = mMemoryCache.get(position);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(null);
            mWorker.execute(new Runnable() {
                @Override
                public void run() {
                    //Album album = mUserAlbum.get(position);
                    HashMap<String, String> photo = mPhotoLinks.get(mPhotoLinks.keyAt(position));
                    final Bitmap bitmap = mStorageCache.load((String)photo.values().toArray()[0]);
                    if (bitmap != null) {
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                        mMemoryCache.put(position, bitmap);
                    } else {
                        downloading(position, (String)photo.values().toArray()[0], imageView);
                    }
            }});
        }
        bitmap = null;
    }
    
    private void downloading(final int position, final String url, final ImageView iv) {
        Bitmap rawBitmap = Http.bitmapLoader(url);
        final Bitmap bitmap = Utils.getRoundedCornerBitmapByMask(rawBitmap, mMask);
        if (bitmap != null) {
          iv.post(new Runnable() {
              @Override
              public void run() {
                  iv.setImageBitmap(bitmap);
              }
          });
        }
        mMemoryCache.put(position, bitmap);
        mStorageCache.save(url, bitmap);
    }
    
    public void setUserData(SparseArray<HashMap<String, String>> photoLinks) {
        mPhotoLinks = photoLinks;
    }
}
