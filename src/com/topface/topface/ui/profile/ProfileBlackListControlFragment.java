package com.topface.topface.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.topface.R;
import com.topface.topface.billing.ResponseHandler;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.ui.fragments.BaseFragment;


public class ProfileBlackListControlFragment extends BaseFragment {
    private int mUserId;

    public static ProfileBlackListControlFragment newInstance(int userId) {
        ProfileBlackListControlFragment mInstance = new ProfileBlackListControlFragment();
        Bundle args = new Bundle();
        args.putInt("user_id",userId);
        mInstance.setArguments(args);
        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_blacklist,null);
        initFieldsFromArguments();
        initViews(root);
        return root;
    }

    private void initFieldsFromArguments() {
        mUserId = getArguments().getInt("user_id");
    }

    private void initViews(View root) {
        Button btnAddToBlackList = (Button)root.findViewById(R.id.fpbAddToBlackList);
        btnAddToBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToBlackList();
            }
        });
    }

    private void addToBlackList() {
        BlackListAddRequest blaRequest = new BlackListAddRequest(mUserId, getActivity());
//        blaRequest.callback(new
//        ).exec();
    }
}
