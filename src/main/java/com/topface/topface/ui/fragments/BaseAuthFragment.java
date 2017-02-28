package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.topface.framework.utils.Debug;
import com.topface.statistics.android.Slices;
import com.topface.statistics.generated.FBInvitesStatisticsGeneratedStatistics;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.AuthTokenStateData;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.state.AuthState;
import com.topface.topface.statistics.AuthStatistics;
import com.topface.topface.ui.dialogs.OldVersionDialog;
import com.topface.topface.ui.external_libs.kochava.KochavaManager;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.ui.external_libs.AdjustManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FBInvitesUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import org.json.JSONException;

/**
 * Base authorization logic
 */
public abstract class BaseAuthFragment extends BaseFragment {

    private AdjustManager mAdjustManager;
    private KochavaManager mKochavaManager;
    private AuthState mAuthState;
    private boolean mHasAuthorized = false;
    private RetryViewCreator mRetryView;
    private BroadcastReceiver mConnectionChangeListener;
    private EditText mPassword;

    @Override
    public void onResume() {
        super.onResume();
        if (!AuthToken.getInstance().isEmpty() && Ssid.isLoaded()) {
            //Если мы попали на этот фрагмент с работающей авторизацией, то просто перезапрашиваем профиль
            loadAllProfileData();
        }
        checkOnline();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mConnectionChangeListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuthState = App.getAppComponent().authState();
        mAdjustManager = App.getAppComponent().adjustManager();
    }

    protected void initViews(View root) {
        mRetryView = initRetryView(root);
    }

