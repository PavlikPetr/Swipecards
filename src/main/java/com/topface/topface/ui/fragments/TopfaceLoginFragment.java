package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;

/**
 * Created by onikitin on 09.07.15.
 */
public class TopfaceLoginFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topface_auth, null);
    }

    @Override
    protected String getTitle() {
        return getActivity().getString(R.string.entrance);
    }


}
