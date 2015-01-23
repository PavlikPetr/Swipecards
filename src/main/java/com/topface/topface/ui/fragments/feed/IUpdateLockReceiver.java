package com.topface.topface.ui.fragments.feed;

/**
 * interface for feeds, for notify them, what update ability changed
 */
public interface IUpdateLockReceiver {
    /**
     * notify feed, what fragment with class name fragmentClassName is now selected
     * and can update its content
     *
     * @param fragmentClassName fragment class name, which now can update its content
     * @param stamp             timestamp from adapter, to allow this notification only for pages from adapter with same stamp
     */
    @SuppressWarnings("UnusedDeclaration")
    public void receiveUpdateLockAbility(String fragmentClassName, long stamp);
}
