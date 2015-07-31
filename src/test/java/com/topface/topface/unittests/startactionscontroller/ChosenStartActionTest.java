package com.topface.topface.unittests.startactionscontroller;

import com.topface.topface.unittests.startactionscontroller.utils.DummyAction;
import com.topface.topface.unittests.startactionscontroller.utils.DummyActionsApplier;
import com.topface.topface.utils.controllers.ChosenStartAction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kirussell on 23/07/15.
 * Unit tests for {@link ChosenStartAction} class
 */
public class ChosenStartActionTest {

    private DummyActionsApplier mApplyer = new DummyActionsApplier();
    private static final int SUCCESS = 1;


    @Test
    public void testChosenActionAppliance() {
        // empty action
        ChosenStartAction action = new ChosenStartAction();
        try {
            mApplyer.applyAction(action);
        } catch (Exception ex) {
            assertTrue(ex.toString(), false);
        }
        // ui call
        mApplyer.applyAction(new ChosenStartAction().chooseFrom(
                new DummyAction(SUCCESS, true).setResultListener(new DummyAction.IResultListener() {
                    @Override public void checkResult(int result) {
                        assertEquals(result, SUCCESS);
                    }
                })
        ));
        // background call
        mApplyer.applyAction(new ChosenStartAction().chooseFrom(
                new DummyAction(SUCCESS, DummyAction.UNPROCESSED, true).setResultListener(new DummyAction.IResultListener() {
                    @Override public void checkResult(int result) {
                        assertEquals(result, SUCCESS);
                    }
                })
        ));
        // applicable only
        mApplyer.applyAction(new ChosenStartAction().chooseFrom(
                new DummyAction(DummyAction.UNPROCESSED, false),
                new DummyAction(DummyAction.UNPROCESSED, false),
                new DummyAction(SUCCESS, true).setResultListener(new DummyAction.IResultListener() {
                    @Override public void checkResult(int result) {
                        assertEquals(result, SUCCESS);
                    }
                }),
                new DummyAction(DummyAction.UNPROCESSED, false),
                new DummyAction(DummyAction.UNPROCESSED, false)
        ));
        // take first with equal priorities
        mApplyer.applyAction(new ChosenStartAction().chooseFrom(
                new DummyAction(SUCCESS, SUCCESS, true).setResultListener(new DummyAction.IResultListener() {
                    @Override public void checkResult(int result) {
                        assertEquals(result, SUCCESS);
                    }
                }),
                new DummyAction(SUCCESS, SUCCESS, true)
        ));
        // take with higher priority
        mApplyer.applyAction(new ChosenStartAction().chooseFrom(
                new DummyAction(SUCCESS, true).setProiority(1),
                new DummyAction(SUCCESS, true).setResultListener(new DummyAction.IResultListener() {
                    @Override public void checkResult(int result) {
                        assertEquals(result, SUCCESS);
                    }
                }).setProiority(3),
                new DummyAction(SUCCESS, true).setProiority(2)
        ));
    }

}
