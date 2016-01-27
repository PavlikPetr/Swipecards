package com.topface.topface.utils.controllers.startactions;

import com.topface.topface.utils.Utils;

/**
 * Класс для действий которые будут выполнятся в очереди. Если действие должно быть ежедневным и
 * должно выполнятся в очереди, то нужно наследоваться от DailyPopupAction а эту логику заново реализовать
 * там
 * Created by onikitin on 17.03.15.
 */
public abstract class LinkedStartAction implements IStartAction {

    protected OnNextActionListener mOnNextActionListener;

    @Override
    public String toString() {
        return getActionName() + Utils.SEMICOLON +
                getPriority() + Utils.SEMICOLON +
                isApplicable();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }
}
