package com.topface.topface.ui.adapters;


import android.view.View;
import android.widget.FrameLayout;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.databinding.PhotoBlogLeadersPhotoItemBinding;


public class LeadersRecyclerViewAdapter extends BasePhotoRecyclerViewAdapter<PhotoBlogLeadersPhotoItemBinding> {

    public static final int EMPTY_SELECTED_ID = -1;
    private int mSelectedPhotoId = 1;
    private int mSelectedPhotoPos = 1;

    public LeadersRecyclerViewAdapter(Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(photoLinks, totalPhotos, callback);
        setSelectedItemOnStart(photoLinks);
        setOnItemClickListener(new onRecyclerViewItemClickListener() {
            @Override
            public void itemClick(View view, int itemPosition, Photo photo) {
                notifyItemChanged(mSelectedPhotoPos);
                mSelectedPhotoPos = itemPosition;
                mSelectedPhotoId = photo.getId();
                ((FrameLayout) view).setForeground(App.getContext().getResources().getDrawable(R.drawable.leader_photo_selection));
            }
        });
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.photo_blog_leaders_photo_item;
    }

    @Override
    protected Class<PhotoBlogLeadersPhotoItemBinding> getItemBindingClass() {
        return PhotoBlogLeadersPhotoItemBinding.class;
    }

    @Override
    protected void setHandlers(PhotoBlogLeadersPhotoItemBinding binding, int position) {
        final Photo photo = getAdapterData().get(position);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getItemWidth(), getItemWidth());
        binding.getRoot().setLayoutParams(params);
        binding.ivLeadPhoto.setLayoutParams(params);
        binding.ivLeadPhoto.setPhoto(photo);
        if (position == mSelectedPhotoPos) {
            ((FrameLayout) binding.getRoot()).setForeground(App.getContext().getResources().getDrawable(R.drawable.leader_photo_selection));
        } else {
            ((FrameLayout) binding.getRoot()).setForeground(null);
        }
    }

    private void setSelectedItemOnStart(Photos photoLinks) {
        if (photoLinks.size() > 0) {
            setSelectedPhotoId(photoLinks.getFirst().getId());
        }
    }

    public int getSelectedPhotoId() {
        return mSelectedPhotoId;
    }

    public void setSelectedPhotoId(int id) {
        mSelectedPhotoId = id;
        notifyDataSetChanged();
    }
}
