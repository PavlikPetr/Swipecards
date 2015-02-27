package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.os.Bundle;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.social.AuthToken;

public class FacebookRequestWindowAction extends DailyPopupAction {

    private Options.FasebookRequests mFasebookRequests;

    public FacebookRequestWindowAction(Context context, int priority) {
        super(context, priority);
        mFasebookRequests = mOptions.fasebookRequests;
    }

    @Override
    protected boolean firstStartShow() {
        App.getUserConfig().setFacebookRequestWindowShow(System.currentTimeMillis());
        return false;
    }

    @Override
    public boolean isApplicable() {
        return loginOrNotLogin() && isFacebook() && isValidFacebookRequestWindowSkip()
                && isTimeoutEnded(mFasebookRequests.fasebookRequestsTimeout);
    }

    @Override
    public void callInBackground() {
        App.getUserConfig().setFacebookRequestWindowShow(System.currentTimeMillis());
        App.getUserConfig().saveConfig();
    }

    @Override
    public void callOnUi() {
        buildRequestWindow();
    }

    private void buildRequestWindow() {
        Session session;
        session = Session.getActiveSession();
        if (session == null) {
            session = Session.openActiveSessionFromCache(mContext);
        }
        Bundle params = new Bundle();
        params.putString("message", mContext.getResources().getString(R.string.go_tf));
        WebDialog.RequestsDialogBuilder dialogBuilder =
                new WebDialog.RequestsDialogBuilder(mContext, session, params);
        dialogBuilder.setOnCompleteListener(new WebDialog.OnCompleteListener() {
            @Override
            public void onComplete(Bundle bundle, FacebookException e) {
                if (e instanceof FacebookOperationCanceledException) {
                    App.getUserConfig().setFacebookRequestSkip(App.getUserConfig().getFacebookRequestSkip() + 1);
                } else {
                    App.getUserConfig().setFacebookRequestSkip(mFasebookRequests.maxAttempts + 1);
                }
            }
        });
        dialogBuilder.build().show();
    }

    private boolean isFacebook() {
        String s = AuthToken.getInstance().getSocialNet();
        return AuthToken.getInstance().getSocialNet()
                .equals(AuthToken.SN_FACEBOOK);
    }

    private boolean isValidFacebookRequestWindowSkip() {
        return App.getUserConfig().getFacebookRequestSkip()
                <= mFasebookRequests.maxAttempts;
    }

    private boolean loginOrNotLogin() {
        return mFasebookRequests.enabledAttempts
                || mFasebookRequests.enabledRequestsOnLogin;
    }

}
