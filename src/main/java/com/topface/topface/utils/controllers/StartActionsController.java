package com.topface.topface.utils.controllers;

import android.app.Activity;

import com.topface.topface.utils.BackgroundThread;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kirussell on 04.12.13.
 * Control actions on start: popups, tips, fullscreens, etc
 */
public class StartActionsController {
    /**
     * Indicates whether some action was processed on start or not.
     * Static field will die with App session
     */
    private static boolean processedActionForSession = false;

    public static final int PRIORITY_HIGH = 3;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_LOW = 1;
    private final Activity mActivity;

    private List<IStartAction> mPendingActions;

    public StartActionsController(Activity activity) {
        mPendingActions = new LinkedList<IStartAction>();
        mActivity = activity;
    }

    public void onLoadProfile() {
        if (!processedActionForSession) {
            processedActionForSession = startAction();
        }
    }

    /**
     * Takes necessary action and processes it. Inapplicable actions are removed from pending list.
     *
     * @return true if action was processed, false otherwise
     */
    private boolean startAction() {
        IStartAction action = getNextAction();
        while (action != null && !action.isApplicable()) {
            mPendingActions.remove(action);
            action = getNextAction();
        }
        boolean result = processAction(action);
        StartActionsController.processedActionForSession = result;
        return result;
    }

    /**
     * Process call-methods for action
     *
     * @param action chosen action
     */
    private boolean processAction(final IStartAction action) {
        if (action == null) return false;
        new BackgroundThread() {
            @Override
            public void execute() {
                action.callInBackground();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        action.callOnUi();
                    }
                });
            }
        };
        return true;
    }

    /**
     * Adds action to pending list of actions
     *
     * @param action which is needed to be process on start
     */
    public void registerAction(IStartAction action) {
        mPendingActions.add(action);
    }

    /**
     * Next action will be the first added with greater priority
     * Action is NOT removed from pending actions
     *
     * @return action from added actions
     */
    private IStartAction getNextAction() {
        int maxPriority = 0;
        IStartAction maxAction = null;
        for (IStartAction action : mPendingActions) {
            if (action.getPriority() > maxPriority) {
                maxAction = action;
            }
        }
        return maxAction;
    }

    public void clear() {
        if (mPendingActions != null && !mPendingActions.isEmpty()) {
            mPendingActions.clear();
        }
    }
}
