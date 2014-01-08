package com.topface.topface.utils.controllers;

/**
 * Start action object to pass
 */
public interface IStartAction {

    /**
     * synchronously called before callOnUi (background thread)
     */
    void callInBackground();

    /**
     * synchronously called after callInBackground (UI thread)
     */
    void callOnUi();

    /**
     * Determines whether the action can be called or not
     * Note: method realization must not change the state of action applicability, only shows one
     * You can change state on call-methods
     *
     * @return true if the action can be called
     */
    boolean isApplicable();

    /**
     * Defined values for priorities from StartActionsController
     *
     * @return AC_PRIORITY_HIGH, AC_PRIORITY_NORMAL, AC_PRIORITY_LOW;
     */
    int getPriority();

    /**
     * Needed for debug purposes
     *
     * @return string with format: "StartAction:[understandable action name]:[value of isApplicable()]"
     */
    String toDebugString();
}
