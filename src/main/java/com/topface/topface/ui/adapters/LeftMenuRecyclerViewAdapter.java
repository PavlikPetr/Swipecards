package com.topface.topface.ui.adapters;


import android.view.View;
import android.widget.FrameLayout;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.databinding.PhotoBlogLeadersPhotoItemBinding;


public class LeftMenuRecyclerViewAdapter extends BasePhotoRecyclerViewAdapter<PhotoBlogLeadersPhotoItemBinding> {


    public LeftMenuRecyclerViewAdapter(Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        super(photoLinks, totalPhotos, callback);
        setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void itemClick(View view, int itemPosition, Photo photo) {
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
    }
}
