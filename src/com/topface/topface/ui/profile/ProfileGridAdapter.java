package com.topface.topface.ui.profile;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.utils.CacheProfile;

public class ProfileGridAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private final LoadingListAdapter.Updater mUpdater;
    private Photos mPhotoLinks;
    private boolean needLoadNewItems = false;

    public ProfileGridAdapter(Photos photoLinks,
                              int totalPhotos,
                              LoadingListAdapter.Updater callback) {
        mPhotoLinks = new Photos();
        mUpdater = callback;
        if (photoLinks != null) {
            setData(photoLinks, totalPhotos > photoLinks.size());
        }
    }

    public void addFirst(Photo photo) {
        if (mPhotoLinks.size() > 1 && mPhotoLinks.get(mPhotoLinks.size() - 1).getId() == 0) {
            mPhotoLinks.add(2, photo);
        } else {
            mPhotoLinks.add(1, photo);
        }
        notifyDataSetChanged();
    }

    public void updateData() {
        mPhotoLinks.clear();
        mPhotoLinks.add(null);
        for(Photo photo: CacheProfile.photos) {
            mPhotoLinks.add(photo);
        }
        notifyDataSetChanged();
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
