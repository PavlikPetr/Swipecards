package com.topface.topface.utils.http;

import android.content.Context;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Ssid;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.requests.RequestBuilder;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

/**
 * Manages requests with added authorization request. Saves new auth data and
 * runs pending requests in ConnectionManager.
 */
public class AuthAssistant {

    private DataApiHandler mAuthHandler = new DataApiHandler<Auth>() {

        @Override
        protected void success(Auth data, IApiResponse response) {
            //Сохраняем новые авторизационные данные
            AuthorizationManager.saveAuthInfo(data);
            //Снимаем блокировку
            ConnectionManager.getInstance().getAuthUpdateFlag().set(false);
            //После этого выполняем все отложенные запросы
            ConnectionManager.getInstance().runPendingRequests();
        }

        @Override
        protected Auth parseResponse(ApiResponse response) {
            return new Auth(response);
        }

        @Override
        public void fail(int codeError, IApiResponse response) {
            if (response.isWrongAuthError()) {
                startReauth();
            }
        }

        @Override
        public void always(IApiResponse response) {
            super.always(response);
            ConnectionManager.getInstance().getAuthUpdateFlag().set(false);

        }

        @Override
        public void cancel() {
            super.cancel();
            ConnectionManager.getInstance().getAuthUpdateFlag().set(false);
        }
    };

    IApiRequest precedeRequestWithAuth(IApiRequest request) {
        if (isAuthUnacceptable(request)) {
            return request;
        }
        if (!request.containsAuth()) {
            Context context = request.getContext();
            AuthRequest authRequest = new AuthRequest(AuthToken.getInstance().getTokenInfo(), context);

            String oldRequestId = request.getId();
            request = new RequestBuilder(context).
                    firstRequest(authRequest, mAuthHandler).request(request).build();
            Debug.log("Request's id changed from " + oldRequestId + " to " + request.getId() +
                    " because of adding authorization subrequest");
            request.setEmptyHandler();
        }
        return request;
    }

    public IApiRequest createAuthRequest() {
        return new AuthRequest(AuthToken.getInstance().getTokenInfo(), App.getContext()).callback(mAuthHandler);
    }

    public static boolean isAuthUnacceptable(IApiRequest request) {
        return request instanceof AuthRequest || request instanceof PhotoAddRequest;
    }

    public boolean checkAuthError(IApiResponse response) {
        boolean result = false;
        //Эти ошибки могут возникать, если это запрос авторизации
        // или когда наши регистрационные данные устарели (сменился токен, пароль и т.п)
        if (response.isWrongAuthError()) {
            startReauth();

            //Изначальный же запрос отменяем, нам не нужно что бы он обрабатывался дальше
            result = true;
        } else {
            Ssid.update();
        }

        return result;
    }

    private void startReauth() {
        Ssid.remove();
        AuthToken.getInstance().removeToken();
        ConnectionManager.getInstance().sendBroadcastReauth(App.getContext());
    }
}
