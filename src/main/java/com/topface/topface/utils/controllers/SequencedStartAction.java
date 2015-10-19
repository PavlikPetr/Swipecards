package com.topface.topface.utils.controllers;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс реализующий запуск очереди попапов. При закрытии одного, сразу появляется другой
 * Created by onikitin on 17.03.15.
 */
public class SequencedStartAction implements IStartAction {

    private IUiRunner mUiRunner;
    private List<IStartAction> mActions = Collections.synchronizedList(new ArrayList<IStartAction>());
    private int mPriority = -1;
    private Integer mCurrentActionPosition;

    public SequencedStartAction(IUiRunner uiRunner, int priority) {
        mPriority = priority;
        mUiRunner = uiRunner;
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
        //если в списке дейвствий есть хотя бы одно готовое к запуску, то true
        synchronized (mActions) {
            for (IStartAction startAction : mActions) {
                isApplicable = isApplicable || startAction.isApplicable();
            }
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

    public List<IStartAction> getActions() {
        return mActions;
    }

    private void setNewAction(IStartAction action) {
        String nextActionName = action != null ? action.getActionName() : "action = NULL";
        Debug.log("SequencedStartAction nextAction " + nextActionName);
        if (action != null) {
            runAction(action);
            action.setStartActionCallback(new OnNextActionListener() {
                @Override
                public void onNextAction() {
                    setNewAction(getNextAction());
                }

                @Override
                public void saveNextActionPosition() {
                    SequencedStartAction.this.saveNextActionPosition();
                }
            });
        } else {
            int pos = getAnavailableActionPosition();
            setCurrentActionPosition(pos);
            saveCurrentActionPosition();
        }

    }

    private int getAnavailableActionPosition() {
        return mActions != null ? mActions.size() : Integer.MAX_VALUE;
    }

    private void runActionQueue() {
        setNewAction(getFirstAction());
    }

    private IStartAction getNextAction() {
        return getAction(getCurrentActionPosition(), 1);
    }

    private void saveNextActionPosition() {
        Integer pos = getFirstAvailableActionPosition(getCurrentActionPosition(), 1);
        saveActionPosition(pos != null ? pos : getAnavailableActionPosition());
    }

    private IStartAction getFirstAction() {
        return getAction(getCurrentActionPosition(), 0);
    }

    private IStartAction getAction(int startPosition, int incr) {
        Integer position = getFirstAvailableActionPosition(startPosition, incr);
        if (position == null) {
            setCurrentActionPosition(0);
            return null;
        } else {
            setCurrentActionPosition(position);
            return mActions.get(position);
        }
    }

    private Integer getFirstAvailableActionPosition(int startPosition, int incr) {
        if (mActions != null && startPosition < mActions.size()) {
            for (int i = startPosition + incr; i < mActions.size(); i++) {
                if (mActions.get(i).isApplicable()) {
                    return i;
                }
            }
        }
        return null;
    }

    private void runAction(final IStartAction action) {
        new BackgroundThread() {
            @Override
            public void execute() {
                action.callInBackground();
                mUiRunner.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean running = true;
                        boolean isRestored = false;
                        if (mUiRunner instanceof BaseFragmentActivity) {
                            running = ((BaseFragmentActivity) mUiRunner).isRunning();
                            isRestored = ((BaseFragmentActivity) mUiRunner).isActivityRestoredState();
                        }
                        if (isRestored && running && !mUiRunner.isFinishing()) {
                            saveCurrentActionPosition();
                            Debug.log("SequencedStartAction action " + action.getActionName());
                            action.callOnUi();
                        }
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

    public interface IUiRunner {
        void runOnUiThread(Runnable runnable);

        boolean isFinishing();
    }

    private int getCurrentActionPosition() {
        if (mCurrentActionPosition == null) {
            mCurrentActionPosition = App.getUserConfig().getStartPositionOfActions();
        }
        return mCurrentActionPosition;
    }

    private void setCurrentActionPosition(int pos) {
        mCurrentActionPosition = pos;
    }

    private void saveCurrentActionPosition() {
        saveActionPosition(mCurrentActionPosition);
    }

    private void saveActionPosition(int pos) {
        UserConfig config = App.getUserConfig();
        config.setStartPositionOfActions(pos);
        config.saveConfig();
    }
}
