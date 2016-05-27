package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.adapters.BasePhotoRecyclerViewAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.adapters.UserRecyclerViewAdapter;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONException;

@FlurryOpenEvent(name = UserPhotoFragment.PAGE_NAME)
public class UserPhotoFragment extends ProfileInnerFragment {

    private static final String USER_ID = "USER_ID";
    private static final String PHOTOS_COUNT = "PHOTOS_COUNT";
    private static final String PHOTO_LINKS = "PHOTO_LINKS";
    private static final String POSITION = "POSITION";
    public static final String PAGE_NAME = "user.photo";

    private int mUserId;
    private int mPhotosCount;
    private UserRecyclerViewAdapter mUserRecyclerViewAdapter;
    private Photos mPhotoLinks;
    private LoadingListAdapter.Updater mUpdater;
    private RecyclerView mGridAlbum;
    private BasePendingInit<User> mPendingUserInit = new BasePendingInit<>();
    private BasePhotoRecyclerViewAdapter.OnRecyclerViewItemClickListener mClickListener = new BasePhotoRecyclerViewAdapter.OnRecyclerViewItemClickListener() {
        @Override
        public void itemClick(View view, int itemPosition, Photo photo) {
            if (itemPosition < mPhotosCount) {
                Intent intent = PhotoSwitcherActivity.getPhotoSwitcherIntent(mPendingUserInit.getData().gifts.getGifts(),
                        itemPosition,
                        mUserId,
                        mPhotosCount,
                        mUserRecyclerViewAdapter
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

    @Override
    public boolean isTrackable() {
        return false;
    }

    public void setUpdater() {
        mUpdater = new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (mGridAlbum != null) {
                    Photos data = mUserRecyclerViewAdapter.getPhotos();
                    AlbumRequest request = new AlbumRequest(getActivity(), mUserId, data.get(data.size() - 1).getPosition() + 1, AlbumRequest.MODE_ALBUM, AlbumLoadController.FOR_GALLERY);
                    request.callback(new DataApiHandler<AlbumPhotos>() {

                        @Override
                        protected void success(AlbumPhotos data, IApiResponse response) {
                            if (mGridAlbum != null) {
                                mUserRecyclerViewAdapter.addPhotos(data, data.more, false, false);
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

    private View createGridViewFooter() {
        return ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gridview_footer_progress_bar, null, false);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.user_photo_layout, container, false);
        mGridAlbum = (RecyclerView) root.findViewById(R.id.usedGrid);
        int position = 0;
        if (savedInstanceState != null) {
            mUserId = savedInstanceState.getInt(USER_ID, 0);
            mPhotosCount = savedInstanceState.getInt(PHOTOS_COUNT, 0);
            String linksString = savedInstanceState.getString(PHOTO_LINKS);
            mPhotoLinks = JsonUtils.optFromJson(linksString, Photos.class, new Photos());
            setPhotos(mPhotoLinks);
            position = savedInstanceState.getInt(POSITION, 0);
        }
        int spanCount = getResources().getInteger(R.integer.add_to_leader_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        mGridAlbum.setLayoutManager(manager);
        mGridAlbum.setAdapter(mUserRecyclerViewAdapter);
        mGridAlbum.scrollToPosition(position);
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
        if (mUserRecyclerViewAdapter != null) {
            outState.putInt(POSITION, (mGridAlbum != null) ? mUserRecyclerViewAdapter.getFirstVisibleItemPos() : 0);
        }
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
        if (mGridAlbum != null && mGridAlbum.getAdapter() == null) {
            setPhotos(mPhotoLinks);
            mGridAlbum.setAdapter(mUserRecyclerViewAdapter);
        }
    }


    private void setPhotos(Photos photos) {
        if (mUserRecyclerViewAdapter == null) {
            mUserRecyclerViewAdapter = (UserRecyclerViewAdapter) new UserRecyclerViewAdapter(
                    photos,
                    mPhotosCount,
                    mUpdater)
                    .setFooter(createGridViewFooter(), false);
            mUserRecyclerViewAdapter.setOnItemClickListener(mClickListener);
        }
    }

    @Override
    public void clearContent() {
        if (mPhotoLinks != null) {
            mPhotoLinks.clear();
        }
        if (mUserRecyclerViewAdapter != null) {
            mUserRecyclerViewAdapter.notifyDataSetChanged();
        }
        mGridAlbum = null;
    }
}
