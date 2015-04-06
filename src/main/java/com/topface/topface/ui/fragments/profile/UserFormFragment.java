package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.StandardMessageSendRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class UserFormFragment extends ProfileInnerFragment implements OnClickListener {

    private static final String FORM_ITEMS = "FORM_ITEMS";
    private static final String FORM_GIFTS = "FORM_GIFTS";
    private static final String POSITION = "POSITION";
    private static final String USER_ID = "USER_ID";

    private int mUserId;
    private LinkedList<FormItem> mForms;
    private Profile.Gifts mGifts;
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
        mUserFormListAdapter.setOnGiftsClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    Activity activity = getActivity();
                    Intent intent = GiftsActivity.getSendGiftIntent(activity, mUserId);
                    activity.startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
                }
            }
        });
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
            setUserData(mUserId, mForms, mGifts);
        } else if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelableArrayList = savedInstanceState.getParcelableArrayList(FORM_ITEMS);
            ArrayList<Gift> parcelableGifts = savedInstanceState.<Gift>getParcelableArrayList(FORM_GIFTS);
            if (parcelableArrayList != null && parcelableGifts != null) {
                Profile.Gifts gifts = new Profile.Gifts();
                gifts.addAll(parcelableGifts);
                setUserData(savedInstanceState.getInt(USER_ID, 0),
                        parcelableArrayList, gifts);
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
        outState.putParcelableArrayList(FORM_GIFTS, mGifts);
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
        setUserData(user.uid, user.forms, user.gifts);
    }

    public void setUserData(int userId, LinkedList<FormItem> forms, Profile.Gifts gifts) {
        mUserId = userId;
        mForms = forms;
        mGifts = gifts;
        mUserFormListAdapter.setUserData(mForms, gifts);
        mUserFormListAdapter.notifyDataSetChanged();

        if (mUserFormListAdapter.isEmpty()) {
            mEmptyFormLayout.setVisibility(View.VISIBLE);
            mListQuestionnaire.setVisibility(View.GONE);
        }
    }

    private void setUserData(int userId, ArrayList<Parcelable> forms, Profile.Gifts gifts) {
        LinkedList<FormItem> llForms = new LinkedList<>();
        for (Parcelable form : forms) {
            llForms.add((FormItem) form);
        }
        setUserData(userId, llForms, gifts);
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
