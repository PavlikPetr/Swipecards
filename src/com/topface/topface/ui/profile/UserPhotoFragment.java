package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Utils;

public class UserPhotoFragment extends BaseFragment {
    private User mUser;
    private UserPhotoGridAdapter mUserPhotoGridAdapter;
    private TextView mTitle;
    private Photos mPhotoLinks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserPhotoGridAdapter = new UserPhotoGridAdapter(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);

        GridView gridAlbum = (GridView) root.findViewById(R.id.fragmentGrid);
        gridAlbum.setAdapter(mUserPhotoGridAdapter);
        gridAlbum.setOnItemClickListener(mOnItemClickListener);

        mTitle = (TextView) root.findViewById(R.id.fragmentTitle);

        if (mPhotoLinks != null) {
            setPhotos(mPhotoLinks);
        } else {
            mTitle.setText(Utils.formatPhotoQuantity(0));
        }
        mTitle.setVisibility(View.VISIBLE);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
            Data.photos = mUser.photos;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoSwitcherActivity.class);
            intent.putExtra(PhotoSwitcherActivity.INTENT_USER_ID, mUser.uid);
            intent.putExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, position);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, mUser.photos);
            startActivity(intent);
        }
    };

    public void setUserData(User user) {
        mUser = user;
        mPhotoLinks = user.photos;
        setPhotos(mPhotoLinks);
    }

    private void setPhotos(Photos photos) {
        if (photos != null) {
            mTitle.setText(Utils.formatPhotoQuantity(photos.size()));
        }

        if (mUserPhotoGridAdapter != null) {
            mUserPhotoGridAdapter.setUserData(photos);
        }
    }

    @Override
    public void clearContent() {
        if (mPhotoLinks != null) mPhotoLinks.clear();
        mTitle.setText(Utils.formatPhotoQuantity(0));
        mUserPhotoGridAdapter.notifyDataSetChanged();
    }
}
 