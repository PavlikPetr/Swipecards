package com.topface.topface.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;

import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.databinding.PhotoBlogLeadersPhotoItemBinding;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

/**
 * Created by onikitin on 06.11.15.
 */
public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder>
        implements AbsListView.OnScrollListener {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PhotoRecyclerViewAdapter.ViewHolder(DataBindingUtil
                .inflate(inflater, R.layout.photo_blog_leaders_photo_item, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Photo photo = getAdapterData().get(position);
        int itemId = photo.getId();
        holder.binding.ivLeadPhoto.setPhoto(photo);
        holder.binding.lpiCheckMark.setVisibility(itemId == mSelectedPhotoId ? View.VISIBLE : View.GONE);
        holder.binding.checkedPhoto.setVisibility(itemId == mSelectedPhotoId ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mPhotoLinks != null ? mPhotoLinks.size() : 0;
    }

    public Photo getItem(int pos) {
        return getAdapterData().get(pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public PhotoBlogLeadersPhotoItemBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    public static final int EMPTY_SELECTED_ID = -1;

    private LayoutInflater mInflater;

    private int mSelectedPhotoId;

    private void setSelectedItemOnStart(Photos photoLinks) {
        if (photoLinks.size() > 0) {
            setSelectedPhotoId(photoLinks.getFirst().getId());
        } else {
            setSelectedPhotoId(EMPTY_SELECTED_ID);
        }
    }

    public int getSelectedPhotoId() {
        return mSelectedPhotoId;
    }

    public void setSelectedPhotoId(int id) {
        mSelectedPhotoId = id;
        notifyDataSetChanged();
    }

//-----------------------------------------------------------------

    private final LoadingListAdapter.Updater mUpdater;
    private final AlbumLoadController mLoadController;
    private Photos mPhotoLinks;
    private boolean needLoadNewItems = false;

    public PhotoRecyclerViewAdapter(Context context, Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        mPhotoLinks = new Photos();
        mUpdater = callback;
        if (photoLinks != null) {
            setData(photoLinks, totalPhotos > photoLinks.size());
        }
        mLoadController = new AlbumLoadController(AlbumLoadController.FOR_GALLERY);
        mInflater = LayoutInflater.from(context);
        setSelectedItemOnStart(photoLinks);
    }

    public void addFirst(Photo photo) {
        if (mPhotoLinks.size() > 1 && mPhotoLinks.get(mPhotoLinks.size() - 1).getId() == 0) {
            mPhotoLinks.add(2, photo);
        } else {
            mPhotoLinks.add(1, photo);
        }
        notifyDataSetChanged();
    }

    public void updateData(Photos photos, int photosCount) {
        if (isNeedUpadate(photos) && photos != null) {
            setData((Photos) photos.clone(), photos.size() != photosCount, true);
        }
    }

    private boolean isNeedUpadate(Photos photos) {
        return !(getPhotos().equals(photos));
    }

    public void setData(Photos photoLinks, boolean needMore) {
        setData(photoLinks, needMore, isAddPhotoButtonEnabled());
    }

    public void setData(Photos photoLinks, boolean needMore, boolean isAddPhotoButtonEnabled) {
        mPhotoLinks.clear();
        if (isAddPhotoButtonEnabled && !(!photoLinks.isEmpty() && photoLinks.get(0).isFake())) {
            mPhotoLinks.add(0, Photo.createFakePhoto());
        }
        addPhotos(photoLinks, needMore, false);
    }

    protected boolean isAddPhotoButtonEnabled() {
        return false;
    }

    @SuppressWarnings("unused")
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

    protected void setImageViewRemoteAnimation(ImageViewRemote view, int duration) {
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(duration);
        view.setViewDisplayAnimate(animation);
    }

}
