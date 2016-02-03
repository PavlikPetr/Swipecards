package com.topface.topface.ui.adapters;

import android.widget.FrameLayout;

import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.databinding.ItemUserGalleryBinding;


public class UserRecyclerViewAdapter extends BasePhotoRecyclerViewAdapter<ItemUserGalleryBinding> {


    public UserRecyclerViewAdapter(Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(photoLinks, totalPhotos, callback);
    }

    @Override
    protected Class<ItemUserGalleryBinding> getItemBindingClass() {
        return ItemUserGalleryBinding.class;
    }

    @Override
    protected void setHandlers(ItemUserGalleryBinding binding, int position) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getItemWidth(), getItemWidth());
        binding.root.setLayoutParams(params);
        binding.ivPhoto.setLayoutParams(params);
        binding.ivPhoto.setPhoto(getItem(position));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_user_gallery;
    }
}
