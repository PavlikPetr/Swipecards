package com.topface.topface.utils.controllers;

import android.app.Activity;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.Static;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import java.util.ArrayList;

/**
 * Класс реализующий запуск очереди попапов. При закрытии одного, сразу появляется другой
 * Created by onikitin on 17.03.15.
 */
public class SequencedStartAction implements IStartAction {

    private Activity mActivity;
    private ArrayList<IStartAction> mActions = new ArrayList<>();
    private int mPriority = -1;

    public SequencedStartAction(Activity activity, int priority) {
        mPriority = priority;
        mActivity = activity;
    }

    @Override
    public void callInBackground() {
        runActionQueue();
    }

    @Override
    public void callOnUi() {

    }

    @Override
    public boolean isApplicable() {
        boolean isApplicable = false;
        //если в списке дейвствий есть хотя бы одно готовой к запуску, то true
        for (IStartAction startAction : mActions) {
            isApplicable = isApplicable || startAction.isApplicable();
        }
        return isApplicable;
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

    }

    public void addAction(IStartAction action) {
        mActions.add(action);
    }

    public ArrayList<IStartAction> getActions() {
        return mActions;
    }

    /**
     * Выпиливаем все действия, которые не могут быть запущены
     */
    private void removeNonApplicableActions() {
        for (IStartAction action : mActions) {
            if (!action.isApplicable()) {
                mActions.remove(action);
            }
        }
    }

    private void runActionQueue() {
        removeNonApplicableActions();
        for (int i = 0; i < mActions.size() - 1; i++) {
            final IStartAction nextAction = mActions.get(i + 1);
            mActions.get(i).setStartActionCallback(new OnNextActionListener() {
                @Override
                public void onNextAction() {
                    if (nextAction.isApplicable()) {
                        runAction(nextAction);
                    }
                }
            });
        }
        //запускаем очередь
        runAction(mActions.get(0));
    }

    private void runAction(final IStartAction action) {
        new BackgroundThread() {
            @Override
            public void execute() {
                action.callInBackground();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.isRestricted();
                        action.callOnUi();
                    }
                });
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (IStartAction startAction : mActions) {
            stringBuilder.append(startAction.getActionName()).append(Static.SEMICOLON)
                    .append(startAction.getPriority())
                    .append(Static.SEMICOLON)
                    .append(startAction.isApplicable())
                    .append(Static.SEMICOLON);
        }
        return stringBuilder.toString();
    }

}
