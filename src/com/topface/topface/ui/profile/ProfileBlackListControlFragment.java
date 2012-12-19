package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.topface.R;
import com.topface.topface.requests.*;
import com.topface.topface.ui.fragments.BaseFragment;


public class ProfileBlackListControlFragment extends BaseFragment {
    public static final String UPDATE_ACTION = "com.topface.topface.updateBlacklistProfile";
    public static final String BLACK_LIST_STATUS = "inBlackList";
    private int mUserId;
    private boolean mInBlackList;
    private Button mBtnAddToBlackList;
    private Button mBtnRemoveFromBlackList;

    public static ProfileBlackListControlFragment newInstance(int userId, boolean inBlackList) {
        ProfileBlackListControlFragment mInstance = new ProfileBlackListControlFragment();
        Bundle args = new Bundle();
        args.putInt("user_id",userId);
        args.putBoolean("in_blacklist", inBlackList);
        mInstance.setArguments(args);
        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_blacklist,null);
        if(savedInstanceState == null) {
            initFieldsFromArguments();
        }
        initViews(root);

        return root;
    }

    private void initFieldsFromArguments() {
        mUserId = getArguments().getInt("user_id");
        mInBlackList = getArguments().getBoolean("in_blacklist");

    }

    private void initViews(View root) {
        mBtnAddToBlackList = (Button)root.findViewById(R.id.fpbAddToBlackList);
        mBtnAddToBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToBlackList();
            }
        });

        mBtnRemoveFromBlackList = (Button)root.findViewById(R.id.fpbDeleteFromBlackList);
        mBtnRemoveFromBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFromBlackList();
            }
        });

        switchButtons();
    }


    private void addToBlackList() {
        BlackListAddRequest blaRequest = new BlackListAddRequest(mUserId, getActivity());
        blaRequest.callback(new VipApiHandler() {
            @Override
            public void success(ApiResponse response) {
                super.success(response);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setChanges(true);
                    }
                });
            }
        }
        ).exec();
    }

    private void removeFromBlackList() {
        BlackListDeleteRequest bldRequest = new BlackListDeleteRequest(mUserId, getActivity());

        bldRequest.callback(new VipApiHandler() {
            @Override
            public void success(ApiResponse response) {
                super.success(response);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setChanges(false);
                    }
                });

            }
        }).exec();
    }

    private void setChanges(boolean inBlackList) {
        mInBlackList = inBlackList;
        switchButtons();
        sendUpdateIntent();
    }

    private void switchButtons() {
        if(mInBlackList) {
            mBtnAddToBlackList.setVisibility(View.GONE);
            mBtnRemoveFromBlackList.setVisibility(View.VISIBLE);
        } else  {
            mBtnRemoveFromBlackList.setVisibility(View.GONE);
            mBtnAddToBlackList.setVisibility(View.VISIBLE);
        }
    }

    private void sendUpdateIntent() {
        Intent intent = new Intent(UPDATE_ACTION);
        intent.putExtra(BLACK_LIST_STATUS,mInBlackList);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