    protected RetryViewCreator initRetryView(View root) {
        final RetryViewCreator retryView = new RetryViewCreator.Builder(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // инициализация обработчика происходит в методе authorizationFailed()
            }
        }).build();
        retryView.setVisibility(View.GONE);

        ViewGroup rootLayout = (ViewGroup) root.findViewById(getRootId());
        rootLayout.addView(retryView.getView());
        mPassword = (EditText) root.findViewById(R.id.edPassword);

        mConnectionChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mConnectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, -1);
                if (ConnectionChangeReceiver.ConnectionType.valueOf(mConnectionType) != ConnectionChangeReceiver.ConnectionType.CONNECTION_OFFLINE) {
                    if (retryView != null) {
                        retryView.performClick();
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mConnectionChangeListener,
                new IntentFilter(ConnectionChangeReceiver.REAUTH));

        return retryView;
    }

    protected int getRootId() {
        return R.id.authContainer;
    }

    protected boolean checkOnline() {
        if (!App.isOnline()) {
            showNoInternetToast();
            return false;
        }
        return true;
    }

    private void showNoInternetToast() {
        Utils.showToastNotification(R.string.general_internet_off, Toast.LENGTH_SHORT);
    }

    protected void auth(final AuthToken token) {
        if (mAdjustManager == null) {
            mAdjustManager = App.getAppComponent().adjustManager();
        }
        if (mKochavaManager == null) {
            mKochavaManager = App.getAppComponent().kochavaManager();
        }
        EasyTracker.sendEvent("Profile", "Auth", "FromActivity" + token.getSocialNet(), 1L);
        showProgress();
        final AuthRequest authRequest = new AuthRequest(token.getTokenInfo(), getActivity());
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                AuthToken.getInstance().writeTokenInPreferences();
                AuthorizationManager.saveAuthInfo(response);
                App.getConfig().onAuthTokenReceived();
                loadAllProfileData();
                onSuccessAuthorization(token);
                mHasAuthorized = true;
                AppConfig appConfig = App.getAppConfig();
                App.sendAdjustAttributeData(appConfig.getAdjustAttributeData());
                App.sendReferrerTrack(appConfig.getReferrerTrackData());
                mAdjustManager.sendRegistrationEvent(token.getSocialNet());
                mKochavaManager.registration();
                mKochavaManager.sendReferralTrack();
                //Отправляем статистику в AppsFlyer
                try {
                    AppsFlyerLib.sendTrackingWithEvent(App.getContext(), App.getContext()
                            .getResources().getString(R.string.appsflyer_registration), "");
                } catch (Exception e) {
                    Debug.error("AppsFlyer Exception", e);
                }
                String authStatus = null;
                try {
                    authStatus = response.getJsonResult().getString("authStatus");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (authStatus != null) {
                    sendFirstAuthUser(authRequest.getPlatform(), authStatus);
                }
                mAuthState.setData(new AuthTokenStateData(AuthTokenStateData.TOKEN_AUTHORIZED));
                String appLink = FBInvitesUtils.INSTANCE.getAppLinkToSend();
                if (!TextUtils.isEmpty(appLink)) {
                    if (authStatus != null) {
                        Slices slice = new Slices().putSlice("val", appLink);
                        if (authStatus.equals("regular")) {
                            FBInvitesStatisticsGeneratedStatistics.sendNow_FB_INVITE_AUTHORIZE(slice);
                        } else if (authStatus.equals("created")) {
                            FBInvitesStatisticsGeneratedStatistics.sendNow_FB_INVITE_REGISTER(slice);
                        }
                    }
                    FBInvitesUtils.INSTANCE.AppLinkSended();
                }
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                mAuthState.setData(new AuthTokenStateData(AuthTokenStateData.TOKEN_NOT_READY));
                authorizationFailed(codeError, authRequest);

            }

            public void always(IApiResponse response) {
            }
        }).exec();
    }

    private void sendFirstAuthUser(String platform, String authStatus) {
        AppConfig appConfig = App.getAppConfig();
        if (appConfig.isFirstAuth()) {
            AuthStatistics.sendFirstAuth(platform, authStatus);
            appConfig.setFirstAuth();
            appConfig.saveConfig();
        }
    }

    protected void loadAllProfileData() {
        hideButtons();
        showProgress();
        App.sendProfileAndOptionsRequests(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                //После авторизации обязательно бросаем события, что бы профиль загрузился
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(CacheProfile.ACTION_PROFILE_LOAD));
                onOptionsAndProfileSuccess();
//                new FindAndSendCurrentLocation();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (isAdded() && response.isCodeEqual(ErrorCodes.BAN)) {
                    showButtons();
                } else {
                    authorizationFailed(codeError, null);
                }
            }
        });
    }

    protected abstract void onOptionsAndProfileSuccess();

    protected void authorizationFailed(int codeError, final ApiRequest request) {
        if (!isAdded()) {
            return;
        }
        processAuthError(codeError, request);
        if (whetherToShowRetrier(codeError)) {
            hideButtons();
            showRetrier();
            if (mPassword != null) {
                mPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            hideProgress();
        } else {
            showButtons();
        }
    }

    protected void processAuthError(int codeError, ApiRequest request) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(RetryViewCreator.REFRESH_TEMPLATE).append(getString(R.string.general_dialog_retry));

        switch (codeError) {
            case ErrorCodes.NETWORK_CONNECT_ERROR:
                showRetryView(getString(R.string.general_reconnect_social), strBuilder.toString(), request);
                break;
            case ErrorCodes.MAINTENANCE:
                showRetryView(getString(R.string.general_maintenance), strBuilder.toString(), request);
                break;
            case ErrorCodes.CODE_OLD_APPLICATION_VERSION:
                if (isAdded()) {
                    OldVersionDialog.newInstance(true).show(getActivity().getSupportFragmentManager(), OldVersionDialog.class.getName());
                }
                break;
            default:
                showRetryView(getString(R.string.general_data_error), strBuilder.toString(), request);
                break;
        }
    }

    protected boolean whetherToShowRetrier(int codeError) {
        switch (codeError) {
            case ErrorCodes.INCORRECT_LOGIN:
            case ErrorCodes.UNKNOWN_SOCIAL_USER:
            case ErrorCodes.INCORRECT_PASSWORD:
            case ErrorCodes.MISSING_REQUIRE_PARAMETER:
            case ErrorCodes.USER_DELETED:
                return false;
            default:
                return true;
        }
    }

    protected void fillRetryView(String text, View.OnClickListener listener, String btnText) {
        mRetryView.setText(text);
        mRetryView.setButtonText(btnText);
        mRetryView.setListener(listener);
    }

    protected void showRetryView(String text, String btnText, final ApiRequest request) {
        fillRetryView(text, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRetrier();
                showProgress();
                resendRequest(request);
            }
        }, btnText);
    }

    private void resendRequest(ApiRequest request) {
        if (request != null) {
            request.canceled = false;
            request.resetResendCounter();
            request.exec();
        } else {
            //Если запрос базовой информации не прошел, то повторяем запрос
            loadAllProfileData();
        }
    }

    protected boolean hasAuthorized() {
        return mHasAuthorized;
    }

    protected void hideRetrier() {
        mRetryView.getView().setVisibility(View.GONE);
    }

    protected void showRetrier() {
        mRetryView.getView().setVisibility(View.VISIBLE);
    }

    protected abstract void showButtons();

    protected abstract void hideButtons();

    protected abstract void showProgress();

    protected abstract void hideProgress();

    protected abstract void onSuccessAuthorization(AuthToken token);
}
