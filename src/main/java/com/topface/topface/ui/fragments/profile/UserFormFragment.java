package com.topface.topface.ui.fragments.profile;

import android.os.Bundle;
import android.os.Parcelable;
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

import java.util.ArrayList;
import java.util.LinkedList;

public class UserFormFragment extends ProfileInnerFragment implements OnClickListener {

    private static final String FORM_ITEMS = "FORM_ITEMS";
    private static final String POSITION = "POSITION";
    private static final String USER_ID = "USER_ID";
    private static final String MATCHES = "MATCHES";
    private static final String MATCHED_DATA_ONLY = "MATCHED_DATA_ONLY";

    private int mUserId;
    private LinkedList<FormItem> mForms;
    private int mFormMatches;
    private UserFormListAdapter mUserFormListAdapter;
    private View mTitleLayout;
    private TextView mTitle;
    private ImageView mState;
    private ViewGroup mEmptyFormLayout;
    private Button mAskToFillForm;
    private ProgressBar mPgb;
    private TextView mSuccessText;
    private ListView mListQuestionnaire;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserFormListAdapter = new UserFormListAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        mListQuestionnaire = (ListView) root.findViewById(R.id.fragmentFormList);
        mListQuestionnaire.setAdapter(mUserFormListAdapter);

        mEmptyFormLayout = (ViewGroup) root.findViewById(R.id.loEmptyForm);

        mAskToFillForm =
                (Button) mEmptyFormLayout.findViewById(R.id.btnEmptyForm);
        mAskToFillForm.setOnClickListener(this);

        mPgb = (ProgressBar) mEmptyFormLayout.findViewById(R.id.pgbProgress);
        mSuccessText = (TextView) mEmptyFormLayout.findViewById(R.id.emptyFormSuccess);

        mTitleLayout = root.findViewById(R.id.loUserTitle);
        mTitle = (TextView) root.findViewById(R.id.tvTitle);
        mState = (ImageView) root.findViewById(R.id.ivState);
        if (mForms != null) {
            setUserData(mUserId, mForms, mFormMatches);
        } else if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelableArrayList = savedInstanceState.getParcelableArrayList(FORM_ITEMS);
            if (parcelableArrayList != null) {
                setUserData(savedInstanceState.getInt(USER_ID, 0),
                        parcelableArrayList,
                        savedInstanceState.getInt(MATCHES, 0));
                mListQuestionnaire.setSelection(savedInstanceState.getInt(POSITION, 0));
                if (savedInstanceState.getBoolean(MATCHED_DATA_ONLY, false)) {
                    mUserFormListAdapter.setMatchedDataOnly();
                }
            }
        } else {
            mTitle.setText(Utils.getQuantityString(R.plurals.form_matches, 0, 0));
            mState.setImageResource(R.drawable.user_cell);
        }
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleLayout.setOnClickListener(this);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(USER_ID, mUserId);
        outState.putInt(MATCHES, mFormMatches);
        outState.putParcelableArrayList(FORM_ITEMS, mUserFormListAdapter.saveState());
        outState.putInt(POSITION, mListQuestionnaire.getFirstVisiblePosition());
        outState.putBoolean(MATCHED_DATA_ONLY, mUserFormListAdapter.isMatchedDataOnly());
    }

    public void setUserData(User user) {
        setUserData(user.uid, user.forms, user.formMatches);
    }

    public void setUserData(int userId, LinkedList<FormItem> forms, int formMatches) {
        mUserId = userId;
        mForms = forms;
        mFormMatches = formMatches;
        mUserFormListAdapter.setUserData(mForms);
        mUserFormListAdapter.notifyDataSetChanged();

        initFormHeader();
    }

    private void setUserData(int userId, ArrayList<Parcelable> forms, int formMatches) {
        LinkedList<FormItem> llForms = new LinkedList<>();
        for (Parcelable form : forms) {
            llForms.add((FormItem) form);
        }
        setUserData(userId, llForms, formMatches);
    }

    private void initFormHeader() {
        mTitle.setText(
                Utils.getQuantityString(R.plurals.form_matches, mFormMatches, mFormMatches)
        );

        if (formIsEmpty(mForms)) {
            mEmptyFormLayout.setVisibility(View.VISIBLE);
        } else {
            if (mFormMatches > 0) {
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
                if (mUserId == 0) break;
                StandardMessageSendRequest request = new StandardMessageSendRequest(getActivity(), StandardMessageSendRequest.MESSAGE_FILL_INTERESTS, mUserId);
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
