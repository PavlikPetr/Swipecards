package com.topface.topface.ui.profile;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.topface.topface.R;
import com.topface.topface.data.Album;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class UserGridAdapter extends BaseAdapter {
    private LayoutInflater mInflater;    
    private LinkedList<Album> mUserAlbum;
    private ExecutorService mWorker;
    private Bitmap mMask;

    static class ViewHolder {
        ImageView mPhoto;
    };

    public UserGridAdapter(Context context, LinkedList<Album> userAlbum) {
        mInflater = LayoutInflater.from(context);
        mUserAlbum = userAlbum;
        mWorker = Executors.newFixedThreadPool(3);
        mMask = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_mask_album);
    }

    @Override
    public int getCount() {
        return mUserAlbum.size();
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
        
        Album album = mUserAlbum.get(position);
        loader(album.getBigLink(), holder.mPhoto);

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

    private void loader(final String url, final ImageView iv) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }
}
