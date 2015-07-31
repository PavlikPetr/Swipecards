package com.topface.topface.unittests.startactionscontroller.utils;

import com.topface.topface.utils.controllers.startactions.IStartAction;

/**
 * Created by kirussell on 23/07/15.
 * Simplified startActions' processing like StartActionController for single action
 */
public class DummyActionsApplier {

    public void applyAction(IStartAction action) {
        if (action.isApplicable()) {
            action.callInBackground();
            action.callOnUi();
        }
    }
}
