package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.topface.topface.App;
import com.topface.topface.utils.config.SessionConfig;

import java.util.Arrays;

/**
 * Class that starts Facebook authorization
 */
public class FbAuthorizer extends Authorizer {

    private String[] FB_PERMISSIONS = {"user_photos", "email", "offline_access"};

    private UiLifecycleHelper mUiHelper;
    private Session.StatusCallback mStatusCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, SessionState state, Exception exception) {
            if (state.equals(SessionState.OPENED)) {
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        Intent intent = new Intent(AUTH_TOKEN_READY_ACTION);
                        if (user != null && AuthToken.getInstance().isEmpty()) {
                            AuthToken.getInstance().saveToken(
                                    AuthToken.SN_FACEBOOK,
                                    user.getId(),
                                    session.getAccessToken(),
                                    session.getExpirationDate().toString()
                            );

                            String name = user.getFirstName() + " " + user.getLastName();
                            SessionConfig sessionConfig = App.getSessionConfig();
                            sessionConfig.setSocialAccountName(name);
                            sessionConfig.saveConfig();

                            intent.putExtra(TOKEN_STATUS, TOKEN_READY);

                        } else {
                            intent.putExtra(TOKEN_STATUS, TOKEN_NOT_READY);
                        }
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                    }
                });
                request.executeAsync();
            }
        }
    };

    public FbAuthorizer(Activity activity) {
        super(activity);
        mUiHelper = new UiLifecycleHelper(activity, mStatusCallback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mUiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUiHelper.onDestroy();
    }

    @Override
    public void authorize() {
        Session.openActiveSession(getActivity(), true, Arrays.asList(FB_PERMISSIONS), mStatusCallback);
    }

    @Override
    public void logout() {
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean refreshToken() {
        return super.refreshToken();
    }
}
