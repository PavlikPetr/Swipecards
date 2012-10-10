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
import com.topface.topface.data.Photos;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class ProfilePhotoFragment extends Fragment {

    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private Photos mPhotoLinks;
    private AddPhotoHelper mAddPhotoHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoLinks = new Photos();
        mPhotoLinks.add(null);
        if (CacheProfile.photos != null) {
            mPhotoLinks.addAll(CacheProfile.photos);
        }
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), mPhotoLinks);
        mAddPhotoHelper = new AddPhotoHelper(this);
        mAddPhotoHelper.setOnResultHandler(mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);

        //Navigation bar
        if (getActivity() instanceof EditContainerActivity) {
            ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
            TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
            subTitle.setVisibility(View.VISIBLE);
            subTitle.setText(R.string.edit_album);

            getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
            Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setText(R.string.navigation_edit);
            btnBack.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }

        GridView gridAlbum = (GridView) root.findViewById(R.id.fragmentGrid);
        gridAlbum.setNumColumns(3);
        gridAlbum.setAdapter(mProfilePhotoGridAdapter);
        gridAlbum.setOnItemClickListener(mOnItemClickListener);

        TextView title = (TextView) root.findViewById(R.id.fragmentTitle);

        if (mPhotoLinks != null && mPhotoLinks.size() >= 0) {
            title.setText(Utils.formatPhotoQuantity(CacheProfile.photos.size())); // mPhotoLinks-1
            title.setVisibility(View.VISIBLE);
        } else {
            title.setVisibility(View.INVISIBLE);
        }

        return root;
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
                mAddPhotoHelper.addPhoto();
                return;
            }
            Data.photos = CacheProfile.photos;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoSwitcherActivity.class);
            intent.putExtra(PhotoSwitcherActivity.INTENT_USER_ID, CacheProfile.uid);
            intent.putExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, --position);
            startActivity(intent);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //mProfilePhotoGridAdapter.notifyDataSetChanged(); // <<<<<<<<<<<<<<< !!!!!!!!!!
            //getProfile();
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Toast.makeText(getActivity(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(getActivity(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
 