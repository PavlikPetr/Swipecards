package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

public class ProfilePhotoFragment extends BaseFragment {

    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private Photos mPhotoLinks;
    private AddPhotoHelper mAddPhotoHelper;
    private ViewFlipper mViewFlipper;
    private LockerView lockerView;
    private GridView mGridAlbum;

    public ProfilePhotoFragment() {
        super();
    }

    public ProfilePhotoFragment(LockerView lockerView) {
        super();
        this.lockerView = lockerView;
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
        mAddPhotoHelper = new AddPhotoHelper(this, lockerView);
        mAddPhotoHelper.setOnResultHandler(mHandler);
    }

    private void sendAlbumRequest() {
        AlbumRequest request = new AlbumRequest(getActivity(), CacheProfile.uid, AlbumRequest.DEFAULT_PHOTOS_LIMIT, mPhotoLinks.get(mPhotoLinks.size() - 2).getId(), false);
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                if(mGridAlbum != null) {
                    ((ProfilePhotoGridAdapter)mGridAlbum.getAdapter()).setData(Photos.parse(response.jsonResult.optJSONArray("items")), response.jsonResult.optBoolean("more"));
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
            ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
            TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
            subTitle.setVisibility(View.VISIBLE);
            subTitle.setText(R.string.edit_album);

            getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
            Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setText(R.string.general_edit_button);
            btnBack.setOnClickListener(new OnClickListener() {
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

        TextView title = (TextView) root.findViewById(R.id.usedTitle);

        if (mPhotoLinks != null && CacheProfile.photos != null) {
            title.setText(Utils.formatPhotoQuantity(CacheProfile.photos.size()));
        } else {
            title.setText(Utils.formatPhotoQuantity(0));
        }
        title.setVisibility(View.VISIBLE);

        root.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(mAddPhotoHelper.getAddPhotoClickListener());
        root.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(mAddPhotoHelper.getAddPhotoClickListener());
        root.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        initPhotoLinks();
        mProfilePhotoGridAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mAddPhotoHelper.checkActivityResult(requestCode, resultCode, data);
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
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, CacheProfile.photos);

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
                mPhotoLinks.add(1, photo);

                mProfilePhotoGridAdapter.notifyDataSetChanged();
                Toast.makeText(App.getContext(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };


}
 