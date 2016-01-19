package com.topface.topface.utils.controllers;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Класс реализующий запуск очереди попапов. При закрытии одного, сразу появляется другой
 * Created by onikitin on 17.03.15.
 */
public class SequencedStartAction implements IStartAction {

    private IUiRunner mUiRunner;
    private List<IStartAction> mActions = Collections.synchronizedList(new ArrayList<IStartAction>());
    private int mPriority = -1;

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

    /**
     * Выпиливаем все действия, которые не могут быть запущены
     */
    protected void removeNonApplicableActions() {
        synchronized (mActions) {
            Iterator<IStartAction> iterator = mActions.iterator();
            while (iterator.hasNext()) {
                IStartAction action = iterator.next();
                if (!action.isApplicable()) {
                    iterator.remove();
                }
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
        if (!mActions.isEmpty()) {
            runAction(mActions.get(0));
        }
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
                        if (mUiRunner instanceof BaseFragmentActivity) {
                            running = ((BaseFragmentActivity) mUiRunner).isRunning();
                        }
                        if (running && !mUiRunner.isFinishing()) {
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
            stringBuilder.append(startAction.getActionName()).append(Utils.SEMICOLON)
                    .append(startAction.getPriority())
                    .append(Utils.SEMICOLON)
                    .append(startAction.isApplicable())
                    .append(Utils.SEMICOLON);
        }
        return stringBuilder.toString();
    }

    public interface IUiRunner {
        void runOnUiThread(Runnable runnable);

        boolean isFinishing();
    }
}
