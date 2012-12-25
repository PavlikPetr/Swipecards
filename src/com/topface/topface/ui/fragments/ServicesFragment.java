package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;

public class ServicesFragment extends BaseFragment {

    public static ServicesFragment newInstance() {
        return new ServicesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_services, null);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        ServicesTextView mCurCoins = (ServicesTextView) root.findViewById(R.id.fpsCurCoins);
        mCurCoins.setText(Integer.toString(CacheProfile.money));

        ServicesTextView mCurPower = (ServicesTextView) root.findViewById(R.id.fpsCurPower);
        mCurPower.setText(Integer.toString(CacheProfile.likes));

        Button mBuyBtn = (Button) root.findViewById(R.id.fpsBuyBtn);
        mBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyAction();
            }
        });
    }

    private void buyAction() {
        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
        startActivity(intent);
    }
}
