package com.topface.topface.ui;

import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;

/**
 * Created by ppavlik on 03.06.16.
 * Simplified email confirmation interface
 */

public abstract class SimpleEmailConfirmationListener implements IEmailConfirmationListener {

    public abstract void onEmailConfirmed(boolean isConfirmed);

    public void onSuccess(Profile data, IApiResponse response) {

    }

    public void fail(int codeError, IApiResponse response) {

    }

    public void always(IApiResponse response) {

    }
}
