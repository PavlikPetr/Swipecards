package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class ProfilePhotoFragment extends Fragment {

    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private Photos mPhotoLinks;
    private AddPhotoHelper mAddPhotoHelper;

    private ViewFlipper mViewFlipper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPhotoLinks();
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), mPhotoLinks);
        mAddPhotoHelper = new AddPhotoHelper(this);
        mAddPhotoHelper.setOnResultHandler(mHandler);
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

        GridView gridAlbum = (GridView) root.findViewById(R.id.fragmentGrid);
        gridAlbum.setAdapter(mProfilePhotoGridAdapter);
        gridAlbum.setOnItemClickListener(mOnItemClickListener);

        TextView title = (TextView) root.findViewById(R.id.fragmentTitle);

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
            Data.photos = CacheProfile.photos;
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
                Toast.makeText(getActivity(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(getActivity(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
 