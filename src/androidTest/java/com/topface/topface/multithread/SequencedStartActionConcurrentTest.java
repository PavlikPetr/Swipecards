package com.topface.topface.multithread;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.utils.controllers.SequencedStartAction;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import junit.framework.TestCase;

/**
 * Тест для проверки метода removeNonApplicableActions в SequencedStartAction на предмет
 * java.util.ConcurrentModificationException
 * Created by onikitin on 06.05.15.
 */
public class SequencedStartActionConcurrentTest extends TestCase {

    private SequencedStartAction mSequencedStartAction = new SequencedStartAction(new SequencedStartAction.IUiRunner() {
        @Override
        public void runOnUiThread(Runnable runnable) {

        }

        @Override
        public boolean isFinishing() {
            return true;
        }
    }, 1);

    private final static int ACTION_COUNT = 5000;
    private final static int THREAD_COUNT = 2;


    public void testStartSequencedStartActionConcurrentTest() {
        for (int i = 0; i <= ACTION_COUNT; i++) {
            mSequencedStartAction.addAction(getNewAction());
        }
        for (int i = 0; i <= THREAD_COUNT; i++) {
            startThread();
        }
        try {
            Debug.debug(this, "Sleep start");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Debug.debug(this, "Test " + this.getName() + " complete");
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
                mSequencedStartAction.callInBackground();
            }
        };
    }

}
