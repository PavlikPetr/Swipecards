package com.topface.topface.utils.controllers.startactions;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.InviteFacebookFriendsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.InviteUniqueStatistics;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;

public class FacebookRequestWindowAction extends DailyPopupAction  {

    private int mPriority;
    private Options.FacebookInviteFriends mFasebookRequests;
    private OnNextActionListener mOnNextActionListener;

    public FacebookRequestWindowAction(Context context, int priority) {
        super(context);
        mPriority = priority;
        mFasebookRequests = getOptions().facebookInviteFriends;
    }

    @Override
    protected boolean firstStartShow() {
        getUserConfig().setFacebookRequestWindowShow(System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean isApplicable() {
        return loginOrNotLogin() && isFacebook() && isValidFacebookRequestWindowSkip()
                && isTimeoutEnded(mFasebookRequests.minDelay,
                getUserConfig().getFacebookRequestWondowShow());
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }

    @Override
    public void callInBackground() {
        getUserConfig().setFacebookRequestWindowShow(System.currentTimeMillis());
        getUserConfig().saveConfig();
    }

    @Override
    public void callOnUi() {
        buildRequestWindow();
    }

    private void buildRequestWindow() {
        Session session;
        session = Session.getActiveSession();
        if (session == null) {
            session = Session.openActiveSessionFromCache(getContext());
        }
        Bundle params = new Bundle();
        params.putString("message", getContext().getResources().getString(R.string.go_tf));
        WebDialog.RequestsDialogBuilder dialogBuilder =
                new WebDialog.RequestsDialogBuilder(getContext(), session, params);
        dialogBuilder.setOnCompleteListener(new WebDialog.OnCompleteListener() {
            @Override
            public void onComplete(Bundle bundle, FacebookException e) {
                if (e instanceof FacebookOperationCanceledException) {
                    if (mOnNextActionListener != null) {
                        mOnNextActionListener.onNextAction();
                    }
                    getUserConfig().setFacebookRequestSkip(getUserConfig().getFacebookRequestSkip() + 1);
                } else {
                    //в bundle id запроса и id пользователей которым были посланы реквесты.
                    InviteUniqueStatistics.sendFacebookInvites(bundle.keySet().size() - 1);
                    getUserConfig().setFacebookRequestSkip(mFasebookRequests.maxAttempts + 1);
                    sendFacebooknInvitesRequest(getFriendsId(bundle));
                }
            }
        });
        dialogBuilder.build().show();
    }

    private void sendFacebooknInvitesRequest(ArrayList<String> friendsId) {
        InviteFacebookFriendsRequest request = new InviteFacebookFriendsRequest(getContext(),friendsId);
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                Balance balance = JsonUtils.fromJson(response.getBalance().toString(),Balance.class);
                if(balance.premium){
                    App.sendProfileAndOptionsRequests(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), Utils.getQuantityString(R.plurals.vip_status_period, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    });
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {

            }
        }).exec();
    }

    private ArrayList<String> getFriendsId(Bundle bundle){
        ArrayList<String> userIds = new ArrayList<>();
        for (String key : bundle.keySet()) {
            if(key.equals("request")){
                continue;
            }
            userIds.add((String) bundle.get(key));
        }
        return userIds;
    }

    private boolean isFacebook() {
        boolean b = AuthToken.getInstance().getSocialNet()
                .equals(AuthToken.SN_FACEBOOK);
        return AuthToken.getInstance().getSocialNet()
                .equals(AuthToken.SN_FACEBOOK);
    }

    private boolean isValidFacebookRequestWindowSkip() {
        boolean b = getUserConfig().getFacebookRequestSkip()
                <= CacheProfile.getOptions().facebookInviteFriends.maxAttempts;
        return getUserConfig().getFacebookRequestSkip()
                <= CacheProfile.getOptions().facebookInviteFriends.maxAttempts;
    }

    private boolean loginOrNotLogin() {
        boolean b = CacheProfile.getOptions().facebookInviteFriends.enabledAttempts
                || CacheProfile.getOptions().facebookInviteFriends.enabledOnLogin;
        return CacheProfile.getOptions().facebookInviteFriends.enabledAttempts
                || CacheProfile.getOptions().facebookInviteFriends.enabledOnLogin;
    }

    @Override
    public String toString() {
        return getActionName() + Static.SEMICOLON +
                getPriority() + Static.SEMICOLON +
                isApplicable();
    }

    private static class Balance{
        public int money;
        public boolean premium;
        public int likes;
    }

}
