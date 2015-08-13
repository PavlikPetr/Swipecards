package com.topface.topface.utils.controllers;

import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kirussell on 23/07/15.
 * Chooses one of given with {@link #chooseFrom(IStartAction...)} actions to apply applicable
 * - support prioritizing
 * - chooses first given among equals
 * - checking applicability with {@link IStartAction#isApplicable()}
 */
public class ChosenStartAction implements IStartAction {

    ArrayList<IStartAction> mActions = new ArrayList<>(2);

    public ChosenStartAction chooseFrom(IStartAction... actions) {
        mActions.addAll(Arrays.asList(actions));
        return this;
    }

    private IStartAction chooseAction() {
        int maxPriority = -1;
        IStartAction maxAction = null;
        for (IStartAction action : mActions) {
            if (action.isApplicable()) {
                int priority = action.getPriority();
                if (priority > maxPriority) {
                    maxAction = action;
                    maxPriority = priority;
                }
            }
        }
        return maxAction;
    }

    @Override
    public void callInBackground() {
        IStartAction action = chooseAction();
        if (action != null) {
            action.callInBackground();
        }
    }

    @Override
    public void callOnUi() {
        IStartAction action = chooseAction();
        if (action != null) {
            action.callOnUi();
        }
    }

    @Override
    public boolean isApplicable() {
        IStartAction chosenAction = chooseAction();
        return chosenAction != null;
    }

    @Override
    public int getPriority() {
        int max = 0, tmp;
        for (IStartAction action : mActions) {
            tmp = action.getPriority();
            if (max < tmp) {
                max = tmp;
            }
        }
        return max;
    }

    @Override
    public String getActionName() {
        StringBuilder builder = new StringBuilder();
        for (IStartAction action : mActions) {
            builder.append(action.getActionName()).append(",");
        }
        return builder.toString();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        //empty
    }
}
