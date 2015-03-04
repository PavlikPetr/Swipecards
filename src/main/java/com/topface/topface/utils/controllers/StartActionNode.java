package com.topface.topface.utils.controllers;

import com.topface.topface.Static;

import java.util.ArrayList;

/**
 * Created by onikitin on 04.03.15.
 * Класс содержащий очередь попапов для запуска.
 */
public class StartActionNode extends AbstractStartAction {

    int mGroupPriority = -1;

    public StartActionNode(int groupPriority) {
        this.mGroupPriority = groupPriority;
    }

    private ArrayList<IStartAction> mActions = new ArrayList<>();

    @Override
    public void addAction(IStartAction action) {
        if (action != null) {
            mActions.add(action);
        }
    }

    @Override
    public boolean hasMoreActions() {
        return true;
    }

    @Override
    public int getPriority() {
        return mGroupPriority;
    }

    @Override
    public void callInBackground() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void callOnUi() {
        throw new UnsupportedOperationException();
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
    public String getActionName() {
        return getClass().getSimpleName();
    }

    @Override
    public final String toString() {
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

    @Override
    public ArrayList<IStartAction> getActions() {
        return mActions;
    }
}
