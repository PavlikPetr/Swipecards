package com.topface.topface.ui.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.utils.Utils;

public class UserFormFragment extends Fragment {
    private User mUser;
    private UserFormListAdapter mUserFormListAdapter;
    private TextView mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserFormListAdapter = new UserFormListAdapter(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        ListView listQuestionnaire = (ListView) root.findViewById(R.id.fragmentFormList);
        listQuestionnaire.setAdapter(mUserFormListAdapter);

        mTitle = (TextView) root.findViewById(R.id.fragmentTitle);
        if (mUser != null) {
        	mTitle.setText(Utils.formatFormMatchesQuantity(mUser.formMatches));
        } else {
        	mTitle.setText(Utils.formatFormMatchesQuantity(0));
        }
        mTitle.setVisibility(View.VISIBLE);

        return root;
    }

    public void setUserData(User user) {
        mUser = user;
        mUserFormListAdapter.setUserData(mUser);
        mUserFormListAdapter.notifyDataSetChanged();
        
        mTitle.setText(Utils.formatFormMatchesQuantity(mUser.formMatches));
    }

}
