package com.topface.topface.utils.http;

import android.content.Context;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages requests with added authorization request. Saves new auth data and
 * runs pending requests in ConnectionManager.
 */
public class AuthAssistant {

    private ConnectionManager mConnectionManager;
    private Set<String> mModifiedRequestsIds = Collections.synchronizedSet(new HashSet<String>());

    private DataApiHandler mAuthHandler = new DataApiHandler<Auth>() {

        @Override
        protected void success(Auth data, IApiResponse response) {
            //Сохраняем новые авторизационные данные
            AuthorizationManager.saveAuthInfo(data);
            //Снимаем блокировку
            mConnectionManager.getAuthUpdateFlag().set(false);
            //После этого выполняем все отложенные запросы
            mConnectionManager.runPendingRequests();
        }

        @Override
        protected Auth parseResponse(ApiResponse response) {
            return new Auth(response);
        }

        @Override
        public void fail(int codeError, IApiResponse response) {
            mConnectionManager.sendBroadcastReauth(getContext());
        }

        @Override
        public void always(IApiResponse response) {
            super.always(response);
            mConnectionManager.getAuthUpdateFlag().set(false);
        }

        @Override
        public void cancel() {
            super.cancel();
            mConnectionManager.getAuthUpdateFlag().set(false);
        }
    };

    public AuthAssistant(ConnectionManager connectionManager) {
        mConnectionManager = connectionManager;
    }

    IApiRequest precedeRequestWithAuth(IApiRequest request) {
        if (isAuthUnacceptable(request)) {
            return request;
        }
        if (!mModifiedRequestsIds.contains(request.getId())) {
            Context context = request.getContext();
            AuthRequest authRequest = new AuthRequest(AuthToken.getInstance().getTokenInfo(), context);

            String oldRequestId = request.getId();
            request = new RequestBuilder(context).
                    firstRequest(authRequest, mAuthHandler).request(request).build();
            Debug.log("Request's id changed from " + oldRequestId + " to " + request.getId() +
                    " because of adding authorization subrequest");
            request.setEmptyHandler();

            mModifiedRequestsIds.add(request.getId());
        }
        return request;
    }

    public IApiRequest explicitAuthRequest() {
        return new AuthRequest(AuthToken.getInstance().getTokenInfo(), App.getContext()).callback(mAuthHandler);
    }

    void forgetRequest(String id) {
        mModifiedRequestsIds.remove(id);
    }

    public static boolean isAuthUnacceptable(IApiRequest request) {
        return request instanceof AuthRequest || request instanceof PhotoAddRequest;
    }
}
