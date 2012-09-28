package com.topface.topface.ui.profile;

import java.util.HashMap;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
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
            }
            else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(getActivity(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
 