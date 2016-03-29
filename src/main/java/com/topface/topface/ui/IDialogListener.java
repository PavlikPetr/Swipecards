package com.topface.topface.ui;

/**
 * Created by Петр on 15.03.2016.
 * Interface to return click of dialog button
 */
public interface IDialogListener {
    void onPositiveButtonClick();

    void onNegativeButtonClick();

    void onDismissListener();
}