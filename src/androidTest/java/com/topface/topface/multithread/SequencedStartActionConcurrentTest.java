package com.topface.topface.multithread;

import android.test.InstrumentationTestCase;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.utils.controllers.SequencedStartAction;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

/**
 * Тест для проверки метода removeNonApplicableActions в SequencedStartAction на предмет
 * java.util.ConcurrentModificationException
 * Created by onikitin on 06.05.15.
 */
public class SequencedStartActionConcurrentTest extends InstrumentationTestCase {

    private SequencedStartAction mStartActionsController = new SequencedStartAction(new SequencedStartAction.IActivityEmulator() {
        @Override
        public void runOnUiThread(Runnable runnable) {

        }

        @Override
        public boolean isFinishing() {
            return true;
        }
    }, 1);

    private final static int ACTION_COUNT = 5000;
    private final static int THREAD_COUNT = 5;


    public void testStartSequencedStartActionConcurrentTest() {
        for (int i = 0; i <= ACTION_COUNT; i++) {
            mStartActionsController.addAction(getNewAction());
        }
        for (int i = 0; i <= THREAD_COUNT; i++) {
            startThread();
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IStartAction getNewAction() {
        return new IStartAction() {
            @Override
            public void callInBackground() {

            }

            @Override
            public void callOnUi() {

            }

            @Override
            public boolean isApplicable() {
                return false;
            }

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public String getActionName() {
                return null;
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {

            }
        };
    }

    private void startThread() {
        new BackgroundThread() {
            @Override
            public void execute() {
                mStartActionsController.callInBackground();
            }
        };
    }

}
