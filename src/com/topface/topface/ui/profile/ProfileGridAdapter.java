package com.topface.topface.ui.profile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileGridAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private final LoadingListAdapter.Updater mUpdater;
    private Photos mPhotoLinks;
    private int mTotalPhotos;
    private boolean needLoadNewItems = false;

    public ProfileGridAdapter(Context context,
                              Photos photoLinks,
                              int totalPhotos,
                              LoadingListAdapter.Updater callback) {
        mPhotoLinks = new Photos();
        mTotalPhotos = totalPhotos;
        mUpdater = callback;
        setData(photoLinks, totalPhotos > photoLinks.size());
    }

    public void setData(Photos photoLinks, boolean needMore) {
        if (mPhotoLinks.size() > 0 && mPhotoLinks.get(mPhotoLinks.size() - 1).getId() == 0) {
            mPhotoLinks.remove(mPhotoLinks.size() - 1);
        }

        for (Photo photo : photoLinks) {
            mPhotoLinks.add(photo);
        }

        if (needMore) {
            mPhotoLinks.add(new Photo());
            needLoadNewItems = true;
        } else {
            needLoadNewItems = false;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPhotoLinks != null ? mPhotoLinks.size() : 0;
    }

    public Photos getData() {
        return mPhotoLinks;
    }

    @Override
    public Photo getItem(int position) {
        return mPhotoLinks.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1 && needLoadNewItems) {
            if (mUpdater != null && !mPhotoLinks.isEmpty()) {
                needLoadNewItems = false;
                mUpdater.onUpdate();
            }
        }
    }
}
