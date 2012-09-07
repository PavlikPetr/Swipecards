package com.topface.topface.ui.profile.gallery;

import java.util.LinkedList;

import android.widget.ListView;
import com.topface.topface.R;
import com.topface.topface.data.Album;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import com.topface.topface.imageloader.DefaultImageLoader;

public class PhotoGalleryAdapter extends BaseAdapter {
    // Data
    private boolean mOwner;
    private Context mContext;
    private LinkedList<Album> mAlbumList = new LinkedList<Album>();
    //private ExecutorService mThreadsPool;

    public PhotoGalleryAdapter(Context context, boolean bOwner) {
        mContext = context;
        mOwner = bOwner;
    }

    public void setDataList(LinkedList<Album> dataList) {
        mAlbumList = dataList;
    }

    @Override
    public int getCount() {
        return mAlbumList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAlbumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ProfileThumbView(mContext);
            ((ProfileThumbView) convertView).setScaleType(ScaleType.CENTER_CROP);
            //Нужно, иначе будет ImageLoader будет падать
            convertView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }

        if (position == 0 && mOwner) {
            convertView.setPadding(0, 0, 0, 20);
            ((ProfileThumbView) convertView).mIsAddButton = true;
            ((ProfileThumbView) convertView).setScaleType(ScaleType.CENTER_INSIDE);
            ((ProfileThumbView) convertView).setImageResource(R.drawable.profile_add_photo);
            return convertView;
        } else {
            ((ProfileThumbView) convertView).mIsAddButton = false;
        }

        loadingImage(position, ((ProfileThumbView) convertView));

        return convertView;
    }

    private void loadingImage(final int position, final ProfileThumbView view) {
        final Album album = (Album) getItem(position);
        DefaultImageLoader.getInstance().displayImage(album.getSmallLink(), view);
    }

    public void release() {
        mContext = null;
        if (mAlbumList != null)
            mAlbumList.clear();
        mAlbumList = null;
    }

}
