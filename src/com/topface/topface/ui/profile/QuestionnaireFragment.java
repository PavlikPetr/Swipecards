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

public class QuestionnaireFragment extends Fragment {
    private User mUser;
    private UserListAdapter mUserListAdapter;
    private TextView mTitle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mUser = ((UserProfileActivity)getActivity()).mDataUser;
        mUserListAdapter = new UserListAdapter(getActivity().getApplicationContext());
        //mUserListAdapter.setUserData(user);
        //mUserListAdapter.setUserData(mDataUser);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_questionnaire, container, false);
        ListView listQuestionnaire = (ListView)root.findViewById(R.id.fragmentQuestionnaire);
        listQuestionnaire.setAdapter(mUserListAdapter);
        
        mTitle = (TextView)root.findViewById(R.id.fragmentTitle);
        mTitle.setText("You have 15 something");
        
        return root;
    }
    
    public void setUserData(User user) {
        mUser = user;
        mUserListAdapter.setUserData(mUser);
        mUserListAdapter.notifyDataSetChanged();
    }
    
}
