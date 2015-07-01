package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.utils.config.SessionConfig;

import java.util.Arrays;

/**
 * Class that starts Facebook authorization
 */
public class FbAuthorizer extends Authorizer {

    public static final String[] PERMISSIONS = new String[]{"email", "public_profile", "user_friends"};
    private UiLifecycleHelper mUiHelper;
    private Request mRequest;
    private Session.StatusCallback mStatusCallback;

    private Request getRequest(final Session session, final Intent intent) {
        mRequest = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        if (AuthToken.getInstance().isEmpty()) {
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
                        }
                    } else if (AuthToken.getInstance().isEmpty()) {
                        intent.putExtra(TOKEN_STATUS, TOKEN_FAILED);
                    }
                    broadcastAuthTokenStatus(intent);
                }
            });
        return mRequest;
    }

    private Session.StatusCallback getStatusCallback() {
        if (mStatusCallback == null) {
            mStatusCallback = new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    Intent intent = new Intent(AUTH_TOKEN_READY_ACTION);

                    switch (state) {
                        case OPENED:
                        case OPENED_TOKEN_UPDATED:
                            getRequest(session, intent).executeAsync();
                            break;
                        case CREATED:
                        case OPENING:
                        case CREATED_TOKEN_LOADED:
                            intent.putExtra(TOKEN_STATUS, TOKEN_PREPARING);
                            break;
                        case CLOSED_LOGIN_FAILED:
                            if (exception instanceof FacebookOperationCanceledException) {
                                intent.putExtra(TOKEN_STATUS, TOKEN_NOT_READY);
                            } else {
                                intent.putExtra(TOKEN_STATUS, TOKEN_FAILED);
                            }
                            break;
                        default:
                            intent.putExtra(TOKEN_STATUS, TOKEN_NOT_READY);
                            break;
                    }

                    broadcastAuthTokenStatus(intent);
                }
            };
        }
        return mStatusCallback;
    }

    public FbAuthorizer(Activity activity) {
        super(activity);
        mUiHelper = new UiLifecycleHelper(activity, null);
        mUiHelper.onStop();
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
        mStatusCallback = null;
        mRequest = null;
    }

    @Override
    public void authorize() {
        Session session = Session.getActiveSession();
        if (session != null && !session.isOpened() && !session.isClosed()) {
            session.openForRead(
                    new Session.OpenRequest(getActivity())
                            .setPermissions(PERMISSIONS)
                            .setCallback(getStatusCallback())
            );
        } else {
            Session.OpenRequest req = (new Session.OpenRequest(getActivity())).setCallback(getStatusCallback());
            if (App.getAppConfig().getStageChecked()) {
                session = (new Session.Builder(getActivity())).setApplicationId(App.getAppSocialAppsIds().fbId).build();    
            } else {
                session = (new Session.Builder(getActivity())).setApplicationId(Static.STAGE_AUTH_FACEBOOK_ID).build();
            }
            
            Session.setActiveSession(session);
            session.openForRead(req.setPermissions(PERMISSIONS));
        }
    }

    @Override
    public void logout() {
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
            if (mStatusCallback != null) {
                session.removeCallback(mStatusCallback);
            }
        }
        Session.setActiveSession(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data);
    }
}
