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
     *
     * @return true if the action can be called
     */
    boolean isApplicable();

    /**
     * Defined values for priorities from StartActionsController
     *
     * @return PRIORITY_HIGH, PRIORITY_NORMAL, PRIORITY_LOW;
     */
    int getPriority();
}
