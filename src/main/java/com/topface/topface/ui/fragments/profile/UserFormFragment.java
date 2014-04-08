package com.topface.topface.ui.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.StandardMessageSendRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class UserFormFragment extends ProfileInnerFragment implements OnClickListener {
    private User mUser;
    private UserFormListAdapter mUserFormListAdapter;
    private View mTitleLayout;
    private TextView mTitle;
    private ImageView mState;
    private ViewGroup mEmptyFormLayout;
    private Button mAskToFillForm;
    private ProgressBar mPgb;
    private TextView mSuccessText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserFormListAdapter = new UserFormListAdapter(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        ListView listQuestionnaire = (ListView) root.findViewById(R.id.fragmentFormList);
        listQuestionnaire.setAdapter(mUserFormListAdapter);

        mEmptyFormLayout = (ViewGroup) root.findViewById(R.id.loEmptyForm);

        mAskToFillForm =
                (Button) mEmptyFormLayout.findViewById(R.id.btnEmptyForm);
        mAskToFillForm.setOnClickListener(this);

        mPgb = (ProgressBar) mEmptyFormLayout.findViewById(R.id.pgbProgress);
        mSuccessText = (TextView) mEmptyFormLayout.findViewById(R.id.emptyFormSuccess);

        mTitleLayout = root.findViewById(R.id.loUserTitle);
        mTitle = (TextView) root.findViewById(R.id.tvTitle);
        mState = (ImageView) root.findViewById(R.id.ivState);
        if (mUser != null) {
            setUserData(mUser);
        } else {
            mTitle.setText(Utils.getQuantityString(R.plurals.form_matches, 0, 0));
            mState.setImageResource(R.drawable.user_cell);
        }
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleLayout.setOnClickListener(this);

        return root;
    }

    public void setUserData(User user) {
        if (mUser != user)
            mUser = user;
        mUserFormListAdapter.setUserData(mUser);
        mUserFormListAdapter.notifyDataSetChanged();

        initFormHeader();
    }

    private void initFormHeader() {
        mTitle.setText(
                Utils.getQuantityString(R.plurals.form_matches, mUser.formMatches, mUser.formMatches)
        );

        if (formIsEmpty(mUser.forms)) {
            mEmptyFormLayout.setVisibility(View.VISIBLE);
        } else {
            if (mUser.formMatches > 0) {
                mState.setImageResource(R.drawable.user_cell_on);
                mTitleLayout.setOnClickListener(this);
            } else {
                mState.setImageResource(R.drawable.user_cell);
            }
        }
    }

    private boolean formIsEmpty(LinkedList<FormItem> forms) {
        for (FormItem formItem : forms) {
            if (formItem.type == FormItem.DATA) {
                if (formItem.dataId != FormItem.NO_RESOURCE_ID ||
                        formItem.value != null)
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loUserTitle:
                if (mUserFormListAdapter.isMatchedDataOnly()) mUserFormListAdapter.setAllData();
                else mUserFormListAdapter.setMatchedDataOnly();
                mUserFormListAdapter.notifyDataSetChanged();
                break;
            case R.id.btnEmptyForm:
                if (mUser.uid == 0) break;
                StandardMessageSendRequest request = new StandardMessageSendRequest(getActivity(), StandardMessageSendRequest.MESSAGE_FILL_INTERESTS, mUser.uid);
                registerRequest(request);
                mAskToFillForm.setVisibility(View.GONE);
                mPgb.setVisibility(View.VISIBLE);
                request.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        if (mPgb != null && mSuccessText != null) {
                            mPgb.setVisibility(View.GONE);
                            mSuccessText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        if (mPgb != null && mAskToFillForm != null) {
                            mPgb.setVisibility(View.GONE);
                            mAskToFillForm.setVisibility(View.VISIBLE);
                        }
                    }
                }).exec();
                break;
        }
    }
}
