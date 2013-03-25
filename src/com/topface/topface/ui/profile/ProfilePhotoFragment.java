package com.topface.topface.ui.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

public class ProfilePhotoFragment extends BaseFragment {

    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private Photos mPhotoLinks;
    private AddPhotoHelper mAddPhotoHelper;
    private ViewFlipper mViewFlipper;
    private GridView mGridAlbum;
    private BroadcastReceiver mPhotosReceiver;

    public ProfilePhotoFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPhotoLinks();
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), mPhotoLinks, CacheProfile.totalPhotos, new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        });
        mAddPhotoHelper = new AddPhotoHelper(this, null);
        mAddPhotoHelper.setOnResultHandler(mHandler);
    }

    private void sendAlbumRequest() {
        if (mPhotoLinks == null || mPhotoLinks.size() < 2) {
            return;
        }
        int position = mPhotoLinks.get(mPhotoLinks.size() - 2).getPosition();
        AlbumRequest request = new AlbumRequest(
                getActivity(),
                CacheProfile.uid,
                AlbumRequest.DEFAULT_PHOTOS_LIMIT,
                position + 1,
                AlbumRequest.MODE_ALBUM
        );
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                if (mGridAlbum != null) {
                    ((ProfilePhotoGridAdapter) mGridAlbum.getAdapter()).setData(Photos.parse(response.jsonResult.optJSONArray("items")), response.jsonResult.optBoolean("more"));
                }
            }

            @Override
            public void fail(int codeError, ApiResponse response) {

            }
        }).exec();
    }

    private void initPhotoLinks() {
        if (mPhotoLinks == null) mPhotoLinks = new Photos();
        mPhotoLinks.clear();
        mPhotoLinks.add(null);
        if (CacheProfile.photos != null) {
            mPhotoLinks.addAll(CacheProfile.photos);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile_photos, container, false);

        //Navigation bar
        if (getActivity() instanceof EditContainerActivity) {
            ActionBar actionBar = getActionBar(root);
            actionBar.setTitleText(getString(R.string.edit_title));
            actionBar.setSubTitleText(getString(R.string.edit_album));
            actionBar.showBackButton(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        mGridAlbum = (GridView) root.findViewById(R.id.usedGrid);
        mGridAlbum.setAdapter(mProfilePhotoGridAdapter);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        mGridAlbum.setOnScrollListener(mProfilePhotoGridAdapter);

        final TextView title = (TextView) root.findViewById(R.id.usedTitle);

        initTitleText(title);

        root.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(mAddPhotoHelper.getAddPhotoClickListener());
        root.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(mAddPhotoHelper.getAddPhotoClickListener());
        root.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
            }
        });

        mPhotosReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<Photo> arrList = intent.getParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS);
                Photos newPhotos = new Photos();
                newPhotos.addAll(arrList);
                ((ProfilePhotoGridAdapter) mGridAlbum.getAdapter()).setData(newPhotos, intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_MORE, false));
                initTitleText(title);
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPhotosReceiver, new IntentFilter(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT));

        return root;
    }

    private void initTitleText(TextView title) {
        title.setVisibility(View.VISIBLE);
        if (mPhotoLinks != null && CacheProfile.photos != null) {
            if (CacheProfile.photos.size() > 1) {
                title.setText(Utils.formatPhotoQuantity(CacheProfile.photos.size()));
                return;
            }
        }
        title.setText(R.string.upload_photos);
    }

    @Override
    public void onResume() {
        initPhotoLinks();
        mProfilePhotoGridAdapter.updateData();
        mProfilePhotoGridAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mViewFlipper.setDisplayedChild(1);
                return;
            }
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoSwitcherActivity.class);
            intent.putExtra(PhotoSwitcherActivity.INTENT_USER_ID, CacheProfile.uid);
            intent.putExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, --position);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, ((ProfileGridAdapter) mGridAlbum.getAdapter()).getData());

            startActivity(intent);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewFlipper.setDisplayedChild(0);
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Photo photo = (Photo) msg.obj;

                CacheProfile.photos.addFirst(photo);
                mProfilePhotoGridAdapter.addFirst(photo);

                Toast.makeText(App.getContext(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPhotosReceiver);
    }
}
 