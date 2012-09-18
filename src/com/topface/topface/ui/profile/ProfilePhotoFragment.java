package com.topface.topface.ui.profile;

import java.util.HashMap;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfilePhotoFragment extends Fragment {
    private TextView mTitle;
    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private SparseArray<HashMap<String, String>> mPhotoLinks;
    private AddPhotoHelper mAddPhotoHelper;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoLinks = new SparseArray<HashMap<String, String>>();
        mPhotoLinks.append(0, null);
        for(int i=0; i<CacheProfile.photoLinks.size(); i++) {
            mPhotoLinks.append(i+1, CacheProfile.photoLinks.get(CacheProfile.photoLinks.keyAt(i)));
        }
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), mPhotoLinks);
        mAddPhotoHelper = new AddPhotoHelper(this);
        mAddPhotoHelper.setOnResultHandler(mHandler);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);
        
        GridView gridAlbum = (GridView)root.findViewById(R.id.fragmentGrid);
        gridAlbum.setNumColumns(3);
        gridAlbum.setAdapter(mProfilePhotoGridAdapter);
        gridAlbum.setOnItemClickListener(mOnItemClickListener);
        
        mTitle = (TextView)root.findViewById(R.id.fragmentTitle);
        
        if(mPhotoLinks != null && mPhotoLinks.size() >= 0) {
            mTitle.setText(CacheProfile.photoLinks.size() + " photos"); // mPhotoLinks-1
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.INVISIBLE);  
        }

        return root;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoHelper.checkActivityResult(requestCode, resultCode, data);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position == 0) {
                mAddPhotoHelper.addPhoto();
                return;
            }
            Data.photoAlbum = CacheProfile.photoLinks;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoAlbumActivity.class);
            intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID, CacheProfile.uid);
            intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS, --position);
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
            }
            else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(getActivity(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
 