package com.topface.topface.ui.fragments.profile;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.GridViewWithHeaderAndFooter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

public class PhotoGridAdapter extends BaseAdapter
        implements AbsListView.OnScrollListener, GridViewWithHeaderAndFooter.IGridSizes {
    private final LoadingListAdapter.Updater mUpdater;
    private final AlbumLoadController mLoadController;
    private Photos mPhotoLinks;
    private boolean needLoadNewItems = false;
    private int mGridWidth;

    public PhotoGridAdapter(Photos photoLinks,
                            int totalPhotos,
                            LoadingListAdapter.Updater callback) {
        mPhotoLinks = new Photos();
        mUpdater = callback;
        if (photoLinks != null) {
            setData(photoLinks, totalPhotos > photoLinks.size());
        }
        mLoadController = new AlbumLoadController(AlbumLoadController.FOR_GALLERY);
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
        if (isNeedUpadate()) {
            setData((Photos) CacheProfile.photos.clone(), CacheProfile.photos.size() != CacheProfile.totalPhotos, true);
        }
    }

    private boolean isNeedUpadate() {
        return !(getPhotos().equals(CacheProfile.photos));
    }

    public void setData(Photos photoLinks, boolean needMore) {
        setData(photoLinks, needMore, isAddPhotoButtonEnabled());
    }

    public void setData(Photos photoLinks, boolean needMore, boolean isAddPhotoButtonEnabled) {
        mPhotoLinks.clear();
        if (isAddPhotoButtonEnabled && !(!photoLinks.isEmpty() && photoLinks.get(0).isFake())) {
            mPhotoLinks.add(0, new Photo());
        }
        addPhotos(photoLinks, needMore, false);
    }

    protected boolean isAddPhotoButtonEnabled() {
        return false;
    }

    public void addData(Photos photoLinks, boolean needMore) {
        if (mPhotoLinks.size() > 0 && !mPhotoLinks.get(mPhotoLinks.size() - 1).isFake()) {
            if (mPhotoLinks.get(mPhotoLinks.size() - 1).getId() == 0) {
                mPhotoLinks.remove(mPhotoLinks.size() - 1);
            }
        }

        addPhotos(photoLinks, needMore, true);
    }

    public void addPhotos(Photos photoLinks, boolean needMore, boolean isReversed) {
        if (mPhotoLinks.size() > 1 && mPhotoLinks.get(mPhotoLinks.size() - 1).isFake()) {
            mPhotoLinks.remove(mPhotoLinks.size() - 1);
        }

        for (Photo photo : photoLinks) {
            if (isReversed) {
                addFirst(photo);
            } else {
                mPhotoLinks.add(photo);
            }
        }

        needLoadNewItems = needMore;
        notifyDataSetChanged();
    }

    public void removePhoto(Photo photo) {
        if (null != mPhotoLinks) {
            mPhotoLinks.remove(photo);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mPhotoLinks != null ? mPhotoLinks.size() : 0;
    }

    public Photos getAdapterData() {
        return mPhotoLinks;
    }

    public Photos getPhotos() {
        Photos photoLinks = (Photos) mPhotoLinks.clone();
        //Убираем первую фейковую фотографию
        if (photoLinks != null && photoLinks.size() > 0 && photoLinks.get(0).isFake()) {
            photoLinks.remove(0);
        }
        return photoLinks;
    }

    public Photos getPhotoLinks() {
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
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1 - mLoadController.getItemsOffsetByConnectionType() && needLoadNewItems) {
            if (mUpdater != null && !mPhotoLinks.isEmpty()) {
                needLoadNewItems = false;
                mUpdater.onUpdate();
            }
        }
    }

    @Override
    public void setColumnWidth(int width) {
        mGridWidth = width;
    }

    protected int getGridItemWidth() {
        return mGridWidth;
    }
}
