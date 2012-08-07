package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Album;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.profile.album.PhotoAlbumActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoFragment extends Fragment {
    private int mUserId;
    private LinkedList<Album> mUserAlbum;
    private UserGridAdapter mUserGridAdapter;
    private TextView mTitle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserAlbum = new LinkedList<Album>();
        mUserGridAdapter = new UserGridAdapter(getActivity().getApplicationContext(), mUserAlbum);
        mUserId = getArguments() != null ? getArguments().getInt(UserProfileActivity.INTENT_USER_ID) : -1;
        
        getUserAlbum();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);
        GridView gridAlbum = (GridView)root.findViewById(R.id.fragmentGrid);
        gridAlbum.setNumColumns(3);
        gridAlbum.setAdapter(mUserGridAdapter);
        gridAlbum.setOnItemClickListener(mOnItemClickListener);
        
        mTitle = (TextView)root.findViewById(R.id.fragmentTitle);
        mTitle.setText(" 15 photos");

        return root;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    
    private void getUserAlbum() {
        AlbumRequest albumRequest = new AlbumRequest(getActivity().getApplicationContext());
        albumRequest.uid = mUserId;
        albumRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                mUserAlbum.addAll(Album.parse(response));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // mTitle.setText(mUserAlbum.size() + "photos");
                        mUserGridAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }
    
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
            Data.photoAlbum = mUserAlbum;
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoAlbumActivity.class);
            intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID, mUserId);
            intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS, position);
            startActivity(intent);
        }
    };
}
 