package com.topface.topface.ui.profile;

import java.util.HashMap;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.User;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class UserPhotoFragment extends Fragment {
    private User mUser;
    private UserPhotoGridAdapter mUserPhotoGridAdapter;
    private TextView mTitle;
    private SparseArray<HashMap<String, String>> mPhotoLinks;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserPhotoGridAdapter = new UserPhotoGridAdapter(getActivity().getApplicationContext());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);
        
        GridView gridAlbum = (GridView)root.findViewById(R.id.fragmentGrid);
        gridAlbum.setNumColumns(3);
        gridAlbum.setAdapter(mUserPhotoGridAdapter);
        gridAlbum.setOnItemClickListener(mOnItemClickListener);
        
        mTitle = (TextView)root.findViewById(R.id.fragmentTitle);

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
            Data.photoAlbum = mUser.photoLinks;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoAlbumActivity.class);
            intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID, mUser.uid);
            intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS, position);
            startActivity(intent);
        }
    };
    
    public void setUserData(User user) {
        mUser = user;
        mPhotoLinks = user.photoLinks;
        if(mUserPhotoGridAdapter != null) {
          mUserPhotoGridAdapter.setUserData(user.photoLinks);
          mUserPhotoGridAdapter.notifyDataSetChanged();
        }
        
        if(mPhotoLinks != null && mPhotoLinks.size() >= 0) {
            mTitle.setText(mPhotoLinks.size() + " фотографий");
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.INVISIBLE);  
        }
    }
}
 