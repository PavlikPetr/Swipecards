package com.topface.topface.ui;

import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;

public interface IEmailConfirmationListener {
    void onEmailConfirmed(boolean isConfirmed);

    void onSuccess(Profile data, IApiResponse response);

    void fail(int codeError, IApiResponse response);

    void always(IApiResponse response);
}