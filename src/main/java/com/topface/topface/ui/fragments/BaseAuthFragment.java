package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

/**
 * Base authorization logic
 */
public abstract class BaseAuthFragment extends BaseFragment {

    private boolean mHasAuthorized = false;
    private RetryViewCreator mRetryView;
    private BroadcastReceiver mConnectionChangeListener;

    @Override
    public void onResume() {
        super.onResume();
        if (!AuthToken.getInstance().isEmpty()) {
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
        Toast.makeText(App.getContext(), R.string.general_internet_off, Toast.LENGTH_SHORT)
                .show();
    }

    protected void auth(final AuthToken token) {
        EasyTracker.sendEvent("Profile", "Auth", "FromActivity" + token.getSocialNet(), 1L);
        App.getConfig().onAuthTokenReceived();
        showProgress();
        final AuthRequest authRequest = new AuthRequest(token.getTokenInfo(), getActivity());
        authRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                AuthorizationManager.saveAuthInfo(response);
                loadAllProfileData();
                onSuccessAuthorization(token);
                mHasAuthorized = true;
                //Отправляем статистику в AppsFlyer
                try {
                    AppsFlyerLib.sendTrackingWithEvent(App.getContext(), "registration", "");
                } catch (Exception e) {
                    Debug.error("AppsFlyer Exception", e);
                }
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                authorizationFailed(codeError, authRequest);
            }

            public void always(IApiResponse response) {
                if (isAdded() && response.getResultCode() == ErrorCodes.HTTPS_CERTIFICATE_EXPIRED) {
                    hideBackButton();
                }
            }
        }).exec();
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

        hideButtons();
        processAuthError(codeError, request);

        if (whetherToShowRetrier(codeError)) {
            showRetrier();
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
                fillRetryView(getString(R.string.general_version_not_supported), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.goToMarket(getActivity());
                    }
                }, getString(R.string.popup_version_update));
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

    protected abstract void hideBackButton();

    protected abstract void onSuccessAuthorization(AuthToken token);
}
