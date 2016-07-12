package com.topface.topface.ui.fragments.profile.photoswitcher;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.views.RetryViewCreator;

public class UserProfileLoader {
    private int mLastLoadedProfileId;
    private ApiResponse mLastResponse;
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;
    private View mLoaderView;
    private IUserProfileReceiver mReceiver = null;
    private int mProfileId;
    private UserRequest mUserRequest;

    public UserProfileLoader(RelativeLayout lockScreen, View loaderView, IUserProfileReceiver receiver, final int profileId) {
        mLockScreen = lockScreen;
        mLoaderView = loaderView;
        mReceiver = receiver;
        mProfileId = profileId;
        mRetryView = new RetryViewCreator.Builder(App.getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserProfile(App.getContext());
            }
        }).build();
        mLockScreen.addView(mRetryView.getView());
    }

    private boolean isLoaded(int profileId) {
        return profileId == mLastLoadedProfileId;
    }

    public void loadUserProfile(Context context) {
        if (isLoaded(mProfileId)) return;
        mLockScreen.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.VISIBLE);
        mUserRequest = new UserRequest(mProfileId, context);
        mUserRequest.callback(new DataApiHandler<User>() {

            @Override
            protected void success(User user, IApiResponse response) {
                mLastLoadedProfileId = mProfileId;
                if (user != null) {
                    mLastResponse = (ApiResponse) response;
                }
                if (user == null) {
                    showRetryBtn();
                } else if (user.banned) {
                    showForBanned();
                } else if (user.deleted) {
                    showForDeleted();
                } else {
                    mLoaderView.setVisibility(View.INVISIBLE);
                    setProfile(user);
                }
            }

            @Override
            protected User parseResponse(ApiResponse response) {
                return new User(mProfileId, response, getContext());
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                if (response.isCodeEqual(ErrorCodes.INCORRECT_VALUE, ErrorCodes.USER_NOT_FOUND)) {
                    showForNotExisting();
                } else {
                    showRetryBtn();
                }
            }
        }).exec();

    }

    public ApiResponse getLastResponse() {
        return mLastResponse;
    }

    private void setProfile(User user) {
        if (mReceiver != null) {
            mReceiver.onReceiveUserProfile(user);
        }
    }

    private void showLockWithText(String text, boolean onlyMessage) {
        if (mRetryView != null) {
            mLoaderView.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.VISIBLE);
            mRetryView.setText(text);
            mRetryView.showRetryButton(!onlyMessage);
        }
    }

    private void showLockWithText(String text) {
        showLockWithText(text, true);
    }

    private void showForBanned() {
        showLockWithText(App.getContext().getString(R.string.user_baned));
    }

    private void showRetryBtn() {
        showLockWithText(App.getContext().getString(R.string.general_profile_error), false);
    }

    private void showForDeleted() {
        showLockWithText(App.getContext().getString(R.string.user_is_deleted));
    }

    private void showForNotExisting() {
        showLockWithText(App.getContext().getString(R.string.user_does_not_exist), true);
    }

    public void release() {
        if (mUserRequest != null) {
            mUserRequest.cancelFromUi();
        }
    }
}