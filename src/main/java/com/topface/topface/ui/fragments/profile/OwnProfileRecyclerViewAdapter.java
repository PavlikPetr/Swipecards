package com.topface.topface.ui.fragments.profile;

import android.widget.FrameLayout;

import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.databinding.ItemUserGalleryBinding;
import com.topface.topface.ui.adapters.BasePhotoRecyclerViewAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;


public class OwnProfileRecyclerViewAdapter extends BasePhotoRecyclerViewAdapter<ItemUserGalleryBinding> {


    public OwnProfileRecyclerViewAdapter(Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(photoLinks, totalPhotos, callback);
    }

    @Override
    protected boolean isAddPhotoButtonEnabled() {
        return true;
    }

    @Override
    protected Class<ItemUserGalleryBinding> getItemBindingClass() {
        return ItemUserGalleryBinding.class;
    }

    @Override
    protected void setHandlers(ItemUserGalleryBinding binding, int position) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getItemWidth(), getItemWidth());
        binding.root.setLayoutParams(lp);
        binding.ivPhoto.setLayoutParams(lp);
        binding.ivPhoto.setPhoto(getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_user_gallery;
    }

}
