package com.topface.topface.utils.controllers;

import android.app.Activity;

import com.topface.topface.App;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.Debug;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kirussell on 04.12.13.
 * Control actions on start: popups, tips, fullscreens, etc
 */
public class StartActionsController {
    private static final String TAG = "StartActionController";
    /**
     * Indicates whether some action was processed on start or not.
     * Static field will die with App session
     */
    private static boolean processedActionForSession = false;

    public static final int AC_PRIORITY_HIGH = 3;
    public static final int AC_PRIORITY_NORMAL = 2;
    public static final int AC_PRIORITY_LOW = 1;
    private final Activity mActivity;

    private List<IStartAction> mPendingActions;

    public StartActionsController(Activity activity) {
        mPendingActions = new LinkedList<IStartAction>();
        mActivity = activity;
    }

    public void onProcessAction() {
        new BackgroundThread() {
            @Override
            public void execute() {
                Debug.log(TAG, "try to process start action");
                if (!processedActionForSession) {
                    processedActionForSession = startAction();
                } else {
                    Debug.log(TAG, "some action already processed for this session");
                }
            }
        };
    }

    /**
     * Takes necessary action and processes it. Inapplicable actions are removed from pending list.
     *
     * @return true if action was processed, false otherwise
     */
    private boolean startAction() {
        if (App.DEBUG) {
            Debug.log(TAG, "===Pending start actions:===");
            for (IStartAction action : mPendingActions) {
                Debug.log(TAG, action.toString());
            }
            Debug.log(TAG, "============================");
        }
        IStartAction action = getNextAction();
        while (action != null && !action.isApplicable()) {
            mPendingActions.remove(action);
            action = getNextAction();
        }
        return processAction(action);
    }

    /**
     * Process call-methods for action
     *
     * @param action chosen action
     */
    private boolean processAction(final IStartAction action) {
        if (action == null) return false;
        action.callInBackground();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                action.callOnUi();
            }
        });
        Debug.log(TAG, "===>process chosen action - " + action.toString());
        return true;
    }

    /**
     * Adds action to pending list of actions
     *
     * @param action which is needed to be process on start
     */
    public void registerAction(IStartAction action) {
        mPendingActions.add(action);
        Debug.log(TAG, "register " + action.toString());
    }

    /**
     * Next action will be the first added with greater priority
     * Action is NOT removed from pending actions
     *
     * @return action from added actions
     */
    private IStartAction getNextAction() {
        int maxPriority = -1;
        IStartAction maxAction = null;
        for (IStartAction action : mPendingActions) {
            int priority = action.getPriority();
            if (priority > maxPriority) {
                maxAction = action;
                maxPriority = priority;
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
