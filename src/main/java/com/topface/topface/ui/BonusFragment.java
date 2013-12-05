package com.topface.topface.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.ui.fragments.BaseFragment;

import com.topface.topface.R;
import com.topface.topface.utils.offerwalls.Offerwalls;

public class BonusFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.meow_offer, null);;
        if (!((BaseFragmentActivity)getActivity()).isPackageInstalled("com.topface.meow", getActivity())) {
            Offerwalls.startOfferwall(getActivity());
        }

        return root;
    }


}
