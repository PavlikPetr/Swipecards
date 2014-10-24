package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;

public class EditorProfileActionsFragment extends BaseFragment {
    public static final String USERID = "USERID";
    private int mUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_editor_profile_actions, container, false);
        Bundle args = getArguments();

        mUserId = args.getInt(USERID, -1);

        initViews(root);

        if (mUserId == -1) {
            getActivity().finish();
        }
        return root;
    }

    private void initViews(View root) {
        root.setVisibility(View.VISIBLE);
    }

}
