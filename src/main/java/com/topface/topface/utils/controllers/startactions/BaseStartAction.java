package com.topface.topface.utils.controllers.startactions;

import android.content.DialogInterface;

import com.topface.topface.ui.dialogs.BaseDialog;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.Nullable;

/**
 * Created by tiberal on 15.03.16.
 */
public abstract class BaseStartAction implements IStartAction {

    @Nullable
    private OnNextActionListener mOnNextActionListener;

    public BaseStartAction() {
        initPopup();
    }

    private void initPopup() {
        BaseDialog mPopup = getPopup();
        if (mPopup != null) {
            mPopup.setRetainInstance(true);
            mPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mOnNextActionListener != null) {
                        mOnNextActionListener.onNextAction();
                    }
                }
            });
        }
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }

    @Nullable
    public abstract BaseDialog getPopup();

    @Override
    public String toString() {
        return getActionName() + Utils.SEMICOLON +
                getPriority() + Utils.SEMICOLON +
                isApplicable();
    }
}
