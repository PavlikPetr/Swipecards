package com.topface.topface.ui.profile;

import com.topface.topface.R;
import com.topface.topface.data.User;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class UserFormFragment extends Fragment {
    private User mUser;
    private UserFormListAdapter mUserPhotoListAdapter;
    private TextView mTitle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mUser = ((UserProfileActivity)getActivity()).mDataUser;
        mUserPhotoListAdapter = new UserFormListAdapter(getActivity().getApplicationContext());
        //mUserListAdapter.setUserData(user);
        //mUserListAdapter.setUserData(mDataUser);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        ListView listQuestionnaire = (ListView)root.findViewById(R.id.fragmentFormList);
        listQuestionnaire.setAdapter(mUserPhotoListAdapter);
        
        mTitle = (TextView)root.findViewById(R.id.fragmentTitle);
        mTitle.setText("You have 15 something");
        
        return root;
    }
    
    public void setUserData(User user) {
        mUser = user;
        mUserPhotoListAdapter.setUserData(mUser);
        mUserPhotoListAdapter.notifyDataSetChanged();
    }
    
}
