package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Utils;

import org.json.JSONObject;

public class UserPhotoFragment extends BaseFragment {
    private User mUser;
    private UserPhotoGridAdapter mUserPhotoGridAdapter;
    private TextView mTitle;
    private Photos mPhotoLinks;
    private LoadingListAdapter.Updater mUpdater;
    private GridView mGridAlbum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
        mUpdater = new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (mGridAlbum != null) {
                    Photos data = ((ProfileGridAdapter) mGridAlbum.getAdapter()).getData();
                    AlbumRequest request = new AlbumRequest(getActivity(), mUser.uid, AlbumRequest.DEFAULT_PHOTOS_LIMIT, data.get(data.size() - 2).getPosition() + 1, AlbumRequest.MODE_ALBUM);
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            if (mGridAlbum != null) {
                                JSONObject jsonResult = response.getJsonResult();
                                ((UserPhotoGridAdapter) mGridAlbum.getAdapter()).addData(Photos.parse(jsonResult.optJSONArray("items")), jsonResult.optBoolean("more"));
                            }
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

        mTitle = (TextView) root.findViewById(R.id.usedTitle);

        if (mPhotoLinks == null) {
            mTitle.setText(Utils.formatPhotoQuantity(0));
        }

        mGridAlbum = (GridView) root.findViewById(R.id.usedGrid);
        mGridAlbum.setAdapter(mUserPhotoGridAdapter);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        if (mUserPhotoGridAdapter != null) {
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
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
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoSwitcherActivity.class);
            intent.putExtra(PhotoSwitcherActivity.INTENT_USER_ID, mUser.uid);
            intent.putExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, position);
            intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS_COUNT, mUser.photosCount);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, ((ProfileGridAdapter) mGridAlbum.getAdapter()).getData());
            startActivity(intent);
        }
    };

    public void setUserData(User user) {
        mUser = user;
        mPhotoLinks = user.photos;
        if (mGridAlbum != null && mGridAlbum.getAdapter() == null) {
            setPhotos(mPhotoLinks);
            mGridAlbum.setAdapter(mUserPhotoGridAdapter);
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
    }

    private void setPhotos(Photos photos) {
        if (photos != null) {
            mTitle.setText(Utils.formatPhotoQuantity(mUser.photosCount));
        }

        if (mUserPhotoGridAdapter == null) {
            mUserPhotoGridAdapter = new UserPhotoGridAdapter(getActivity().getApplicationContext(),
                    photos,
                    mUser.photosCount,
                    mUpdater);
        }
    }

    @Override
    public void clearContent() {
        if (mPhotoLinks != null) {
            mPhotoLinks.clear();
        }
        if (mTitle != null) {
            mTitle.setText(Utils.formatPhotoQuantity(0));
        }
        if (mUserPhotoGridAdapter != null) {
            mUserPhotoGridAdapter.notifyDataSetChanged();
        }
        mGridAlbum = null;
    }

}
