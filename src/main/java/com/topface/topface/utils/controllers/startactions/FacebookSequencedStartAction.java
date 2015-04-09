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
            }
        }
        return false;
    }

    private boolean isFacebookAction(String actionName) {
        return AuthToken.getInstance().getSocialNet()
                .equals(AuthToken.SN_FACEBOOK) &&
                actionName.equals(FacebookRequestWindowAction.class.getSimpleName());
    }

}
