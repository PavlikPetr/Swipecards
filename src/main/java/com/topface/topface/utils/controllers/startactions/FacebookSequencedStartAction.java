package com.topface.topface.utils.controllers.startactions;

import android.app.Activity;

import com.topface.topface.utils.controllers.SequencedStartAction;
import com.topface.topface.utils.social.AuthToken;

public class FacebookSequencedStartAction extends SequencedStartAction {


    public FacebookSequencedStartAction(Activity activity, int priority) {
        super(activity, priority);
    }

    @Override
    public boolean isApplicable() {
        for (IStartAction startAction : getActions()) {
            if (isFacebookAction(startAction.getActionName())) {
                return startAction.isApplicable();
            } else if (!isFacebook() && isInviteAction(startAction.getActionName())) {
                return startAction.isApplicable();
            }
        }
        return false;
    }

    private boolean isInviteAction(String actionName) {
        return actionName.equals(InvitePopupAction.class.getSimpleName());
    }

    private boolean isFacebookAction(String actionName) {
        return isFacebook() &&
                actionName.equals(FacebookRequestWindowAction.class.getSimpleName());
    }

    private boolean isFacebook() {
        return AuthToken.getInstance().getSocialNet()
                .equals(AuthToken.SN_FACEBOOK);
    }

}
