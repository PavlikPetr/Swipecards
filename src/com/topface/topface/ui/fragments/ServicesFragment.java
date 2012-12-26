package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;

public class ServicesFragment extends BaseFragment {

    private ServicesTextView mCurCoins;
    private ServicesTextView mCurLikes;
    private BroadcastReceiver mBroadcastReceiver;

    public static ServicesFragment newInstance() {
        return new ServicesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateViews();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_services, null);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        mCurCoins = (ServicesTextView) root.findViewById(R.id.fpsCurCoins);
        mCurLikes = (ServicesTextView) root.findViewById(R.id.fpsCurLikes);
        updateViews();

        Button mBuyBtn = (Button) root.findViewById(R.id.fpsBuyBtn);
        mBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyAction();
            }
        });
    }

    public void updateViews() {
        if (mCurLikes != null && mCurCoins != null) {
            mCurCoins.setText(Integer.toString(CacheProfile.money));
            mCurLikes.setText(Integer.toString(CacheProfile.likes));
        }
    }

    private void buyAction() {
        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }
}
