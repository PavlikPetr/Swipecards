package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.StandardMessageSendRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;

@FlurryOpenEvent(name = UserFormFragment.PAGE_NAME)
public class UserFormFragment extends AbstractFormFragment implements OnClickListener {

    public static final String PAGE_NAME = "user.form";

    private ViewGroup mEmptyFormLayout;
    private Button mAskToFillForm;
    private ProgressBar mPgb;
    private TextView mSuccessText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mEmptyFormLayout = (ViewGroup) root.findViewById(R.id.loEmptyForm);
        mAskToFillForm =
                (Button) mEmptyFormLayout.findViewById(R.id.btnEmptyForm);
        mAskToFillForm.setOnClickListener(this);
        mPgb = (ProgressBar) mEmptyFormLayout.findViewById(R.id.pgbProgress);
        mSuccessText = (TextView) mEmptyFormLayout.findViewById(R.id.emptyFormSuccess);
        return root;
    }

    @Override
    protected AbstractFormListAdapter createFormAdapter(Context context) {
        return new UserFormListAdapter(context, false);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onGiftsClick() {
        Activity activity = getActivity();
        Intent intent = GiftsActivity.getSendGiftIntent(activity, getUserId());
        getParentFragment().startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
    }

    @Override
    public void setUserData(String status, int userId, LinkedList<FormItem> forms, Profile.Gifts gifts, int giftsCount) {
        super.setUserData(status, userId, forms, gifts, giftsCount);
        if (mEmptyFormLayout != null) {
            mEmptyFormLayout.setVisibility(getFormAdapter().isFormEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEmptyForm:
                int userId = getUserId();
                if (userId == 0) break;
                StandardMessageSendRequest request = new StandardMessageSendRequest(getActivity()
                        , StandardMessageSendRequest.MESSAGE_FILL_INTERESTS, userId, App.from(getActivity()).getOptions().blockUnconfirmed);
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
