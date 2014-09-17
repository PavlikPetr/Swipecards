package com.topface.topface.ui.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONArray;
import org.json.JSONException;

public class UserPhotoFragment extends ProfileInnerFragment {

    private static final String USER_ID = "USER_ID";
    private static final String PHOTOS_COUNT = "PHOTOS_COUNT";
    private static final String PHOTO_LINKS = "PHOTO_LINKS";
    private static final String POSITION = "POSITION";

    private int mUserId;
    private int mPhotosCount;
    private UserPhotoGridAdapter mUserPhotoGridAdapter;
    private TextView mTitle;
    private Photos mPhotoLinks;
    private LoadingListAdapter.Updater mUpdater;
    private GridView mGridAlbum;
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
            Intent intent = PhotoSwitcherActivity.getPhotoSwitcherIntent(
                    position,
                    mUserId,
                    mPhotosCount,
                    (ProfileGridAdapter) mGridAlbum.getAdapter()
            );
            Fragment parentFrag = getParentFragment();
            if (parentFrag != null) {
                parentFrag.startActivity(intent);
            } else {
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpdater = new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (mGridAlbum != null) {
                    Photos data = ((ProfileGridAdapter) mGridAlbum.getAdapter()).getData();
                    AlbumRequest request = new AlbumRequest(getActivity(), mUserId, data.get(data.size() - 2).getPosition() + 1, AlbumRequest.MODE_ALBUM, AlbumLoadController.FOR_GALLERY);
                    request.callback(new DataApiHandler<AlbumPhotos>() {

                        @Override
                        protected void success(AlbumPhotos data, IApiResponse response) {
                            if (mGridAlbum != null) {
                                ((UserPhotoGridAdapter) mGridAlbum.getAdapter()).addPhotos(data, data.more, false);
                            }
                        }

                        @Override
                        protected AlbumPhotos parseResponse(ApiResponse response) {
                            return new AlbumPhotos(response);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    }).exec();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);
        // title
        mTitle = (TextView) root.findViewById(R.id.usedTitle);
        if (mPhotoLinks == null) {
            mTitle.setText(Utils.formatPhotoQuantity(0));
        }
        mTitle.setVisibility(View.VISIBLE);
        // album
        mGridAlbum = (GridView) root.findViewById(R.id.usedGrid);

        int position = 0;
        if (savedInstanceState != null) {
            mUserId = savedInstanceState.getInt(USER_ID, 0);
            mPhotosCount = savedInstanceState.getInt(PHOTOS_COUNT, 0);
            try {
                String linksString = savedInstanceState.getString(PHOTO_LINKS);
                mPhotoLinks = linksString != null ? new Photos(new JSONArray(linksString)) : new Photos();
            } catch (JSONException e) {
                Debug.error(e);
            }
            setPhotos(mPhotoLinks);
            position = savedInstanceState.getInt(POSITION, 0);
        }

        mGridAlbum.setAdapter(mUserPhotoGridAdapter);
        mGridAlbum.setSelection(position);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        if (mUserPhotoGridAdapter != null) {
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
        initTitle(mPhotoLinks);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(USER_ID, mUserId);
        outState.putInt(PHOTOS_COUNT, mPhotosCount);
        try {
            outState.putString(PHOTO_LINKS, mPhotoLinks != null ? mPhotoLinks.toJson().toString() : null);
        } catch (JSONException e) {
            Debug.error(e);
        }
        outState.putInt(POSITION, mGridAlbum != null ? mGridAlbum.getFirstVisiblePosition() : null);
    }

    public void setUserData(User user) {
        mUserId = user.uid;
        mPhotosCount = user.photosCount;
        mPhotoLinks = user.photos;
        if (mGridAlbum != null && mGridAlbum.getAdapter() == null) {
            setPhotos(mPhotoLinks);
            mGridAlbum.setAdapter(mUserPhotoGridAdapter);
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
    }

    private void setPhotos(Photos photos) {
        initTitle(photos);

        if (mUserPhotoGridAdapter == null) {
            mUserPhotoGridAdapter = new UserPhotoGridAdapter(getActivity().getApplicationContext(),
                    photos,
                    mPhotosCount,
                    mUpdater);
        }
    }

    private void initTitle(Photos photos) {
        if (mTitle != null && photos != null) {
            mTitle.setText(Utils.formatPhotoQuantity(mPhotosCount));
        }
    }

    @Override
    public void clearContent() {
        if (mPhotoLinks != null) {
            mPhotoLinks.clear();
        }
        initTitle(mPhotoLinks);
        if (mUserPhotoGridAdapter != null) {
            mUserPhotoGridAdapter.notifyDataSetChanged();
        }
        mGridAlbum = null;
    }
}
