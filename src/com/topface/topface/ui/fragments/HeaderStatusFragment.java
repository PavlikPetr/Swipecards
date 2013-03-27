package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.EditMainFormItemsFragment;
import com.topface.topface.utils.CacheProfile;

/**
* Created with IntelliJ IDEA.
* User: User
* Date: 27.03.13
* Time: 14:56
* To change this template use File | Settings | File Templates.
*/
public class HeaderStatusFragment extends BaseFragment {
    private static final String ARG_TAG_STATUS = "status";
    private static final String ARG_TAG_PROFILE_TYPE = "profile_type";

    private ImageButton mBtnEditStatus;
    private EditText mStatusView;
    private String mStatusVal;
    private int mProfileType;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        restoreState();

        //init views
        View root = inflater.inflate(R.layout.fragment_profile_header_status, null);
        mBtnEditStatus = (ImageButton) root.findViewById(R.id.btnEdit);
        mStatusView = (EditText) root.findViewById(R.id.tvStatus);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(EditMainFormItemsFragment.MAX_STATUS_LENGTH);
        mStatusView.setFilters(filters);
        mStatusView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    final String oldStatus = CacheProfile.status;
                    final String newStatus = mStatusView.getText().toString();
                    if (!oldStatus.equals(newStatus)) {
                        SettingsRequest request = new SettingsRequest(getActivity());
                        request.status = newStatus;
                        CacheProfile.status = newStatus;
                        mStatusVal = newStatus;
                        request.callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) {
                                CacheProfile.status = oldStatus;
                                mStatusVal = oldStatus;
                            }
                        }).exec();
                    }
                }
            }
        });

        if (mProfileType == ProfileFragment.TYPE_MY_PROFILE) {
            mStatusView.setHint(R.string.status_is_empty);
            mBtnEditStatus.setVisibility(View.VISIBLE);
            mBtnEditStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                    startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_STATUS);
                }
            });
        } else {
            mBtnEditStatus.setVisibility(View.GONE);
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViews();
    }

    public void setProfile(Profile profile) {
        if (profile != null) {
            initState(profile);
            saveState(this, profile);
        }
        refreshViews();
    }

    private void refreshViews() {
        updateUI(new Runnable() {
            @Override
            public void run() {
                mStatusView.setText(mStatusVal);
            }
        });
    }

    private void restoreState() {
        if (getArguments() != null) {
            mStatusVal = getArguments().getString(ARG_TAG_STATUS);
            mProfileType = getArguments().getInt(ARG_TAG_PROFILE_TYPE);
        }
    }

    private void initState(Profile profile) {
        mStatusVal = profile.status;
    }

    private static void saveState(Fragment fragment, Profile profile, int profileType) {
        if (!fragment.isVisible()) {
            Bundle args = new Bundle();
            if (fragment.getArguments() == null) {
                fragment.setArguments(args);
            }
            fragment.getArguments().putString(ARG_TAG_STATUS, profile.status);
            fragment.getArguments().putInt(ARG_TAG_PROFILE_TYPE, profileType);
        }
    }

    private void saveState(HeaderStatusFragment fragment, Profile profile) {
        if (!fragment.isVisible()) {
            Bundle args = new Bundle();
            if (fragment.getArguments() == null) {
                fragment.setArguments(args);
            }
            fragment.getArguments().putString(ARG_TAG_STATUS, profile.status);
        }
    }

    public static Fragment newInstance(Profile profile, int profileType) {
        HeaderStatusFragment fragment = new HeaderStatusFragment();
        if (profile == null) return fragment;
        saveState(fragment, profile, profileType);
        return fragment;
    }

    @Override
    public void clearContent() {
        mStatusView.setText(Static.EMPTY);
    }

    @Override
    public void onPause() {
        super.onPause();
        mStatusView.clearFocus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mProfileType == ProfileFragment.TYPE_MY_PROFILE && requestCode == EditContainerActivity.INTENT_EDIT_STATUS) {
            mStatusVal = CacheProfile.status;
        }
    }
}
