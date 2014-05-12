package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.offerwalls.Offerwalls;

public class FreeCoinsFragment extends BaseFragment{
    public static FreeCoinsFragment newInstance() {
        return new FreeCoinsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.free_coins_fragment, null);
        initView(root);
        return root;
    }

    private void initView(View root) {
        View offerwall = root.findViewById(R.id.btnOfferwall);
        offerwall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Offerwalls.startOfferwall(getActivity());
            }
        });
        offerwall.setVisibility(CacheProfile.paid ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Debug.log("FreeCoinsFragment:: onResume");
    }
}
