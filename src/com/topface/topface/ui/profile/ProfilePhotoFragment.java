package com.topface.topface.ui.profile;

import java.util.HashMap;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.ui.profile.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfilePhotoFragment extends Fragment {
    private TextView mTitle;
    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private SparseArray<HashMap<String, String>> mPhotoLinks;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoLinks = new SparseArray<HashMap<String, String>>();
        mPhotoLinks.append(0, null);
        for(int i=0; i<CacheProfile.photoLinks.size(); i++) {
            mPhotoLinks.append(i+1, CacheProfile.photoLinks.get(CacheProfile.photoLinks.keyAt(i)));
        }
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), mPhotoLinks);
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
			
			((Button)getActivity().findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);		
			Button btnBack = (Button)getActivity().findViewById(R.id.btnNavigationBackWithText);
			btnBack.setVisibility(View.VISIBLE);
			btnBack.setText(R.string.navigation_edit);
			btnBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {		
					getActivity().finish();				
				}
			});
        }
        
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
    public void onStart() {
        super.onStart();        
    }
    
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position == 0) {
                Toast.makeText(getActivity(), "Click", Toast.LENGTH_SHORT).show();
                return;
            }
            Data.photoAlbum = CacheProfile.photoLinks;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoAlbumActivity.class);
            intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID, CacheProfile.uid);
            intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS, --position);
            startActivity(intent);
        }
    };
}
 