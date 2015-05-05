package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.GridViewWithHeaderAndFooter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONException;

public class UserPhotoFragment extends ProfileInnerFragment {

    private static final String USER_ID = "USER_ID";
    private static final String PHOTOS_COUNT = "PHOTOS_COUNT";
    private static final String PHOTO_LINKS = "PHOTO_LINKS";
    private static final String POSITION = "POSITION";

    private int mUserId;
    private int mPhotosCount;
    private UserPhotoGridAdapter mUserPhotoGridAdapter;
    private Photos mPhotoLinks;
    private LoadingListAdapter.Updater mUpdater;
    private GridViewWithHeaderAndFooter mGridAlbum;
    private View mGridFooterView;
    private BasePendingInit<User> mPendingUserInit = new BasePendingInit<>();
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
            if (position < mPhotosCount) {
                Intent intent = PhotoSwitcherActivity.getPhotoSwitcherIntent(mPendingUserInit.getData().gifts,
                        position,
                        mUserId,
                        mPhotosCount,
                        mUserPhotoGridAdapter
                );
                Fragment parentFrag = getParentFragment();
                if (parentFrag != null) {
                    parentFrag.startActivity(intent);
                } else {
                    startActivity(intent);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpdater();
    }

    public void setUpdater() {
        mUpdater = new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (mGridAlbum != null) {
                    mGridFooterView.setVisibility(View.VISIBLE);
                    Photos data = mUserPhotoGridAdapter.getPhotos();
                    AlbumRequest request = new AlbumRequest(getActivity(), mUserId, data.get(data.size() - 1).getPosition() + 1, AlbumRequest.MODE_ALBUM, AlbumLoadController.FOR_GALLERY);
                    request.callback(new DataApiHandler<AlbumPhotos>() {

                        @Override
                        protected void success(AlbumPhotos data, IApiResponse response) {
                            if (mGridAlbum != null) {
                                mUserPhotoGridAdapter.addPhotos(data, data.more, false);
                            }
                        }

                        @Override
                        protected AlbumPhotos parseResponse(ApiResponse response) {
                            return new AlbumPhotos(response);
                        }

                        @Override
                        public void always(IApiResponse response) {
                            super.always(response);
                            mGridFooterView.setVisibility(View.GONE);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    }).exec();
                }
            }
        };
    }

    private View createGridViewFooter() {
        return ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gridview_footer_progress_bar, null, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);
        mGridFooterView = createGridViewFooter();
        // album
        mGridAlbum = (GridViewWithHeaderAndFooter) root.findViewById(R.id.usedGrid);

        int position = 0;
        if (savedInstanceState != null) {
            mUserId = savedInstanceState.getInt(USER_ID, 0);
            mPhotosCount = savedInstanceState.getInt(PHOTOS_COUNT, 0);
            String linksString = savedInstanceState.getString(PHOTO_LINKS);
            mPhotoLinks = JsonUtils.optFromJson(linksString, Photos.class, new Photos());
            setPhotos(mPhotoLinks);
            position = savedInstanceState.getInt(POSITION, 0);
        }
        addFooterView();
        mGridAlbum.setAdapter(mUserPhotoGridAdapter);
        mGridAlbum.setSelection(position);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        if (mUserPhotoGridAdapter != null) {
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPendingUserInit.setCanSet(true);
        if (mPendingUserInit.getCanSet()) {
            setUserDataPending(mPendingUserInit.getData());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPendingUserInit.setCanSet(false);
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
        outState.putInt(POSITION, (mGridAlbum != null) ? mGridAlbum.getFirstVisiblePosition() : 0);
    }

    public void setUserData(User user) {
        mPendingUserInit.setData(user);
        if (mPendingUserInit.getCanSet()) {
            setUserDataPending(mPendingUserInit.getData());
        }
    }

    private void setUserDataPending(User user) {
        mUserId = user.uid;
        mPhotosCount = user.photosCount;
        mPhotoLinks = user.photos;
        if (mGridAlbum != null && mGridAlbum.getGridViewAdapter() == null) {
            setPhotos(mPhotoLinks);
            addFooterView();
            mGridAlbum.setAdapter(mUserPhotoGridAdapter);
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
    }

    private void addFooterView() {
        if (mGridAlbum != null) {
            if (mGridAlbum.getFooterViewCount() == 0) {
                mGridAlbum.addFooterView(mGridFooterView);
            }
            mGridFooterView.setVisibility(View.GONE);
        }
    }


    private void setPhotos(Photos photos) {
        if (mUserPhotoGridAdapter == null) {
            mUserPhotoGridAdapter = new UserPhotoGridAdapter(getActivity().getApplicationContext(),
                    photos,
                    mPhotosCount,
                    mUpdater);
        }
    }

    @Override
    public void clearContent() {
        if (mPhotoLinks != null) {
            mPhotoLinks.clear();
        }
        if (mUserPhotoGridAdapter != null) {
            mUserPhotoGridAdapter.notifyDataSetChanged();
        }
        mGridAlbum = null;
    }
}
