package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;

import com.topface.topface.ui.TopfaceAuthActivity;

/**
 * Class that starts Topface authorization
 */
public class TfAuthorizer extends Authorizer {
    public TfAuthorizer(Activity activity) {
        super(activity);
    }

    @Override
    public void authorize() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, TopfaceAuthActivity.class);
        activity.startActivityForResult(intent, TopfaceAuthActivity.INTENT_TOPFACE_AUTH);
    }

    @Override
    public void logout() {
        AuthToken.getInstance().removeToken();
    }


}
