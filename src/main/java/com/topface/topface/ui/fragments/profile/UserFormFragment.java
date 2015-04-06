package com.topface.topface.ui.fragments.profile;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.User;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.StandardMessageSendRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.LinkedList;

public class UserFormFragment extends ProfileInnerFragment implements OnClickListener {

    private static final String FORM_ITEMS = "FORM_ITEMS";
    private static final String POSITION = "POSITION";
    private static final String USER_ID = "USER_ID";

    private int mUserId;
    private LinkedList<FormItem> mForms;
    private UserFormListAdapter mUserFormListAdapter;
    private ViewGroup mEmptyFormLayout;
    private Button mAskToFillForm;
    private ProgressBar mPgb;
    private TextView mSuccessText;
    private ListView mListQuestionnaire;
    private BasePendingInit<User> mPendingUserInit = new BasePendingInit<>();

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

        if (mForms != null) {
            setUserData(mUserId, mForms);
        } else if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelableArrayList = savedInstanceState.getParcelableArrayList(FORM_ITEMS);
            if (parcelableArrayList != null) {
                setUserData(savedInstanceState.getInt(USER_ID, 0),
                        parcelableArrayList);
                mListQuestionnaire.setSelection(savedInstanceState.getInt(POSITION, 0));
            }
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(USER_ID, mUserId);
        outState.putParcelableArrayList(FORM_ITEMS, mUserFormListAdapter.saveState());
        outState.putInt(POSITION, mListQuestionnaire.getFirstVisiblePosition());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPendingUserInit.setCanSet(true);
        if (mPendingUserInit.getCanSet()) {
            setUserDataPending(mPendingUserInit.getData());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPendingUserInit.setCanSet(false);
    }

    public void setUserData(User user) {
        mPendingUserInit.setData(user);
        if (mPendingUserInit.getCanSet()) {
            setUserDataPending(mPendingUserInit.getData());
        }
    }

    private void setUserDataPending(User user) {
        setUserData(user.uid, user.forms);
    }

    public void setUserData(int userId, LinkedList<FormItem> forms) {
        mUserId = userId;
        mForms = forms;
        mUserFormListAdapter.setUserData(mForms);
        mUserFormListAdapter.notifyDataSetChanged();

        if (mUserFormListAdapter.isEmpty()) {
            mEmptyFormLayout.setVisibility(View.VISIBLE);
            mListQuestionnaire.setVisibility(View.GONE);
        }
    }

    private void setUserData(int userId, ArrayList<Parcelable> forms) {
        LinkedList<FormItem> llForms = new LinkedList<>();
        for (Parcelable form : forms) {
            llForms.add((FormItem) form);
        }
        setUserData(userId, llForms);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
