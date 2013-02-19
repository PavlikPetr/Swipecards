package com.topface.topface.ui.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

public class UserPhotoFragment extends BaseFragment {
    private User mUser;
    private UserPhotoGridAdapter mUserPhotoGridAdapter;
    private TextView mTitle;
    private Photos mPhotoLinks;
    private LoadingListAdapter.Updater mUpdater;
    private int totalCount;
    private GridView mGridAlbum;
    private BroadcastReceiver mPhotosReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpdater = new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                Photos data = ((ProfileGridAdapter)mGridAlbum.getAdapter()).getData();
                AlbumRequest request = new AlbumRequest(getActivity(), mUser.uid, AlbumRequest.DEFAULT_PHOTOS_LIMIT, data.get(data.size() - 2).getId(), AlbumRequest.MODE_ALBUM);
                request.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        if(mGridAlbum != null) {
                            ((UserPhotoGridAdapter)mGridAlbum.getAdapter()).setData(Photos.parse(response.jsonResult.optJSONArray("items")), response.jsonResult.optBoolean("more"));
                        }
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {

                    }
                }).exec();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);

        mTitle = (TextView) root.findViewById(R.id.usedTitle);

        if (mPhotoLinks != null) {
//            setPhotos(mPhotoLinks);
        } else {
            mTitle.setText(Utils.formatPhotoQuantity(0));
        }

        mGridAlbum = (GridView) root.findViewById(R.id.usedGrid);
        mGridAlbum.setAdapter(mUserPhotoGridAdapter);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        if(mUserPhotoGridAdapter != null) {
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
        mPhotosReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<Photo> arrList = intent.getParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS);
                Photos newPhotos = new Photos();
                newPhotos.addAll(arrList);
                ((UserPhotoGridAdapter) mGridAlbum.getAdapter()).setData(newPhotos, intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_MORE, false));
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPhotosReceiver, new IntentFilter(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT));

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
            intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS_COUNT, mUser.totalPhotos);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, ((ProfileGridAdapter)mGridAlbum.getAdapter()).getData());
            startActivity(intent);
        }
    };

    public void setUserData(User user) {
        mUser = user;
        mPhotoLinks = user.photos;
        if(mGridAlbum.getAdapter() != null) {
//            ((UserPhotoGridAdapter)mGridAlbum.getAdapter()).setData(mPhotoLinks);
        } else {
            setPhotos(mPhotoLinks);
            mGridAlbum.setAdapter(mUserPhotoGridAdapter);
            mGridAlbum.setOnScrollListener(mUserPhotoGridAdapter);
        }
    }

    private void setPhotos(Photos photos) {
        if (photos != null) {
            mTitle.setText(Utils.formatPhotoQuantity(mUser.totalPhotos));
        }

        if (mUserPhotoGridAdapter == null) {
            mUserPhotoGridAdapter = new UserPhotoGridAdapter(getActivity().getApplicationContext(),
                                                             photos,
                                                             mUser.totalPhotos,
                                                                     mUpdater);
        }
    }

    @Override
    public void clearContent() {
        if (mPhotoLinks != null) mPhotoLinks.clear();
        mTitle.setText(Utils.formatPhotoQuantity(0));
        mUserPhotoGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPhotosReceiver);
    }
}
