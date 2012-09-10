package com.topface.topface.ui.profile;

import java.util.HashMap;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
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

public class ProfilePhotoFragment extends Fragment {
    private TextView mTitle;
    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private SparseArray<HashMap<String, String>> mPhotoLinks;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoLinks = CacheProfile.photoLinks;
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), mPhotoLinks);
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
            mTitle.setText(mPhotoLinks.size() + " фотографий");
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
    public void onStart() {
        super.onStart();        
    }
    
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
            Data.photoAlbum = CacheProfile.photoLinks;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoAlbumActivity.class);
            intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID, CacheProfile.uid);
            intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS, position);
            startActivity(intent);
        }
    };
}
 