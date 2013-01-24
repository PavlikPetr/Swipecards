package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.views.IllustratedTextView;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class AuthFragment extends BaseFragment {

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

    private void initAuthorizationHandler() {
        mAuthorizationManager = AuthorizationManager.getInstance(getActivity());
        mAuthorizationManager.setOnAuthorizationHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AuthorizationManager.AUTHORIZATION_FAILED:
                        authorizationFailed(ApiResponse.NETWORK_CONNECT_ERROR, null);
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
        mRetryView = new RetryView(getActivity().getApplicationContext(), R.id.ivAuthLogo);
        mRetryView.setErrorMsg(getString(R.string.general_data_error));
        mRetryView.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mRetryView.setVisibility(View.GONE);

        RelativeLayout authContainer = (RelativeLayout) root.findViewById(R.id.authContainer);
        authContainer.addView(mRetryView);

        connectionChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mConnectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, -1);
                if (mConnectionType != ConnectionChangeReceiver.CONNECTION_OFFLINE) {
                    IllustratedTextView btn = mRetryView.getBtn1();
                    if (btn != null) {
                        btn.performClick();
                    }
                }
            }
        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthorizationManager.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            showButtons();
        }
    }

    private void initOtherViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsAuthLoading);
    }

    private boolean checkOnline() {
        if (!App.isOnline()) {
            showNoInternetToast();
            return false;
        }
        return true;
    }

    private void showNoInternetToast() {
        Toast.makeText(getActivity(), getString(R.string.general_internet_off), Toast.LENGTH_SHORT)
                .show();
    }

    private void auth(AuthToken token) {
        final AuthRequest authRequest = generateAuthRequest(token);
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                saveAuthInfo(response);
                getProfileAndOptions();
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {

                authorizationFailed(codeError, authRequest);
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

    private void getProfileAndOptions() {
        final ProfileRequest profileRequest = new ProfileRequest(getActivity());
        profileRequest.part = ProfileRequest.P_ALL;
        registerRequest(profileRequest);
        profileRequest.callback(new DataApiHandler<Profile>() {

            @Override
            protected void success(Profile data, ApiResponse response) {
                CacheProfile.setProfile(data, response);
                getOptions();
            }

            @Override
            protected Profile parseResponse(ApiResponse response) {
                return Profile.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (response.code == ApiResponse.BAN)
                    showButtons();
                else {
                    profileRequest.handler = this;
                    authorizationFailed(codeError, profileRequest);
                    Toast.makeText(getActivity(), getString(R.string.general_data_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).exec();
    }

    private void getOptions() {
        final OptionsRequest request = new OptionsRequest(getActivity());
        registerRequest(request);
        request.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                Options.parse(response);
                ((BaseFragmentActivity) getActivity()).close(AuthFragment.this);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (response.code == ApiResponse.BAN)
                    showButtons();
                else {
                    request.callback(this);
                    authorizationFailed(codeError, request);
                    Toast.makeText(getActivity(), getString(R.string.general_data_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).exec();
    }

    private void authorizationFailed(int codeError, final ApiRequest request) {
        hideButtons();
        switch (codeError) {
            case ApiResponse.NETWORK_CONNECT_ERROR:
                mRetryView.setErrorMsg(getString(R.string.general_reconnect_social));
                mRetryView.setTextToButton1(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRetryView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        request.canceled = false;
                        registerRequest(request);
                        request.exec();
                    }
                });
                break;
            case ApiResponse.MAINTENANCE:
                mRetryView.setErrorMsg(getString(R.string.general_maintenance));
                mRetryView.setTextToButton1(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRetryView.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        request.canceled = false;
                        registerRequest(request);
                        request.exec();
                    }
                });
                break;
            case ApiResponse.CODE_OLD_APPLICATION_VERSION:
                mRetryView.setErrorMsg(getString(R.string.general_version_not_supported));
                mRetryView.setTextToButton1(getString(R.string.popup_version_update));
                mRetryView.setListenerToBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.goToMarket(getActivity());
                    }
                });
                break;
            default:
                mRetryView.setErrorMsg(getString(R.string.general_data_error));
                break;
        }

        if (request != null) {
            mRetryView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);


        } else {
            showButtons();
        }
    }

    private void showButtons() {
        //Эта проверка нужна, для безопасной работы в
        if (mFBButton != null && mVKButton != null && mProgressBar != null) {
            mFBButton.setVisibility(View.VISIBLE);
            mVKButton.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mRetryView.setVisibility(View.GONE);
        }
    }

    private void hideButtons() {
        mFBButton.setVisibility(View.GONE);
        mVKButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRetryView.setVisibility(View.GONE);
    }

    private void btnVKClick() {
        if (checkOnline()) {
            mAuthorizationManager.vkontakteAuth();
        }
//
    }


    private void btnFBClick() {
        if (checkOnline()) {
            mAuthorizationManager.facebookAuth();
        }
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
