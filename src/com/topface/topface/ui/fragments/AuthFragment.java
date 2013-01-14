package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.Data;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.WebAuthActivity;

public class AuthFragment extends BaseFragment{

    private RetryView mRetryView;
    private Button mFBButton;
    private Button mVKButton;
    private ProgressBar mProgressBar;
    private AuthorizationManager mAuthorizationManager;
    private BroadcastReceiver connectionChangeListener;

    public static AuthFragment newInstance() {
        return new AuthFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ac_auth, null);
        initViews(root);
        initAuthorizationHandler();
        checkOnline();
        return root;
    }

    private void initViews(View root) {
        initButtons(root);
        initRetryView(root);
        initOtherViews(root);
    }

    private void initAuthorizationHandler () {
        mAuthorizationManager = AuthorizationManager.getInstance(getActivity());
        mAuthorizationManager.setOnAuthorizationHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AuthorizationManager.AUTHORIZATION_FAILED:
                        authorizationFailed(ApiResponse.NETWORK_CONNECT_ERROR);
                        break;
                    case AuthorizationManager.DIALOG_COMPLETED:
                        hideButtons();
                        break;
                    case AuthorizationManager.TOKEN_RECEIVED:
                        auth((AuthToken) msg.obj);
                        break;
                    case AuthorizationManager.AUTHORIZATION_CANCELLED:
                        showButtons();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });
    }

    private void initButtons(View root) {
        mVKButton = (Button) root.findViewById(R.id.btnAuthVK);
        mVKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnVKClick();
            }
        });

        mFBButton = (Button) root.findViewById(R.id.btnAuthFB);
        mFBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnFBClick();
            }
        });
    }

    private void initRetryView(View root) {
        mRetryView = new RetryView(getActivity().getApplicationContext());
        mRetryView.setErrorMsg(getString(R.string.general_data_error));
        mRetryView.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth(new AuthToken(getActivity().getApplicationContext()));
                mRetryView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
        mRetryView.setVisibility(View.GONE);

        RelativeLayout authContainer = (RelativeLayout) root.findViewById(R.id.authContainer);
        authContainer.addView(mRetryView);

        connectionChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mConnectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, -1);
//                reAuthAfterInternetConnected(mConnectionType);
            }
        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthorizationManager.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_CANCELED) {
            hideButtons();
//            showButtons();
        }
    }

    public void reAuthAfterInternetConnected(int type) {
        if(type != ConnectionChangeReceiver.CONNECTION_OFFLINE) {
            if(!(new AuthToken(getActivity()).isEmpty())) {
                mAuthorizationManager.reAuthorize();
                hideButtons();
                mRetryView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initOtherViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsAuthLoading);
    }

    private void checkOnline() {
        if (!App.isOnline()) {
            showNoInternetToast();
        }
    }

    private void showNoInternetToast() {
        Toast.makeText(getActivity(), getString(R.string.general_internet_off), Toast.LENGTH_SHORT)
                .show();
    }

    private void auth(AuthToken token) {
        AuthRequest authRequest = generateAuthRequest(token);
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                saveAuthInfo(response);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProfileRequest profileRequest = new ProfileRequest(getActivity());
                        profileRequest.part = ProfileRequest.P_ALL;
                        registerRequest(profileRequest);
                        profileRequest.callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
                                CacheProfile.setProfile(Profile.parse(response), response);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        OptionsRequest request = new OptionsRequest(getActivity());

                                        registerRequest(request);
                                        request.callback(new ApiHandler() {
                                            @Override
                                            public void success(final ApiResponse response) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Options.parse(response);
                                                        ((TrackedFragmentActivity) getActivity()).close(AuthFragment.this);
                                                    }
                                                });

                                            }

                                            @Override
                                            public void fail(int codeError, ApiResponse response) {
                                                Debug.log("fail");
                                            }
                                        }).exec();
                                    }
                                });
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) {
                            }
                        }).exec();
                    }
                });
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        authorizationFailed(codeError);
                    }
                });
            }

            @Override
            public void cancel() {
                showButtons();
            }
        }).exec();
    }

    private AuthRequest generateAuthRequest(AuthToken token) {
        AuthRequest authRequest = new AuthRequest(getActivity());
        String socialNet = token.getSocialNet();
        registerRequest(authRequest);
        authRequest.platform = socialNet;
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();

        EasyTracker.getTracker().trackEvent("Profile", "Auth", "FromActivity" + socialNet, 1L);

        return authRequest;
    }

    private void saveAuthInfo(ApiResponse response) {
        Auth auth = Auth.parse(response);
        Data.saveSSID(getActivity().getApplicationContext(), auth.ssid);
        GCMUtils.init(getActivity());

    }

    private void authorizationFailed(int codeError) {
        hideButtons();
        switch (codeError) {
            case ApiResponse.NETWORK_CONNECT_ERROR:
                mRetryView.setErrorMsg(getString(R.string.general_reconnect_social));
                break;
            case ApiResponse.MAINTENANCE:
                mRetryView.setErrorMsg(getString(R.string.general_maintenance));
                break;
            default:
                mRetryView.setErrorMsg(getString(R.string.general_data_error));
                break;
        }
        mRetryView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void showButtons() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Эта проверка нужна, для безопасной работы в
                if (mFBButton != null && mVKButton != null && mProgressBar != null) {
                    mFBButton.setVisibility(View.VISIBLE);
                    mVKButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mRetryView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void hideButtons() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFBButton.setVisibility(View.GONE);
                mVKButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mRetryView.setVisibility(View.GONE);
            }
        });
    }

    private void btnVKClick() {
        Intent intent = new Intent(getActivity(), WebAuthActivity.class);
        startActivityForResult(intent, WebAuthActivity.INTENT_WEB_AUTH);
//        mAuthorizationManager.vkontakteAuth();
    }



    private void btnFBClick() {
        mAuthorizationManager.facebookAuth();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(connectionChangeListener, new IntentFilter(ConnectionChangeReceiver.REAUTH));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(connectionChangeListener);
    }
}
